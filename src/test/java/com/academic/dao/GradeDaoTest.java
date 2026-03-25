package com.academic.dao;

import com.academic.model.*;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GradeDao and EnrollmentDao using an in-memory SQLite database.
 * Tests grade creation, report queries, and date filtering.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GradeDaoTest {

    private static GradeDao gradeDao;
    private static EnrollmentDao enrollmentDao;
    private static CourseDao courseDao;
    private static int testEnrollmentId;
    private static int testGradeId;
    private static int testCourseId;
    private static int testStudentId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        gradeDao = new GradeDao();
        enrollmentDao = new EnrollmentDao();
        courseDao = new CourseDao();

        // Create all prerequisite data in the correct order
        UserDao userDao = new UserDao();
        StudentDao studentDao = new StudentDao();
        LecturerDao lecturerDao = new LecturerDao();

        int userId = userDao.create(new User("gradetest", "hash", User.Role.STUDENT));
        Student student = new Student(userId, "Grade", "Tester", "grade@test.com", "GT001", "CS");
        testStudentId = studentDao.create(student);

        int lecturerId = lecturerDao.create(new Lecturer("Dr", "Test", "dr@test.com", "CS"));
        testCourseId = courseDao.create(new Course("TEST101", "Test Course", 15, lecturerId, 30));

        Enrollment enrollment = new Enrollment(testStudentId, testCourseId,
            LocalDate.now(), Enrollment.Status.ENROLLED);
        testEnrollmentId = enrollmentDao.create(enrollment);
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    // ==================== Enrollment Tests ====================

    @Test
    @Order(1)
    void testEnrollmentCreated() {
        assertTrue(testEnrollmentId > 0, "Enrollment should be created successfully");
    }

    @Test
    @Order(2)
    void testIsEnrolled() {
        assertTrue(enrollmentDao.isEnrolled(testStudentId, testCourseId));
        assertFalse(enrollmentDao.isEnrolled(testStudentId, 9999));
    }

    @Test
    @Order(3)
    void testFindEnrollmentsByStudentId() {
        List<Enrollment> enrollments = enrollmentDao.findByStudentId(testStudentId);
        assertFalse(enrollments.isEmpty());
        assertEquals(testCourseId, enrollments.get(0).getCourseId());
    }

    @Test
    @Order(4)
    void testFindAllEnrollments() {
        List<Enrollment> enrollments = enrollmentDao.findAll();
        assertFalse(enrollments.isEmpty());
        assertNotNull(enrollments.get(0).getStudentName());
        assertNotNull(enrollments.get(0).getCourseCode());
    }

    @Test
    @Order(5)
    void testGetEnrollmentCount() {
        int count = courseDao.getEnrollmentCount(testCourseId);
        assertEquals(1, count);
    }

    // ==================== Grade Tests ====================

    @Test
    @Order(6)
    void testCreateGrade() {
        Grade grade = new Grade(testEnrollmentId, 75.0, "A", "Good work", LocalDate.now());
        testGradeId = gradeDao.create(grade);
        assertTrue(testGradeId > 0, "Grade should be created successfully");
    }

    @Test
    @Order(7)
    void testExistsForEnrollment() {
        assertTrue(gradeDao.existsForEnrollment(testEnrollmentId));
        assertFalse(gradeDao.existsForEnrollment(9999));
    }

    @Test
    @Order(8)
    void testFindAllGrades() {
        List<Grade> grades = gradeDao.findAll();
        assertFalse(grades.isEmpty());
        Grade grade = grades.get(0);
        assertEquals("A", grade.getGradeLetter());
        assertEquals(75.0, grade.getGradeValue());
        assertNotNull(grade.getStudentName());
        assertNotNull(grade.getCourseCode());
    }

    @Test
    @Order(9)
    void testFindGradesByStudentId() {
        List<Grade> grades = gradeDao.findByStudentId(testStudentId);
        assertFalse(grades.isEmpty());
        assertEquals("TEST101", grades.get(0).getCourseCode());
    }

    // ==================== Report Tests ====================

    @Test
    @Order(10)
    void testGetAverageGradeByCourse() {
        Map<String, Double> averages = gradeDao.getAverageGradeByCourse();
        assertFalse(averages.isEmpty(), "Should have at least one course average");
        // Verify the average is 75.0 for our test data
        for (Double avg : averages.values()) {
            assertEquals(75.0, avg, 0.01);
        }
    }

    @Test
    @Order(11)
    void testGetCourseGradeStats() {
        Map<String, Object> stats = gradeDao.getCourseGradeStats(testCourseId);
        assertFalse(stats.isEmpty());
        assertEquals(1, stats.get("total"));
        assertEquals(75.0, (Double) stats.get("average"), 0.01);
        assertEquals(75.0, (Double) stats.get("maximum"), 0.01);
        assertEquals(75.0, (Double) stats.get("minimum"), 0.01);
    }

    @Test
    @Order(12)
    void testGetGradeDistribution() {
        Map<String, Integer> distribution = gradeDao.getGradeDistribution(testCourseId);
        assertEquals(1, distribution.getOrDefault("A", 0), "Should have 1 A grade");
        assertEquals(0, distribution.getOrDefault("B", 0));
        assertEquals(0, distribution.getOrDefault("F", 0));
    }

    @Test
    @Order(13)
    void testFindByDateRange() {
        List<Grade> grades = gradeDao.findByDateRange(
            LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)
        );
        assertFalse(grades.isEmpty(), "Should find grades within the date range");
    }

    @Test
    @Order(14)
    void testFindByDateRangeEmpty() {
        List<Grade> grades = gradeDao.findByDateRange(
            LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)
        );
        assertTrue(grades.isEmpty(), "Should find no grades in a past date range");
    }

    @Test
    @Order(15)
    void testGetEnrollmentStatusCounts() {
        Map<String, Integer> counts = gradeDao.getEnrollmentStatusCounts();
        assertFalse(counts.isEmpty());
        assertTrue(counts.containsKey("ENROLLED"));
        assertTrue(counts.get("ENROLLED") >= 1);
    }

    // ==================== Update and Delete ====================

    @Test
    @Order(16)
    void testUpdateGrade() {
        List<Grade> grades = gradeDao.findAll();
        assertFalse(grades.isEmpty());
        Grade grade = grades.get(0);
        grade.setGradeValue(85.0);
        grade.setGradeLetter(Grade.calculateGradeLetter(85.0));
        grade.setFeedback("Excellent work");
        grade.setGradedDate(LocalDate.now());
        assertTrue(gradeDao.update(grade));

        // Verify the update
        List<Grade> updated = gradeDao.findAll();
        assertEquals(85.0, updated.get(0).getGradeValue());
        assertEquals("A", updated.get(0).getGradeLetter());
    }

    @Test
    @Order(17)
    void testUpdateEnrollmentStatus() {
        assertTrue(enrollmentDao.updateStatus(testEnrollmentId, Enrollment.Status.COMPLETED));
        Enrollment enrollment = enrollmentDao.findById(testEnrollmentId);
        assertNotNull(enrollment);
        assertEquals(Enrollment.Status.COMPLETED, enrollment.getStatus());
    }

    @Test
    @Order(18)
    void testDeleteGrade() {
        assertTrue(gradeDao.delete(testGradeId));
        assertFalse(gradeDao.existsForEnrollment(testEnrollmentId),
            "Grade should no longer exist after deletion");
    }

    @Test
    @Order(19)
    void testDeleteEnrollment() {
        assertTrue(enrollmentDao.delete(testEnrollmentId));
        assertNull(enrollmentDao.findById(testEnrollmentId));
    }
}
