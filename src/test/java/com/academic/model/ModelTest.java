package com.academic.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all model classes.
 */
class ModelTest {

    // ==================== User Tests ====================

    @Test
    void testUserCreation() {
        User user = new User("testuser", "hash123", User.Role.STUDENT);
        assertEquals("testuser", user.getUsername());
        assertEquals("hash123", user.getPasswordHash());
        assertEquals(User.Role.STUDENT, user.getRole());
    }

    @Test
    void testUserCreationWithId() {
        User user = new User(1, "admin", "hash", User.Role.ADMIN);
        assertEquals(1, user.getId());
        assertEquals("admin", user.getUsername());
        assertEquals(User.Role.ADMIN, user.getRole());
    }

    @Test
    void testUserToString() {
        User user = new User(1, "admin", "hash", User.Role.ADMIN);
        assertEquals("admin (ADMIN)", user.toString());
    }

    @Test
    void testUserSetters() {
        User user = new User();
        user.setId(5);
        user.setUsername("newuser");
        user.setPasswordHash("newhash");
        user.setRole(User.Role.STUDENT);

        assertEquals(5, user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("newhash", user.getPasswordHash());
        assertEquals(User.Role.STUDENT, user.getRole());
    }

    // ==================== Student Tests ====================

    @Test
    void testStudentFullName() {
        Student student = new Student(1, "John", "Doe", "john@test.com", "SN123", "CS");
        assertEquals("John Doe", student.getFullName());
    }

    @Test
    void testStudentToString() {
        Student student = new Student(1, "John", "Doe", "john@test.com", "SN123", "CS");
        assertEquals("SN123 - John Doe", student.toString());
    }

    @Test
    void testStudentSetters() {
        Student student = new Student();
        student.setId(1);
        student.setUserId(10);
        student.setFirstName("Alice");
        student.setLastName("Brown");
        student.setEmail("alice@test.com");
        student.setStudentNumber("SN999");
        student.setProgramme("Mathematics");

        assertEquals(1, student.getId());
        assertEquals(10, student.getUserId());
        assertEquals("Alice", student.getFirstName());
        assertEquals("Brown", student.getLastName());
        assertEquals("alice@test.com", student.getEmail());
        assertEquals("SN999", student.getStudentNumber());
        assertEquals("Mathematics", student.getProgramme());
    }

    // ==================== Lecturer Tests ====================

    @Test
    void testLecturerFullName() {
        Lecturer lecturer = new Lecturer("Jane", "Smith", "jane@uni.com", "CS");
        assertEquals("Jane Smith", lecturer.getFullName());
    }

    @Test
    void testLecturerToString() {
        Lecturer lecturer = new Lecturer("Jane", "Smith", "jane@uni.com", "CS");
        assertEquals("Jane Smith (CS)", lecturer.toString());
    }

    @Test
    void testLecturerSetters() {
        Lecturer lecturer = new Lecturer();
        lecturer.setId(1);
        lecturer.setFirstName("Bob");
        lecturer.setLastName("Wilson");
        lecturer.setEmail("bob@uni.com");
        lecturer.setDepartment("Physics");

        assertEquals(1, lecturer.getId());
        assertEquals("Bob", lecturer.getFirstName());
        assertEquals("Wilson", lecturer.getLastName());
        assertEquals("bob@uni.com", lecturer.getEmail());
        assertEquals("Physics", lecturer.getDepartment());
    }

    // ==================== Course Tests ====================

    @Test
    void testCourseCreation() {
        Course course = new Course("COMP1322", "Programming", 15, 1, 30);
        assertEquals("COMP1322", course.getCourseCode());
        assertEquals("Programming", course.getCourseName());
        assertEquals(15, course.getCredits());
        assertEquals(1, course.getLecturerId());
        assertEquals(30, course.getMaxCapacity());
    }

    @Test
    void testCourseToString() {
        Course course = new Course("COMP1322", "Programming", 15, 1, 30);
        assertEquals("COMP1322 - Programming", course.toString());
    }

    @Test
    void testCourseSetters() {
        Course course = new Course();
        course.setId(1);
        course.setCourseCode("MATH101");
        course.setCourseName("Calculus");
        course.setCredits(15);
        course.setLecturerId(2);
        course.setMaxCapacity(50);
        course.setLecturerName("Dr Smith");

        assertEquals(1, course.getId());
        assertEquals("MATH101", course.getCourseCode());
        assertEquals("Calculus", course.getCourseName());
        assertEquals(15, course.getCredits());
        assertEquals(2, course.getLecturerId());
        assertEquals(50, course.getMaxCapacity());
        assertEquals("Dr Smith", course.getLecturerName());
    }

    // ==================== Enrollment Tests ====================

    @Test
    void testEnrollmentCreation() {
        LocalDate now = LocalDate.now();
        Enrollment enrollment = new Enrollment(1, 2, now, Enrollment.Status.ENROLLED);
        assertEquals(1, enrollment.getStudentId());
        assertEquals(2, enrollment.getCourseId());
        assertEquals(now, enrollment.getEnrollmentDate());
        assertEquals(Enrollment.Status.ENROLLED, enrollment.getStatus());
    }

    @Test
    void testEnrollmentStatusChange() {
        Enrollment enrollment = new Enrollment(1, 1, LocalDate.now(), Enrollment.Status.ENROLLED);
        assertEquals(Enrollment.Status.ENROLLED, enrollment.getStatus());

        enrollment.setStatus(Enrollment.Status.WITHDRAWN);
        assertEquals(Enrollment.Status.WITHDRAWN, enrollment.getStatus());

        enrollment.setStatus(Enrollment.Status.COMPLETED);
        assertEquals(Enrollment.Status.COMPLETED, enrollment.getStatus());
    }

    @Test
    void testEnrollmentDisplayFields() {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentName("John Doe");
        enrollment.setCourseCode("COMP101");
        enrollment.setCourseName("Intro to CS");

        assertEquals("John Doe", enrollment.getStudentName());
        assertEquals("COMP101", enrollment.getCourseCode());
        assertEquals("Intro to CS", enrollment.getCourseName());
    }

    // ==================== Grade Tests ====================

    @Test
    void testGradeLetterCalculation() {
        assertEquals("A", Grade.calculateGradeLetter(85));
        assertEquals("A", Grade.calculateGradeLetter(70));
        assertEquals("B", Grade.calculateGradeLetter(65));
        assertEquals("B", Grade.calculateGradeLetter(60));
        assertEquals("C", Grade.calculateGradeLetter(55));
        assertEquals("C", Grade.calculateGradeLetter(50));
        assertEquals("D", Grade.calculateGradeLetter(45));
        assertEquals("D", Grade.calculateGradeLetter(40));
        assertEquals("F", Grade.calculateGradeLetter(35));
        assertEquals("F", Grade.calculateGradeLetter(0));
    }

    @Test
    void testGradeLetterBoundaryValues() {
        assertEquals("A", Grade.calculateGradeLetter(100));
        assertEquals("A", Grade.calculateGradeLetter(70));
        assertEquals("B", Grade.calculateGradeLetter(69.9));
        assertEquals("C", Grade.calculateGradeLetter(59.9));
        assertEquals("D", Grade.calculateGradeLetter(49.9));
        assertEquals("F", Grade.calculateGradeLetter(39.9));
        assertEquals("F", Grade.calculateGradeLetter(0));
    }

    @Test
    void testGradeCreation() {
        LocalDate now = LocalDate.now();
        Grade grade = new Grade(1, 75.0, "A", "Good work", now);
        assertEquals(1, grade.getEnrollmentId());
        assertEquals(75.0, grade.getGradeValue());
        assertEquals("A", grade.getGradeLetter());
        assertEquals("Good work", grade.getFeedback());
        assertEquals(now, grade.getGradedDate());
    }

    @Test
    void testGradeToString() {
        Grade grade = new Grade(1, 85.0, "A", "Excellent", LocalDate.now());
        assertEquals("A (85.0)", grade.toString());
    }

    @Test
    void testGradeDisplayFields() {
        Grade grade = new Grade();
        grade.setStudentName("Alice Brown");
        grade.setCourseCode("COMP201");
        grade.setCourseName("Data Structures");

        assertEquals("Alice Brown", grade.getStudentName());
        assertEquals("COMP201", grade.getCourseCode());
        assertEquals("Data Structures", grade.getCourseName());
    }
}
