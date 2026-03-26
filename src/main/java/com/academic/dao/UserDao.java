package com.academic.dao;

import com.academic.model.User;
import com.academic.util.ErrorHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object for User authentication and account management.
 */
public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
    private final DatabaseManager dbManager;

    public UserDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Authenticates a user with username and password hash */
    public User authenticate(String username, String passwordHash) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapRow(rs);
                ErrorHandler.logAuthEvent("LOGIN", username, true, "Authentication successful");
                return user;
            }
            ErrorHandler.logAuthEvent("LOGIN", username, false, "Invalid credentials");
        } catch (SQLException e) {
            ErrorHandler.handleError("authenticating user: " + username, e);
            ErrorHandler.logAuthEvent("LOGIN", username, false, e.getMessage());
        }
        return null;
    }

    /** Creates a new user account and returns the generated ID */
    public int create(User user) {
        try {
            return create(user, dbManager.getConnection());
        } catch (SQLException e) {
            ErrorHandler.handleError("creating user: " + user.getUsername(), e);
            return -1;
        }
    }

    /** Creates a new user account and returns the generated ID using the provided connection. */
    public int create(User user, Connection conn) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.executeUpdate();
            try (ResultSet keys = conn.createStatement()
                    .executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) {
                    int userId = keys.getInt(1);
                    ErrorHandler.logDatabaseOperation("INSERT", "User", userId, true, null);
                    LOGGER.info("User created successfully: username={}, id={}", user.getUsername(), userId);
                    return userId;
                }
            }
        } catch (SQLException e) {
            ErrorHandler.logDatabaseOperation("INSERT", "User", -1, false, e);
            ErrorHandler.handleError("creating user: " + user.getUsername(), e);
        }
        return -1;
    }

    /** Finds a user by their ID */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LOGGER.debug("User found with ID: {}", id);
                return mapRow(rs);
            }
            LOGGER.debug("No user found with ID: {}", id);
        } catch (SQLException e) {
            ErrorHandler.handleError("finding user with ID: " + id, e);
        }
        return null;
    }

    /** Finds a user by username */
    public User findByUsername(String username) {
        try {
            return findByUsername(username, dbManager.getConnection());
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
            return null;
        }
    }

    /** Finds a user by username using the provided connection. */
    public User findByUsername(String username, Connection conn) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    /** Returns all users in the system */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
        return users;
    }

    /** Updates a user's password */
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
        return false;
    }

    /** Deletes a user by ID */
    public boolean delete(int id) {
        try {
            return delete(id, dbManager.getConnection());
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /** Deletes a user by ID using the provided connection. */
    public boolean delete(int id, Connection conn) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    /** Maps a database row to a User object */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            User.Role.valueOf(rs.getString("role"))
        );
    }
}
