package com.academic.service;

import com.academic.dao.DatabaseManager;
import com.academic.dao.StudentDao;
import com.academic.dao.UserDao;
import com.academic.model.Student;
import com.academic.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service-layer tests for student account workflows and transaction safety.
 */
class StudentServiceTest {

    private static StudentService studentService;
    private static UserDao userDao;
    private static StudentDao studentDao;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        studentService = new StudentService();
        userDao = new UserDao();
        studentDao = new StudentDao();
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    @Test
    void testRegisterStudentAccountSuccess() {
        ServiceResult<Integer> result = studentService.registerStudentAccount(
            "serviceuser1", "password123", "Jane", "Doe", "jane@test.com", "SD1001", "CS"
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());

        User createdUser = userDao.findByUsername("serviceuser1");
        assertNotNull(createdUser);
        Student createdStudent = studentDao.findByUserId(createdUser.getId());
        assertNotNull(createdStudent);
        assertEquals("SD1001", createdStudent.getStudentNumber());
    }

    @Test
    void testRegisterStudentAccountDuplicateUsernameFails() {
        ServiceResult<Integer> first = studentService.registerStudentAccount(
            "serviceuser_dup", "password123", "Janet", "Doe", "janet1@test.com", "SD1901", "CS"
        );
        assertTrue(first.isSuccess());

        ServiceResult<Integer> result = studentService.registerStudentAccount(
            "serviceuser_dup", "password123", "Janet", "Doe", "janet2@test.com", "SD1902", "CS"
        );

        assertFalse(result.isSuccess());
        assertEquals("Username already taken.", result.getMessage());
    }

    @Test
    void testRegisterStudentAccountRollbackOnStudentInsertFailure() {
        ServiceResult<Integer> first = studentService.registerStudentAccount(
            "serviceuser2", "password123", "Alpha", "One", "alpha@test.com", "SD2001", "Math"
        );
        assertTrue(first.isSuccess());

        ServiceResult<Integer> second = studentService.registerStudentAccount(
            "serviceuser3", "password123", "Beta", "Two", "beta@test.com", "SD2001", "Math"
        );

        assertFalse(second.isSuccess());
        assertEquals("Failed to create student. Student number may already exist.", second.getMessage());

        // Transaction should roll back the user created in the failed attempt.
        User rolledBackUser = userDao.findByUsername("serviceuser3");
        assertNull(rolledBackUser);
    }

    @Test
    void testDeleteStudentDeletesStudentAndUser() {
        ServiceResult<Integer> createResult = studentService.registerStudentAccount(
            "serviceuser4", "password123", "Delete", "Me", "deleteme@test.com", "SD3001", "CS"
        );
        assertTrue(createResult.isSuccess());

        User user = userDao.findByUsername("serviceuser4");
        assertNotNull(user);
        Student student = studentDao.findByUserId(user.getId());
        assertNotNull(student);

        ServiceResult<Void> deleteResult = studentService.deleteStudent(student);
        assertTrue(deleteResult.isSuccess());

        assertNull(studentDao.findById(student.getId()));
        assertNull(userDao.findById(user.getId()));
    }
}
