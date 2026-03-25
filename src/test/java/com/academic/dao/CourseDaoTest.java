package com.academic.dao;

import com.academic.model.Course;
import com.academic.model.Lecturer;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CourseDao and LecturerDao using an in-memory SQLite database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseDaoTest {

    private static CourseDao courseDao;
    private static LecturerDao lecturerDao;
    private static int testLecturerId;
    private static int testCourseId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        courseDao = new CourseDao();
        lecturerDao = new LecturerDao();

        // Create a lecturer for the test courses
        Lecturer lecturer = new Lecturer("Test", "Lecturer", "test@uni.com", "Computer Science");
        testLecturerId = lecturerDao.create(lecturer);
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    // ==================== Lecturer Tests ====================

    @Test
    @Order(1)
    void testLecturerCreated() {
        assertTrue(testLecturerId > 0, "Lecturer should be created successfully");
    }

    @Test
    @Order(2)
    void testFindLecturerById() {
        Lecturer lecturer = lecturerDao.findById(testLecturerId);
        assertNotNull(lecturer);
        assertEquals("Test", lecturer.getFirstName());
        assertEquals("Lecturer", lecturer.getLastName());
        assertEquals("test@uni.com", lecturer.getEmail());
        assertEquals("Computer Science", lecturer.getDepartment());
    }

    @Test
    @Order(3)
    void testFindAllLecturers() {
        List<Lecturer> lecturers = lecturerDao.findAll();
        assertFalse(lecturers.isEmpty());
    }

    @Test
    @Order(4)
    void testUpdateLecturer() {
        Lecturer lecturer = lecturerDao.findById(testLecturerId);
        assertNotNull(lecturer);
        lecturer.setDepartment("Mathematics");
        assertTrue(lecturerDao.update(lecturer));

        Lecturer updated = lecturerDao.findById(testLecturerId);
        assertEquals("Mathematics", updated.getDepartment());
    }

    // ==================== Course Tests ====================

    @Test
    @Order(5)
    void testCreateCourse() {
        Course course = new Course("COMP1322", "Programming", 15, testLecturerId, 30);
        testCourseId = courseDao.create(course);
        assertTrue(testCourseId > 0, "Course should be created successfully");
    }

    @Test
    @Order(6)
    void testFindCourseById() {
        Course course = courseDao.findById(testCourseId);
        assertNotNull(course);
        assertEquals("COMP1322", course.getCourseCode());
        assertEquals("Programming", course.getCourseName());
        assertEquals(15, course.getCredits());
        assertEquals(30, course.getMaxCapacity());
        assertNotNull(course.getLecturerName(), "Lecturer name should be populated from JOIN");
    }

    @Test
    @Order(7)
    void testFindAllCourses() {
        List<Course> courses = courseDao.findAll();
        assertFalse(courses.isEmpty());
        assertEquals(1, courses.size());
    }

    @Test
    @Order(8)
    void testGetEnrollmentCountEmpty() {
        int count = courseDao.getEnrollmentCount(testCourseId);
        assertEquals(0, count, "New course should have zero enrollments");
    }

    @Test
    @Order(9)
    void testUpdateCourse() {
        Course course = courseDao.findById(testCourseId);
        assertNotNull(course);
        course.setCourseName("Advanced Programming");
        course.setCredits(20);
        assertTrue(courseDao.update(course));

        Course updated = courseDao.findById(testCourseId);
        assertEquals("Advanced Programming", updated.getCourseName());
        assertEquals(20, updated.getCredits());
    }

    @Test
    @Order(10)
    void testDeleteCourse() {
        assertTrue(courseDao.delete(testCourseId));
        assertNull(courseDao.findById(testCourseId), "Deleted course should no longer exist");
    }

    @Test
    @Order(11)
    void testDeleteLecturer() {
        assertTrue(lecturerDao.delete(testLecturerId));
        assertNull(lecturerDao.findById(testLecturerId));
    }
}
