package com.academic.service;

import com.academic.dao.CourseDao;
import com.academic.dao.DatabaseManager;
import com.academic.dao.LecturerDao;
import com.academic.model.Course;
import com.academic.model.Lecturer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service-layer tests for course validation and CRUD workflows.
 */
class CourseServiceTest {

    private static CourseService courseService;
    private static CourseDao courseDao;
    private static Lecturer lecturer;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        courseService = new CourseService();
        courseDao = new CourseDao();

        LecturerDao lecturerDao = new LecturerDao();
        int lecturerId = lecturerDao.create(new Lecturer("Test", "Lecturer", "svclect@test.com", "CS"));
        lecturer = lecturerDao.findById(lecturerId);
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    @Test
    void testAddCourseRejectsInvalidCode() {
        ServiceResult<Void> result = courseService.addCourse("BAD", "Course", "15", "30", lecturer);
        assertFalse(result.isSuccess());
        assertEquals("Course code must be 2-5 letters followed by 3-5 digits (e.g., COMP1322).", result.getMessage());
    }

    @Test
    void testAddCourseSuccessAndDuplicateCodeFailure() {
        ServiceResult<Void> first = courseService.addCourse("COMP9999", "Service Testing", "15", "25", lecturer);
        assertTrue(first.isSuccess());

        ServiceResult<Void> duplicate = courseService.addCourse("COMP9999", "Service Testing 2", "15", "25", lecturer);
        assertFalse(duplicate.isSuccess());
        assertEquals("Failed to add course. Code may already exist.", duplicate.getMessage());
    }

    @Test
    void testUpdateCourseValidationAndSuccess() {
        ServiceResult<Void> create = courseService.addCourse("COMP9988", "Update Candidate", "15", "20", lecturer);
        assertTrue(create.isSuccess());

        Course existing = courseDao.findAll().stream()
            .filter(c -> "COMP9988".equals(c.getCourseCode()))
            .findFirst()
            .orElse(null);
        assertNotNull(existing);

        ServiceResult<Void> invalid = courseService.updateCourse(existing, "X1", "Name", "15", "25", lecturer);
        assertFalse(invalid.isSuccess());

        ServiceResult<Void> success = courseService.updateCourse(existing, "COMP9998", "Updated Name", "20", "40", lecturer);
        assertTrue(success.isSuccess());

        Course updated = courseDao.findById(existing.getId());
        assertNotNull(updated);
        assertEquals("COMP9998", updated.getCourseCode());
        assertEquals("Updated Name", updated.getCourseName());
        assertEquals(20, updated.getCredits());
        assertEquals(40, updated.getMaxCapacity());
    }
}
