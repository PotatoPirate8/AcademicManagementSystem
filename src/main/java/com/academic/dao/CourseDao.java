package com.academic.dao;

import com.academic.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course CRUD operations.
 * Joins with lecturers table to include lecturer name in results.
 */
public class CourseDao {

    private final DatabaseManager dbManager;

    public CourseDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /** Creates a new course and returns the generated ID */
    public int create(Course course) {
        String sql = "INSERT INTO courses (course_code, course_name, credits, lecturer_id, max_capacity) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getLecturerId());
            stmt.setInt(5, course.getMaxCapacity());
            stmt.executeUpdate();
            try (ResultSet keys = dbManager.getConnection().createStatement()
                    .executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating course: " + e.getMessage());
        }
        return -1;
    }

    /** Finds a course by ID, including the lecturer name */
    public Course findById(int id) {
        String sql = "SELECT c.*, l.first_name || ' ' || l.last_name AS lecturer_name " +
                     "FROM courses c LEFT JOIN lecturers l ON c.lecturer_id = l.id WHERE c.id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding course: " + e.getMessage());
        }
        return null;
    }

    /** Returns all courses with lecturer names, ordered by course code */
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, l.first_name || ' ' || l.last_name AS lecturer_name " +
                     "FROM courses c LEFT JOIN lecturers l ON c.lecturer_id = l.id ORDER BY c.course_code";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing courses: " + e.getMessage());
        }
        return courses;
    }

    /** Returns the number of currently enrolled students in a course */
    public int getEnrollmentCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND status = 'ENROLLED'";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting enrollments: " + e.getMessage());
        }
        return 0;
    }

    /** Updates an existing course */
    public boolean update(Course course) {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, credits = ?, " +
                     "lecturer_id = ?, max_capacity = ? WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getLecturerId());
            stmt.setInt(5, course.getMaxCapacity());
            stmt.setInt(6, course.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
        }
        return false;
    }

    /** Deletes a course by ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
        }
        return false;
    }

    /** Maps a database row to a Course object */
    private Course mapRow(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setCredits(rs.getInt("credits"));
        course.setLecturerId(rs.getInt("lecturer_id"));
        course.setMaxCapacity(rs.getInt("max_capacity"));
        try {
            course.setLecturerName(rs.getString("lecturer_name"));
        } catch (SQLException ignored) {
            // lecturer_name column may not be present in all queries
        }
        return course;
    }
}
