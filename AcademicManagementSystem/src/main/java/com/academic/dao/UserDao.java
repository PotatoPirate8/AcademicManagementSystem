package com.academic.dao;

import com.academic.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User authentication and account management.
 */
public class UserDao {

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
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    /** Creates a new user account and returns the generated ID */
    public int create(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
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
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    /** Finds a user by username */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
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
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
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
