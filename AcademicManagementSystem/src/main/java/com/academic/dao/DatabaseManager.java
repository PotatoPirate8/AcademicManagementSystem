package com.academic.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and schema initialization.
 * Uses a singleton pattern so the same connection is shared throughout the app.
 */
public class DatabaseManager {

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
        }
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
