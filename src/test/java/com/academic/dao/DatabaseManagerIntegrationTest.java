package com.academic.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests focused on schema migration and database reliability setup.
 */
class DatabaseManagerIntegrationTest {

    private static DatabaseManager databaseManager;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        databaseManager = DatabaseManager.getInstance();
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    @Test
    void testSchemaMigrationsApplied() throws SQLException {
        Connection conn = databaseManager.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT COUNT(*) AS total, MAX(version) AS latest FROM schema_migrations"
        )) {
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertTrue(rs.getInt("total") >= 2, "Expected at least two applied migrations");
            assertEquals(2, rs.getInt("latest"), "Expected latest migration version to be 2");
        }
    }

    @Test
    void testForeignKeysEnabled() throws SQLException {
        Connection conn = databaseManager.getConnection();
        assertThrows(SQLException.class, () -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO students (user_id, first_name, last_name, email, student_number, programme) VALUES (?, ?, ?, ?, ?, ?)"
            )) {
                stmt.setInt(1, 99999);
                stmt.setString(2, "No");
                stmt.setString(3, "User");
                stmt.setString(4, "nouser@test.com");
                stmt.setString(5, "NU1234");
                stmt.setString(6, "CS");
                stmt.executeUpdate();
            }
        });
    }
}
