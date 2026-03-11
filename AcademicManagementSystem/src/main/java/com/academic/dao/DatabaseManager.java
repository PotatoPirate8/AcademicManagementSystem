package com.academic.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and schema initialization.
 * Uses a singleton pattern so the same connection is shared throughout the app.
 */
public class DatabaseManager {

    // Change the path below to store the database wherever your Java source files are located
    private static final String DB_URL = "jdbc:sqlite:academic_system.db";
    private static DatabaseManager instance;
    private Connection connection;

    /** Private constructor for default production database */
    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeSchema();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /** Private constructor with custom URL (used for testing) */
    private DatabaseManager(String url) {
        try {
            connection = DriverManager.getConnection(url);
            initializeSchema();
        } catch (SQLException e) {
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
        }
        return connection;
    }

    /**
     * Creates all required database tables if they do not already exist.
     * Also inserts a default admin account for first-time setup.
     */
    private void initializeSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table for authentication
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('STUDENT', 'ADMIN'))
                )
            """);

            // Students table linked to user accounts
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

            // Lecturers table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lecturers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    department TEXT NOT NULL
                )
            """);

            // Courses table with lecturer assignment
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

            // Enrollments table linking students to courses
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

            // Grades table linked to enrollments
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

            // Insert default admin account (password: admin123)
            stmt.execute("""
                INSERT OR IGNORE INTO users (username, password_hash, role)
                VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN')
            """);

            // Seed dummy data if the database is fresh (no lecturers yet)
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM lecturers");
            rs.next();
            if (rs.getInt(1) == 0) {
                seedDummyData(stmt);
            }
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

        // Courses
        stmt.execute("""
            INSERT INTO courses (course_code, course_name, credits, lecturer_id, max_capacity) VALUES
            ('COMP1322', 'Computational Thinking', 15, 1, 120),
            ('COMP1206', 'Programming 2', 15, 1, 100),
            ('COMP1216', 'Software Modelling', 15, 2, 80),
            ('MATH1060', 'Calculus', 15, 3, 150),
            ('ELEC1201', 'Digital Systems', 15, 4, 90)
        """);

        // Enrollments
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
