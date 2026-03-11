package com.academic.dao;

import com.academic.model.Grade;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Grade CRUD operations.
 * Also provides report generation queries (averages, distributions, etc.).
 */
public class GradeDao {

    private final DatabaseManager dbManager;

    public GradeDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Creates a new grade and returns the generated ID */
    public int create(Grade grade) {
        String sql = "INSERT INTO grades (enrollment_id, grade_value, grade_letter, feedback, graded_date) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, grade.getEnrollmentId());
            stmt.setDouble(2, grade.getGradeValue());
            stmt.setString(3, grade.getGradeLetter());
            stmt.setString(4, grade.getFeedback());
            stmt.setString(5, grade.getGradedDate().toString());
            stmt.executeUpdate();
            try (ResultSet keys = dbManager.getConnection().createStatement()
                    .executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating grade: " + e.getMessage());
        }
        return -1;
    }

    /** Finds all grades for a specific student */
    public List<Grade> findByStudentId(int studentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.course_code, c.course_name " +
                     "FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.id " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "WHERE e.student_id = ? ORDER BY g.graded_date DESC";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                grades.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding grades: " + e.getMessage());
        }
        return grades;
    }

    /** Finds all grades with course and student details */
    public List<Grade> findAll() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.course_code, c.course_name, " +
                     "s.first_name || ' ' || s.last_name AS student_name " +
                     "FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.id " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN students s ON e.student_id = s.id " +
                     "ORDER BY g.graded_date DESC";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                grades.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing grades: " + e.getMessage());
        }
        return grades;
    }

    /** Updates an existing grade */
    public boolean update(Grade grade) {
        String sql = "UPDATE grades SET grade_value = ?, grade_letter = ?, feedback = ?, graded_date = ? WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, grade.getGradeValue());
            stmt.setString(2, grade.getGradeLetter());
            stmt.setString(3, grade.getFeedback());
            stmt.setString(4, grade.getGradedDate().toString());
            stmt.setInt(5, grade.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating grade: " + e.getMessage());
        }
        return false;
    }

    /** Deletes a grade by ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM grades WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting grade: " + e.getMessage());
        }
        return false;
    }

    /** Checks if a grade already exists for a given enrollment */
    public boolean existsForEnrollment(int enrollmentId) {
        String sql = "SELECT COUNT(*) FROM grades WHERE enrollment_id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking grade: " + e.getMessage());
        }
        return false;
    }

    // ==================== Report Queries ====================

    /** Returns average grade per course (used in Average Grade report) */
    public Map<String, Double> getAverageGradeByCourse() {
        Map<String, Double> averages = new HashMap<>();
        String sql = "SELECT c.course_code || ' - ' || c.course_name AS course, AVG(g.grade_value) AS avg_grade " +
                     "FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.id " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "GROUP BY c.id ORDER BY c.course_code";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                averages.put(rs.getString("course"), rs.getDouble("avg_grade"));
            }
        } catch (SQLException e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
        return averages;
    }

    /** Returns aggregate statistics (count, avg, max, min) for a specific course */
    public Map<String, Object> getCourseGradeStats(int courseId) {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(g.id) AS total, AVG(g.grade_value) AS average, " +
                     "MAX(g.grade_value) AS maximum, MIN(g.grade_value) AS minimum " +
                     "FROM grades g JOIN enrollments e ON g.enrollment_id = e.id " +
                     "WHERE e.course_id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("average", rs.getDouble("average"));
                stats.put("maximum", rs.getDouble("maximum"));
                stats.put("minimum", rs.getDouble("minimum"));
            }
        } catch (SQLException e) {
            System.err.println("Error generating stats: " + e.getMessage());
        }
        return stats;
    }

    /** Returns grade letter distribution (count per letter) for a course */
    public Map<String, Integer> getGradeDistribution(int courseId) {
        // Initialize all possible letter grades to zero
        Map<String, Integer> distribution = new HashMap<>();
        for (String letter : new String[]{"A", "B", "C", "D", "F"}) {
            distribution.put(letter, 0);
        }
        String sql = "SELECT g.grade_letter, COUNT(*) AS count FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.id " +
                     "WHERE e.course_id = ? GROUP BY g.grade_letter";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distribution.put(rs.getString("grade_letter"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error generating distribution: " + e.getMessage());
        }
        return distribution;
    }

    /** Returns grades filtered by a date range */
    public List<Grade> findByDateRange(LocalDate from, LocalDate to) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.course_code, c.course_name, " +
                     "s.first_name || ' ' || s.last_name AS student_name " +
                     "FROM grades g " +
                     "JOIN enrollments e ON g.enrollment_id = e.id " +
                     "JOIN courses c ON e.course_id = c.id " +
                     "JOIN students s ON e.student_id = s.id " +
                     "WHERE g.graded_date BETWEEN ? AND ? " +
                     "ORDER BY g.graded_date DESC";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, from.toString());
            stmt.setString(2, to.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                grades.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error filtering grades: " + e.getMessage());
        }
        return grades;
    }

    /** Returns a count of enrollments grouped by status */
    public Map<String, Integer> getEnrollmentStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS count FROM enrollments GROUP BY status";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error generating status report: " + e.getMessage());
        }
        return counts;
    }

    /** Maps a database row to a Grade object */
    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getInt("id"));
        grade.setEnrollmentId(rs.getInt("enrollment_id"));
        grade.setGradeValue(rs.getDouble("grade_value"));
        grade.setGradeLetter(rs.getString("grade_letter"));
        grade.setFeedback(rs.getString("feedback"));
        grade.setGradedDate(LocalDate.parse(rs.getString("graded_date")));
        try {
            grade.setCourseCode(rs.getString("course_code"));
            grade.setCourseName(rs.getString("course_name"));
        } catch (SQLException ignored) { /* Column may not be present */ }
        try {
            grade.setStudentName(rs.getString("student_name"));
        } catch (SQLException ignored) { /* Column may not be present */ }
        return grade;
    }
}
