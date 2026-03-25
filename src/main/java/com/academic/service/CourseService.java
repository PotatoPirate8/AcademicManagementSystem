package com.academic.service;

import com.academic.dao.CourseDao;
import com.academic.model.Course;
import com.academic.model.Lecturer;
import com.academic.util.ValidationUtil;

import java.util.List;

/**
 * Service layer for course-related business rules.
 */
public class CourseService {

    private final CourseDao courseDao;

    public CourseService() {
        this.courseDao = new CourseDao();
    }

    public List<Course> getAllCourses() {
        return courseDao.findAll();
    }

    public int getEnrollmentCount(int courseId) {
        return courseDao.getEnrollmentCount(courseId);
    }

    public ServiceResult<Void> addCourse(String code, String name, String creditsStr,
                                         String capacityStr, Lecturer lecturer) {
        String validationError = validateCourseFields(code, name, creditsStr, capacityStr, lecturer);
        if (validationError != null) {
            return ServiceResult.failure(validationError);
        }

        Course course = new Course(
            code.trim().toUpperCase(), name.trim(),
            ValidationUtil.parseIntSafe(creditsStr),
            lecturer.getId(),
            ValidationUtil.parseIntSafe(capacityStr)
        );
        if (courseDao.create(course) > 0) {
            return ServiceResult.success("Course added successfully.");
        }
        return ServiceResult.failure("Failed to add course. Code may already exist.");
    }

    public ServiceResult<Void> updateCourse(Course course, String code, String name,
                                            String creditsStr, String capacityStr, Lecturer lecturer) {
        if (course == null) {
            return ServiceResult.failure("Please select a course to update.");
        }
        String validationError = validateCourseFields(code, name, creditsStr, capacityStr, lecturer);
        if (validationError != null) {
            return ServiceResult.failure(validationError);
        }

        course.setCourseCode(code.trim().toUpperCase());
        course.setCourseName(name.trim());
        course.setCredits(ValidationUtil.parseIntSafe(creditsStr));
        course.setMaxCapacity(ValidationUtil.parseIntSafe(capacityStr));
        course.setLecturerId(lecturer.getId());

        if (courseDao.update(course)) {
            return ServiceResult.success("Course updated successfully.");
        }
        return ServiceResult.failure("Failed to update course.");
    }

    public ServiceResult<Void> deleteCourse(Course course) {
        if (course == null) {
            return ServiceResult.failure("Please select a course to delete.");
        }
        courseDao.delete(course.getId());
        return ServiceResult.success("Course deleted successfully.");
    }

    private String validateCourseFields(String code, String name, String creditsStr,
                                        String capacityStr, Lecturer lecturer) {
        if (!ValidationUtil.isValidCourseCode(code.trim().toUpperCase())) {
            return "Course code must be 2-5 letters followed by 3-5 digits (e.g., COMP1322).";
        }
        if (ValidationUtil.isNullOrEmpty(name)) {
            return "Course name cannot be empty.";
        }
        int credits = ValidationUtil.parseIntSafe(creditsStr);
        if (!ValidationUtil.isPositiveInteger(credits)) {
            return "Credits must be a positive number.";
        }
        int capacity = ValidationUtil.parseIntSafe(capacityStr);
        if (!ValidationUtil.isPositiveInteger(capacity)) {
            return "Capacity must be a positive number.";
        }
        if (lecturer == null) {
            return "Please select a lecturer.";
        }
        return null;
    }
}
