package com.academic.controller;

import com.academic.dao.GradeDao;
import com.academic.model.*;
import com.academic.service.CourseService;
import com.academic.service.EnrollmentService;
import com.academic.service.ServiceResult;
import com.academic.service.StudentService;
import com.academic.util.SessionManager;
import java.util.List;

/**
 * Controller for student dashboard operations.
 * Handles enrollment, withdrawal, profile updates, and data retrieval
 * for the currently logged-in student.
 */
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final GradeDao gradeDao;

    public StudentController() {
        this.studentService = new StudentService();
        this.courseService = new CourseService();
        this.enrollmentService = new EnrollmentService();
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
        if (!SessionManager.isStudent()) {
            return null;
        }
        return SessionManager.getCurrentStudent();
    }

    /** Retrieves all enrollments for the current student. */
    public List<Enrollment> getMyEnrollments(int studentId) {
        if (!SessionManager.isStudent()) {
            return List.of();
        }
        return enrollmentService.getEnrollmentsByStudent(studentId);
    }

    /** Retrieves all available courses. */
    public List<Course> getAllCourses() {
        if (!SessionManager.isStudent()) {
            return List.of();
        }
        return courseService.getAllCourses();
    }

    /** Retrieves all grades for the current student. */
    public List<Grade> getMyGrades(int studentId) {
        if (!SessionManager.isStudent()) {
            return List.of();
        }
        return gradeDao.findByStudentId(studentId);
    }

    /**
     * Enrolls the current student in a course, with capacity and duplicate checks.
     */
    public OperationResult enroll(Student student, Course course) {
        OperationResult auth = requireStudent();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = enrollmentService.studentEnroll(student, course);
        return toOperationResult(result);
    }

    /**
     * Withdraws the student from an enrollment.
     */
    public OperationResult withdraw(Enrollment enrollment) {
        OperationResult auth = requireStudent();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = enrollmentService.withdraw(enrollment);
        return toOperationResult(result);
    }

    /**
     * Deletes a withdrawn enrollment from the student's history.
     */
    public OperationResult deleteEnrollment(Enrollment enrollment) {
        OperationResult auth = requireStudent();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = enrollmentService.deleteEnrollmentFromHistory(enrollment);
        return toOperationResult(result);
    }

    /**
     * Updates the student's profile information.
     */
    public OperationResult updateProfile(Student student, String firstName, String lastName,
                                         String email, String programme) {
        OperationResult auth = requireStudent();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = studentService.updateProfile(
            student, firstName, lastName, email, programme
        );
        return toOperationResult(result);
    }

    private OperationResult toOperationResult(ServiceResult<?> serviceResult) {
        if (serviceResult.isSuccess()) {
            return OperationResult.success(serviceResult.getMessage());
        }
        return OperationResult.failure(serviceResult.getMessage());
    }

    private OperationResult requireStudent() {
        if (!SessionManager.isStudent()) {
            return OperationResult.failure("Access denied. Student role is required.");
        }
        return null;
    }

    /** Logs out the current user. */
    public void logout() {
        SessionManager.logout();
    }
}
