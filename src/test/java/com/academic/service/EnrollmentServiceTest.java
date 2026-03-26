package com.academic.service;

import com.academic.dao.CourseDao;
import com.academic.dao.DatabaseManager;
import com.academic.dao.EnrollmentDao;
import com.academic.dao.LecturerDao;
import com.academic.dao.StudentDao;
import com.academic.dao.UserDao;
import com.academic.model.Course;
import com.academic.model.Enrollment;
import com.academic.model.Lecturer;
import com.academic.model.Student;
import com.academic.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service-layer tests for enrollment workflow edge cases.
 */
class EnrollmentServiceTest {

    private static EnrollmentService enrollmentService;
    private static CourseDao courseDao;
    private static EnrollmentDao enrollmentDao;
    private static Student studentOne;
    private static Student studentTwo;
    private static int courseCounter;

    @BeforeAll
    static void setUp() {
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        enrollmentService = new EnrollmentService();
        courseDao = new CourseDao();
        enrollmentDao = new EnrollmentDao();

        UserDao userDao = new UserDao();
        StudentDao studentDao = new StudentDao();
        LecturerDao lecturerDao = new LecturerDao();

        int userOneId = userDao.create(new User("enroll1", "hash", User.Role.STUDENT));
        int userTwoId = userDao.create(new User("enroll2", "hash", User.Role.STUDENT));
        int studentOneId = studentDao.create(new Student(userOneId, "Stu", "One", "one@test.com", "EN1001", "CS"));
        int studentTwoId = studentDao.create(new Student(userTwoId, "Stu", "Two", "two@test.com", "EN1002", "CS"));
        studentOne = studentDao.findById(studentOneId);
        studentTwo = studentDao.findById(studentTwoId);

        int lecturerId = lecturerDao.create(new Lecturer("Edge", "Case", "edge@test.com", "CS"));
        courseCounter = 0;
        storedLecturerId = lecturerId;
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    @Test
    void testStudentEnrollSuccessThenDuplicateFails() {
        Course tightCapacityCourse = createSingleCapacityCourse();

        ServiceResult<Void> first = enrollmentService.studentEnroll(studentOne, tightCapacityCourse);
        assertTrue(first.isSuccess());

        ServiceResult<Void> duplicate = enrollmentService.studentEnroll(studentOne, tightCapacityCourse);
        assertFalse(duplicate.isSuccess());
        assertEquals("You are already enrolled in this course.", duplicate.getMessage());
    }

    @Test
    void testStudentEnrollCapacityReached() {
        Course tightCapacityCourse = createSingleCapacityCourse();

        ServiceResult<Void> occupySeat = enrollmentService.studentEnroll(studentOne, tightCapacityCourse);
        assertTrue(occupySeat.isSuccess());

        ServiceResult<Void> result = enrollmentService.studentEnroll(studentTwo, tightCapacityCourse);
        assertFalse(result.isSuccess());
        assertEquals("This course has reached its maximum capacity.", result.getMessage());
    }

    @Test
    void testWithdrawAndDeleteFromHistoryWorkflow() {
        Course tightCapacityCourse = createSingleCapacityCourse();

        ServiceResult<Void> first = enrollmentService.studentEnroll(studentOne, tightCapacityCourse);
        assertTrue(first.isSuccess());

        Enrollment enrollment = enrollmentDao.findByStudentId(studentOne.getId()).stream()
            .filter(e -> e.getCourseId() == tightCapacityCourse.getId())
            .findFirst()
            .orElse(null);
        assertNotNull(enrollment);

        ServiceResult<Void> cannotDeleteActive = enrollmentService.deleteEnrollmentFromHistory(enrollment);
        assertFalse(cannotDeleteActive.isSuccess());
        assertEquals("You must withdraw from the course before deleting it.", cannotDeleteActive.getMessage());

        ServiceResult<Void> withdraw = enrollmentService.withdraw(enrollment);
        assertTrue(withdraw.isSuccess());

        Enrollment withdrawn = enrollmentDao.findById(enrollment.getId());
        assertNotNull(withdrawn);
        assertEquals(Enrollment.Status.WITHDRAWN, withdrawn.getStatus());

        ServiceResult<Void> delete = enrollmentService.deleteEnrollmentFromHistory(withdrawn);
        assertTrue(delete.isSuccess());
    }

    private static int storedLecturerId;

    private Course createSingleCapacityCourse() {
        courseCounter++;
        String code = String.format("COMP%04d", 8100 + courseCounter);
        Course course = new Course(code, "Capacity Test " + courseCounter, 15, storedLecturerId, 1);
        int courseId = courseDao.create(course);
        return courseDao.findById(courseId);
    }
}
