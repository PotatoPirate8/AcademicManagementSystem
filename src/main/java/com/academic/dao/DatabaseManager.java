package com.academic.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the SQLite database connection and schema initialization.
 * Uses a singleton pattern so the same connection is shared throughout the app.
 */
public class DatabaseManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:academic_system.db";
    private static final String DB_URL = resolveDatabaseUrl();
    private static DatabaseManager instance;
    private Connection connection;
    private final boolean shouldSeedDummyData;

    @FunctionalInterface
    public interface SqlWork<T> {
        T execute(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlVoidWork {
        void execute(Connection connection) throws SQLException;
    }

    private static String resolveDatabaseUrl() {
        String envUrl = System.getenv("ACADEMIC_DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl;
        }
        String propUrl = System.getProperty("academic.db.url");
        if (propUrl != null && !propUrl.isBlank()) {
            return propUrl;
        }
        return DEFAULT_DB_URL;
    }

    /** Private constructor for default production database */
    private DatabaseManager() {
        try {
            LOGGER.info("Initializing DatabaseManager with default URL: {}", DB_URL);
            shouldSeedDummyData = true;
            connection = DriverManager.getConnection(DB_URL);
            LOGGER.info("Database connection established");
            initializeSchema();
            LOGGER.info("Database schema initialized successfully");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /** Private constructor with custom URL (used for testing) */
    private DatabaseManager(String url) {
        try {
            LOGGER.info("Initializing DatabaseManager with custom URL: {}", url);
            shouldSeedDummyData = DB_URL.equals(url);
            connection = DriverManager.getConnection(url);
            LOGGER.info("Database connection established (test mode)");
            initializeSchema();
            LOGGER.info("Database schema initialized successfully (test mode)");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /** Returns the singleton instance of DatabaseManager */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /** Returns the active database connection */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            configureConnection(connection);
        }
        return connection;
    }

    /** Executes a unit of work inside a database transaction. */
    public synchronized <T> T executeInTransaction(SqlWork<T> work) throws SQLException {
        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            LOGGER.debug("Starting database transaction");
            if (originalAutoCommit) {
                conn.setAutoCommit(false);
            }
            T result = work.execute(conn);
            conn.commit();
            LOGGER.debug("Transaction committed successfully");
            return result;
        } catch (SQLException e) {
            LOGGER.error("Transaction failed, rolling back: {}", e.getMessage(), e);
            conn.rollback();
            throw e;
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected error during transaction, rolling back: {}", e.getMessage(), e);
            conn.rollback();
            throw e;
        } finally {
            if (originalAutoCommit) {
                conn.setAutoCommit(true);
            }
        }
    }

    private void configureConnection(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA busy_timeout = 5000");
        }
    }

    /**
     * Creates all required database tables if they do not already exist.
     * Also inserts a default admin account for first-time setup.
     */
    private void initializeSchema() throws SQLException {
        configureConnection(connection);
        executeInTransaction(conn -> {
            ensureMigrationsTable(conn);
            applyPendingMigrations(conn);
            seedInitialData(conn);
            return null;
        });
    }

    private void ensureMigrationsTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schema_migrations (
                    version INTEGER PRIMARY KEY,
                    description TEXT NOT NULL,
                    applied_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    private void applyPendingMigrations(Connection conn) throws SQLException {
        int currentVersion = getCurrentSchemaVersion(conn);
        LOGGER.info("Current database schema version: {}", currentVersion);
        for (Migration migration : getMigrations()) {
            if (migration.version() > currentVersion) {
                LOGGER.info("Applying migration v{}: {}", migration.version(), migration.description());
                migration.run(conn);
                recordMigration(conn, migration);
                LOGGER.info("Migration v{} completed successfully", migration.version());
            }
        }
    }

    private int getCurrentSchemaVersion(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(version), 0) FROM schema_migrations";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<Migration> getMigrations() {
        List<Migration> migrations = new ArrayList<>();
        migrations.add(new Migration(1, "Create core tables", this::migrationV1CreateCoreTables));
        migrations.add(new Migration(2, "Add reliability indexes", this::migrationV2AddIndexes));
        return migrations;
    }

    private void recordMigration(Connection conn, Migration migration) throws SQLException {
        String sql = "INSERT INTO schema_migrations (version, description) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, migration.version());
            stmt.setString(2, migration.description());
            stmt.executeUpdate();
        }
    }

    private void migrationV1CreateCoreTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('STUDENT', 'ADMIN', 'LECTURER'))
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER UNIQUE NOT NULL,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    student_number TEXT UNIQUE NOT NULL,
                    programme TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lecturers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    department TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_code TEXT UNIQUE NOT NULL,
                    course_name TEXT NOT NULL,
                    credits INTEGER NOT NULL,
                    lecturer_id INTEGER,
                    max_capacity INTEGER NOT NULL DEFAULT 30,
                    FOREIGN KEY (lecturer_id) REFERENCES lecturers(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS enrollments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    course_id INTEGER NOT NULL,
                    enrollment_date TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'ENROLLED'
                        CHECK(status IN ('ENROLLED', 'COMPLETED', 'WITHDRAWN')),
                    FOREIGN KEY (student_id) REFERENCES students(id),
                    FOREIGN KEY (course_id) REFERENCES courses(id),
                    UNIQUE(student_id, course_id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS grades (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    enrollment_id INTEGER UNIQUE NOT NULL,
                    grade_value REAL NOT NULL CHECK(grade_value >= 0 AND grade_value <= 100),
                    grade_letter TEXT NOT NULL,
                    feedback TEXT,
                    graded_date TEXT NOT NULL,
                    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id)
                )
            """);
        }
    }

    private void migrationV2AddIndexes(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_students_user_id ON students(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enrollments_student_status ON enrollments(student_id, status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_enrollments_course_id ON enrollments(course_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_grades_enrollment_id ON grades(enrollment_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_grades_graded_date ON grades(graded_date)");
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Insert default admin account (password: admin123)
            stmt.execute("""
                INSERT OR IGNORE INTO users (username, password_hash, role)
                VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN')
            """);

            // Seed dummy data only for the default production database.
            if (shouldSeedDummyData) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lecturers");
                rs.next();
                if (rs.getInt(1) == 0) {
                    seedDummyData(stmt);
                }
            }
        }
    }

    private record Migration(int version, String description, SqlVoidWork action) {
        void run(Connection conn) throws SQLException {
            action.execute(conn);
        }
    }

    /** Inserts sample lecturers, students, courses, enrollments, and grades */
    private void seedDummyData(Statement stmt) throws SQLException {
        // Lecturers
        stmt.execute("""
            INSERT INTO lecturers (first_name, last_name, email, department) VALUES
            ('James', 'Smith', 'j.smith@soton.ac.uk', 'Computer Science'),
            ('Sarah', 'Johnson', 's.johnson@soton.ac.uk', 'Computer Science'),
            ('Michael', 'Brown', 'm.brown@soton.ac.uk', 'Mathematics'),
            ('Emily', 'Davis', 'e.davis@soton.ac.uk', 'Electronics')
        """);

        // Student user accounts (password: password123)
        String studentHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f";
        stmt.execute("INSERT INTO users (username, password_hash, role) VALUES ('jdoe', '" + studentHash + "', 'STUDENT')");
        stmt.execute("INSERT INTO users (username, password_hash, role) VALUES ('asmith', '" + studentHash + "', 'STUDENT')");
        stmt.execute("INSERT INTO users (username, password_hash, role) VALUES ('bwilson', '" + studentHash + "', 'STUDENT')");
        stmt.execute("INSERT INTO users (username, password_hash, role) VALUES ('clee', '" + studentHash + "', 'STUDENT')");
        stmt.execute("INSERT INTO users (username, password_hash, role) VALUES ('dpatel', '" + studentHash + "', 'STUDENT')");

        // Students (user_id 2-6, since admin is user_id 1)
        stmt.execute("""
            INSERT INTO students (user_id, first_name, last_name, email, student_number, programme) VALUES
            (2, 'John', 'Doe', 'jd2e25@soton.ac.uk', 'JD2025', 'BSc Computer Science'),
            (3, 'Alice', 'Smith', 'as3e25@soton.ac.uk', 'AS2025', 'BSc Computer Science'),
            (4, 'Ben', 'Wilson', 'bw1e25@soton.ac.uk', 'BW2025', 'MEng Software Engineering'),
            (5, 'Clara', 'Lee', 'cl4e25@soton.ac.uk', 'CL2025', 'BSc Mathematics'),
            (6, 'Dev', 'Patel', 'dp5e25@soton.ac.uk', 'DP2025', 'MEng Computer Science')
        """);

        // Courses (with lecturer_id 1-4)
        stmt.execute("""
            INSERT INTO courses (course_code, course_name, credits, lecturer_id, max_capacity) VALUES
            ('COMP1322', 'Computational Thinking', 15, 1, 120),
            ('COMP1206', 'Programming 2', 15, 1, 100),
            ('COMP1216', 'Software Modelling', 15, 2, 80),
            ('MATH1060', 'Calculus', 15, 3, 150),
            ('ELEC1201', 'Digital Systems', 15, 4, 90)
        """);

        // Enrollments (student_id 1-5 maps to actual student record IDs, course_id 1-5 maps to actual course record IDs)
        stmt.execute("""
            INSERT INTO enrollments (student_id, course_id, enrollment_date, status) VALUES
            (1, 1, '2025-09-22', 'ENROLLED'),
            (1, 2, '2025-09-22', 'ENROLLED'),
            (1, 4, '2025-09-22', 'COMPLETED'),
            (2, 1, '2025-09-22', 'ENROLLED'),
            (2, 3, '2025-09-22', 'ENROLLED'),
            (3, 1, '2025-09-22', 'ENROLLED'),
            (3, 2, '2025-09-22', 'COMPLETED'),
            (3, 3, '2025-09-22', 'ENROLLED'),
            (4, 4, '2025-09-22', 'ENROLLED'),
            (4, 5, '2025-09-22', 'WITHDRAWN'),
            (5, 1, '2025-09-22', 'ENROLLED'),
            (5, 2, '2025-09-22', 'ENROLLED')
        """);

        // Grades (only for some enrollments)
        stmt.execute("""
            INSERT INTO grades (enrollment_id, grade_value, grade_letter, feedback, graded_date) VALUES
            (3, 72.5, 'A', 'Excellent work on calculus assignments.', '2026-01-15'),
            (7, 65.0, 'B', 'Good programming skills demonstrated.', '2026-01-15'),
            (1, 58.0, 'C', 'Solid understanding of core concepts.', '2026-02-10'),
            (4, 81.0, 'A', 'Outstanding coursework and exam performance.', '2026-02-10'),
            (2, 45.0, 'D', 'Needs improvement in object-oriented design.', '2026-02-10')
        """);
    }

    /** Closes the database connection */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }

    /** Resets the singleton instance (used for testing) */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    /** Creates a DatabaseManager with a custom URL (used for in-memory testing) */
    public static synchronized void initializeWithUrl(String url) {
        resetInstance();
        instance = new DatabaseManager(url);
    }
}
