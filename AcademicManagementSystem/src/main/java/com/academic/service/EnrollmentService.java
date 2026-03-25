package com.academic.service;

import com.academic.dao.CourseDao;
import com.academic.dao.EnrollmentDao;
import com.academic.model.Course;
import com.academic.model.Enrollment;
import com.academic.model.Student;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for enrollment-related business rules.
 */
public class EnrollmentService {

    private final EnrollmentDao enrollmentDao;
    private final CourseDao courseDao;

    public EnrollmentService() {
        this.enrollmentDao = new EnrollmentDao();
        this.courseDao = new CourseDao();
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentDao.findAll();
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        return enrollmentDao.findByStudentId(studentId);
    }

    public List<Enrollment> getEnrollmentsByCourse(int courseId) {
        return enrollmentDao.findByCourseId(courseId);
    }

    public ServiceResult<Void> addEnrollment(Student student, Course course) {
        if (student == null || course == null) {
            return ServiceResult.failure("Please select both a student and a course.");
        }
        if (enrollmentDao.isEnrolled(student.getId(), course.getId())) {
            return ServiceResult.failure("This student is already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment(
            student.getId(), course.getId(), LocalDate.now(), Enrollment.Status.ENROLLED
        );
        if (enrollmentDao.create(enrollment) > 0) {
            return ServiceResult.success("Enrollment created successfully.");
        }
        return ServiceResult.failure("Failed to create enrollment.");
    }

    public ServiceResult<Void> studentEnroll(Student student, Course course) {
        if (course == null) {
            return ServiceResult.failure("Please select a course to enroll in.");
        }
        if (student == null) {
            return ServiceResult.failure("Student profile not found.");
        }
        if (enrollmentDao.isEnrolled(student.getId(), course.getId())) {
            return ServiceResult.failure("You are already enrolled in this course.");
        }

        int enrolled = courseDao.getEnrollmentCount(course.getId());
        if (enrolled >= course.getMaxCapacity()) {
            return ServiceResult.failure("This course has reached its maximum capacity.");
        }

        Enrollment enrollment = new Enrollment(
            student.getId(), course.getId(),
            LocalDate.now(), Enrollment.Status.ENROLLED
        );

        if (enrollmentDao.create(enrollment) > 0) {
            return ServiceResult.success("Successfully enrolled in " + course.getCourseCode() + ".");
        }
        return ServiceResult.failure("Failed to enroll. Please try again.");
    }

    public ServiceResult<Void> updateEnrollmentStatus(Enrollment enrollment, Enrollment.Status newStatus) {
        if (enrollment == null) {
            return ServiceResult.failure("Please select an enrollment to update.");
        }
        if (newStatus == null) {
            return ServiceResult.failure("Please select a status.");
        }

        if (enrollmentDao.updateStatus(enrollment.getId(), newStatus)) {
            return ServiceResult.success("Enrollment status updated.");
        }
        return ServiceResult.failure("Failed to update status.");
    }

    public ServiceResult<Void> withdraw(Enrollment enrollment) {
        if (enrollment == null) {
            return ServiceResult.failure("Please select an enrollment to withdraw from.");
        }
        if (enrollment.getStatus() != Enrollment.Status.ENROLLED) {
            return ServiceResult.failure("You can only withdraw from active enrollments.");
        }

        if (enrollmentDao.updateStatus(enrollment.getId(), Enrollment.Status.WITHDRAWN)) {
            return ServiceResult.success("Successfully withdrawn from the course.");
        }
        return ServiceResult.failure("Failed to withdraw. Please try again.");
    }

    public ServiceResult<Void> deleteEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            return ServiceResult.failure("Please select an enrollment to delete.");
        }
        enrollmentDao.delete(enrollment.getId());
        return ServiceResult.success("Enrollment deleted successfully.");
    }

    public ServiceResult<Void> deleteEnrollmentFromHistory(Enrollment enrollment) {
        if (enrollment == null) {
            return ServiceResult.failure("Please select an enrollment to delete.");
        }
        if (enrollment.getStatus() == Enrollment.Status.ENROLLED) {
            return ServiceResult.failure("You must withdraw from the course before deleting it.");
        }

        if (enrollmentDao.delete(enrollment.getId())) {
            return ServiceResult.success("Enrollment record deleted successfully.");
        }
        return ServiceResult.failure("Failed to delete enrollment. Please try again.");
    }
}
