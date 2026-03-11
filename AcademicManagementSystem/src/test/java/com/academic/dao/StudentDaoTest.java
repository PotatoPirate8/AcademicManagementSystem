package com.academic.dao;

import com.academic.model.Student;
import com.academic.model.User;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for StudentDao using an in-memory SQLite database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentDaoTest {

    private static StudentDao studentDao;
    private static UserDao userDao;
    private static int testUserId;
    private static int testStudentId;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        studentDao = new StudentDao();
        userDao = new UserDao();

        // Create a user account for the test student
        User user = new User("studenttest", "hash", User.Role.STUDENT);
        testUserId = userDao.create(user);
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    @Test
    @Order(1)
    void testCreateStudent() {
        Student student = new Student(testUserId, "John", "Doe", "john@test.com", "SN1234", "Computer Science");
        testStudentId = studentDao.create(student);
        assertTrue(testStudentId > 0, "Created student should have a positive ID");
    }

    @Test
    @Order(2)
    void testFindByUserId() {
        Student student = studentDao.findByUserId(testUserId);
        assertNotNull(student);
        assertEquals("John", student.getFirstName());
        assertEquals("Doe", student.getLastName());
        assertEquals("SN1234", student.getStudentNumber());
    }

    @Test
    @Order(3)
    void testFindById() {
        Student student = studentDao.findById(testStudentId);
        assertNotNull(student);
        assertEquals("john@test.com", student.getEmail());
        assertEquals("Computer Science", student.getProgramme());
    }

    @Test
    @Order(4)
    void testFindByIdNonexistent() {
        Student student = studentDao.findById(99999);
        assertNull(student, "Finding non-existent student should return null");
    }

    @Test
    @Order(5)
    void testFindAll() {
        List<Student> students = studentDao.findAll();
        assertFalse(students.isEmpty());
        assertEquals(1, students.size());
    }

    @Test
    @Order(6)
    void testUpdateStudent() {
        Student student = studentDao.findById(testStudentId);
        assertNotNull(student);

        student.setEmail("newemail@test.com");
        student.setProgramme("Mathematics");
        assertTrue(studentDao.update(student));

        Student updated = studentDao.findById(testStudentId);
        assertEquals("newemail@test.com", updated.getEmail());
        assertEquals("Mathematics", updated.getProgramme());
    }

    @Test
    @Order(7)
    void testStudentFullName() {
        Student student = studentDao.findById(testStudentId);
        assertNotNull(student);
        assertEquals("John Doe", student.getFullName());
    }

    @Test
    @Order(8)
    void testDeleteStudent() {
        assertTrue(studentDao.delete(testStudentId));
        assertNull(studentDao.findById(testStudentId), "Deleted student should no longer exist");
    }
}
