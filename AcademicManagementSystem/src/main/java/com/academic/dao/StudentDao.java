package com.academic.dao;

import com.academic.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student CRUD operations.
 */
public class StudentDao {

    private final DatabaseManager dbManager;

    public StudentDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Creates a new student record and returns the generated ID */
    public int create(Student student) {
        String sql = "INSERT INTO students (user_id, first_name, last_name, email, student_number, programme) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, student.getUserId());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getStudentNumber());
            stmt.setString(6, student.getProgramme());
            stmt.executeUpdate();
            try (ResultSet keys = dbManager.getConnection().createStatement()
                    .executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating student: " + e.getMessage());
        }
        return -1;
    }

    /** Finds a student by their linked user account ID */
    public Student findByUserId(int userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding student: " + e.getMessage());
        }
        return null;
    }

    /** Finds a student by their ID */
    public Student findById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding student: " + e.getMessage());
        }
        return null;
    }

    /** Returns all students ordered by last name */
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing students: " + e.getMessage());
        }
        return students;
    }

    /** Updates an existing student record */
    public boolean update(Student student) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, " +
                     "student_number = ?, programme = ? WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setString(3, student.getEmail());
            stmt.setString(4, student.getStudentNumber());
            stmt.setString(5, student.getProgramme());
            stmt.setInt(6, student.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
        }
        return false;
    }

    /** Deletes a student by ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
        }
        return false;
    }

    /** Maps a database row to a Student object */
    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setUserId(rs.getInt("user_id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));
        student.setStudentNumber(rs.getString("student_number"));
        student.setProgramme(rs.getString("programme"));
        return student;
    }
}
