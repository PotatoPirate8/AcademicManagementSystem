package com.academic.dao;

import com.academic.model.Enrollment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Enrollment CRUD operations.
 * Manages student-course enrollment relationships.
 */
public class EnrollmentDao {

    private final DatabaseManager dbManager;

    public EnrollmentDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Creates a new enrollment and returns the generated ID */
    public int create(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, enrollment.getStudentId());
            stmt.setInt(2, enrollment.getCourseId());
            stmt.setString(3, enrollment.getEnrollmentDate().toString());
            stmt.setString(4, enrollment.getStatus().name());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error creating enrollment: " + e.getMessage());
        }
        return -1;
    }

    /** Finds all enrollments for a specific student, with course details */
    public List<Enrollment> findByStudentId(int studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.course_code, c.course_name " +
                     "FROM enrollments e JOIN courses c ON e.course_id = c.id " +
                     "WHERE e.student_id = ? ORDER BY e.enrollment_date DESC";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                enrollments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollments: " + e.getMessage());
        }
        return enrollments;
    }

    /** Finds all enrollments with student and course details */
    public List<Enrollment> findAll() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.course_code, c.course_name, " +
                     "s.first_name || ' ' || s.last_name AS student_name " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN students s ON e.student_id = s.id " +
                     "ORDER BY e.enrollment_date DESC";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                enrollments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing enrollments: " + e.getMessage());
        }
        return enrollments;
    }

    /** Finds an enrollment by ID with full details */
    public Enrollment findById(int id) {
        String sql = "SELECT e.*, c.course_code, c.course_name, " +
                     "s.first_name || ' ' || s.last_name AS student_name " +
                     "FROM enrollments e " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN students s ON e.student_id = s.id " +
                     "WHERE e.id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding enrollment: " + e.getMessage());
        }
        return null;
    }

    /** Updates the status of an enrollment */
    public boolean updateStatus(int enrollmentId, Enrollment.Status status) {
        String sql = "UPDATE enrollments SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, enrollmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating enrollment: " + e.getMessage());
        }
        return false;
    }

    /** Deletes an enrollment by ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting enrollment: " + e.getMessage());
        }
        return false;
    }

    /** Checks if a student is currently enrolled (active) in a course */
    public boolean isEnrolled(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking enrollment: " + e.getMessage());
        }
        return false;
    }

    /** Maps a database row to an Enrollment object */
    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getInt("id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setCourseId(rs.getInt("course_id"));
        enrollment.setEnrollmentDate(LocalDate.parse(rs.getString("enrollment_date")));
        enrollment.setStatus(Enrollment.Status.valueOf(rs.getString("status")));
        try {
            enrollment.setCourseCode(rs.getString("course_code"));
            enrollment.setCourseName(rs.getString("course_name"));
        } catch (SQLException ignored) { /* Column may not be present */ }
        try {
            enrollment.setStudentName(rs.getString("student_name"));
        } catch (SQLException ignored) { /* Column may not be present */ }
        return enrollment;
    }
}
