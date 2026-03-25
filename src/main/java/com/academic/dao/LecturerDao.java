package com.academic.dao;

import com.academic.model.Lecturer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Lecturer CRUD operations.
 */
public class LecturerDao {

    private final DatabaseManager dbManager;

    public LecturerDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Creates a new lecturer and returns the generated ID */
    public int create(Lecturer lecturer) {
        String sql = "INSERT INTO lecturers (first_name, last_name, email, department) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, lecturer.getFirstName());
            stmt.setString(2, lecturer.getLastName());
            stmt.setString(3, lecturer.getEmail());
            stmt.setString(4, lecturer.getDepartment());
            stmt.executeUpdate();
            try (ResultSet keys = dbManager.getConnection().createStatement()
                    .executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating lecturer: " + e.getMessage());
        }
        return -1;
    }

    /** Finds a lecturer by ID */
    public Lecturer findById(int id) {
        String sql = "SELECT * FROM lecturers WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding lecturer: " + e.getMessage());
        }
        return null;
    }

    /** Returns all lecturers ordered by last name */
    public List<Lecturer> findAll() {
        List<Lecturer> lecturers = new ArrayList<>();
        String sql = "SELECT * FROM lecturers ORDER BY last_name, first_name";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lecturers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing lecturers: " + e.getMessage());
        }
        return lecturers;
    }

    /** Updates an existing lecturer */
    public boolean update(Lecturer lecturer) {
        String sql = "UPDATE lecturers SET first_name = ?, last_name = ?, email = ?, department = ? WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, lecturer.getFirstName());
            stmt.setString(2, lecturer.getLastName());
            stmt.setString(3, lecturer.getEmail());
            stmt.setString(4, lecturer.getDepartment());
            stmt.setInt(5, lecturer.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating lecturer: " + e.getMessage());
        }
        return false;
    }

    /** Deletes a lecturer by ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM lecturers WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting lecturer: " + e.getMessage());
        }
        return false;
    }

    /** Maps a database row to a Lecturer object */
    private Lecturer mapRow(ResultSet rs) throws SQLException {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(rs.getInt("id"));
        lecturer.setFirstName(rs.getString("first_name"));
        lecturer.setLastName(rs.getString("last_name"));
        lecturer.setEmail(rs.getString("email"));
        lecturer.setDepartment(rs.getString("department"));
        return lecturer;
    }
}
