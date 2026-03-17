package com.academic.controller;

import com.academic.dao.CourseDao;
import com.academic.dao.EnrollmentDao;
import com.academic.dao.GradeDao;
import com.academic.dao.StudentDao;
import com.academic.model.*;
import com.academic.util.SessionManager;
import com.academic.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for student dashboard operations.
 * Handles enrollment, withdrawal, profile updates, and data retrieval
 * for the currently logged-in student.
 */
public class StudentController {

    private final StudentDao studentDao;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;

    public StudentController() {
        this.studentDao = new StudentDao();
        this.courseDao = new CourseDao();
        this.enrollmentDao = new EnrollmentDao();
        this.gradeDao = new GradeDao();
    }

    /**
     * Generic result object for operations that can succeed or fail.
     */
    public static class OperationResult {
        private final boolean success;
        private final String message;

        private OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static OperationResult success(String message) {
            return new OperationResult(true, message);
        }

        public static OperationResult failure(String message) {
            return new OperationResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    /** Returns the currently logged-in student. */
    public Student getCurrentStudent() {
        return SessionManager.getCurrentStudent();
    }

    /** Retrieves all enrollments for the current student. */
    public List<Enrollment> getMyEnrollments(int studentId) {
        return enrollmentDao.findByStudentId(studentId);
    }

    /** Retrieves all available courses. */
    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    /** Retrieves all grades for the current student. */
    public List<Grade> getMyGrades(int studentId) {
        return gradeDao.findByStudentId(studentId);
    }

    /**
     * Enrolls the current student in a course, with capacity and duplicate checks.
     */
    public OperationResult enroll(Student student, Course course) {
        if (course == null) {
            return OperationResult.failure("Please select a course to enroll in.");
        }
        if (student == null) {
            return OperationResult.failure("Student profile not found.");
        }

        // Check if already enrolled
        if (enrollmentDao.isEnrolled(student.getId(), course.getId())) {
            return OperationResult.failure("You are already enrolled in this course.");
        }

        // Check course capacity
        int enrolled = courseDao.getEnrollmentCount(course.getId());
        if (enrolled >= course.getMaxCapacity()) {
            return OperationResult.failure("This course has reached its maximum capacity.");
        }

        Enrollment enrollment = new Enrollment(
            student.getId(), course.getId(),
            LocalDate.now(), Enrollment.Status.ENROLLED
        );

        if (enrollmentDao.create(enrollment) > 0) {
            return OperationResult.success("Successfully enrolled in " + course.getCourseCode() + ".");
        }
        return OperationResult.failure("Failed to enroll. Please try again.");
    }

    /**
     * Withdraws the student from an enrollment.
     */
    public OperationResult withdraw(Enrollment enrollment) {
        if (enrollment == null) {
            return OperationResult.failure("Please select an enrollment to withdraw from.");
        }
        if (enrollment.getStatus() != Enrollment.Status.ENROLLED) {
            return OperationResult.failure("You can only withdraw from active enrollments.");
        }

        if (enrollmentDao.updateStatus(enrollment.getId(), Enrollment.Status.WITHDRAWN)) {
            return OperationResult.success("Successfully withdrawn from the course.");
        }
        return OperationResult.failure("Failed to withdraw. Please try again.");
    }

    /**
     * Deletes a withdrawn enrollment from the student's history.
     */
    public OperationResult deleteEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            return OperationResult.failure("Please select an enrollment to delete.");
        }
        if (enrollment.getStatus() == Enrollment.Status.ENROLLED) {
            return OperationResult.failure("You must withdraw from the course before deleting it.");
        }

        if (enrollmentDao.delete(enrollment.getId())) {
            return OperationResult.success("Enrollment record deleted successfully.");
        }
        return OperationResult.failure("Failed to delete enrollment. Please try again.");
    }

    /**
     * Updates the student's profile information.
     */
    public OperationResult updateProfile(Student student, String firstName, String lastName,
                                         String email, String programme) {
        if (student == null) {
            return OperationResult.failure("Student profile not found.");
        }
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return OperationResult.failure("Name cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return OperationResult.failure("Please enter a valid email.");
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            return OperationResult.failure("Programme cannot be empty.");
        }

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setProgramme(programme);

        if (studentDao.update(student)) {
            return OperationResult.success("Profile updated successfully.");
        }
        return OperationResult.failure("Failed to update profile.");
    }

    /** Logs out the current user. */
    public void logout() {
        SessionManager.logout();
    }
}
