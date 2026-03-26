package com.academic.controller;

import com.academic.dao.*;
import com.academic.model.*;
import com.academic.service.CourseService;
import com.academic.service.EnrollmentService;
import com.academic.service.ServiceResult;
import com.academic.service.StudentService;
import com.academic.util.SessionManager;
import com.academic.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for admin dashboard operations.
 * Handles CRUD for students, lecturers, courses, enrollments, grades,
 * and all report generation logic.
 */
public class AdminController {

    private final LecturerDao lecturerDao;
    private final GradeDao gradeDao;
    private final StudentService studentService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public AdminController() {
        this.lecturerDao = new LecturerDao();
        this.gradeDao = new GradeDao();
        this.studentService = new StudentService();
        this.courseService = new CourseService();
        this.enrollmentService = new EnrollmentService();
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

    // ==================== Student CRUD ====================

    public List<Student> getAllStudents() {
        if (!SessionManager.isAdmin()) {
            return List.of();
        }
        return studentService.getAllStudents();
    }

    public OperationResult updateStudent(Student student, String firstName, String lastName,
                                         String email, String studentNumber, String programme) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = studentService.updateStudent(
            student, firstName, lastName, email, studentNumber, programme
        );
        return toOperationResult(result);
    }

    public OperationResult addStudent(String username, String password, String firstName,
                                       String lastName, String email, String studentNumber, String programme) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Integer> result = studentService.registerStudentAccount(
            username, password, firstName, lastName, email, studentNumber, programme
        );
        return toOperationResult(result);
    }

    public OperationResult deleteStudent(Student student) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = studentService.deleteStudent(student);
        return toOperationResult(result);
    }

    // ==================== Lecturer CRUD ====================

    public List<Lecturer> getAllLecturers() {
        if (!SessionManager.isAdmin()) {
            return List.of();
        }
        return lecturerDao.findAll();
    }

    public OperationResult addLecturer(String firstName, String lastName, String email, String department) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        String validationError = validateLecturerFields(firstName, lastName, email, department);
        if (validationError != null) {
            return OperationResult.failure(validationError);
        }

        Lecturer lecturer = new Lecturer(
            firstName.trim(), lastName.trim(), email.trim(), department.trim()
        );
        if (lecturerDao.create(lecturer) > 0) {
            return OperationResult.success("Lecturer added successfully.");
        }
        return OperationResult.failure("Failed to add lecturer. Email may already exist.");
    }

    public OperationResult updateLecturer(Lecturer lecturer, String firstName, String lastName,
                                          String email, String department) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        if (lecturer == null) {
            return OperationResult.failure("Please select a lecturer to update.");
        }
        String validationError = validateLecturerFields(firstName, lastName, email, department);
        if (validationError != null) {
            return OperationResult.failure(validationError);
        }

        lecturer.setFirstName(firstName.trim());
        lecturer.setLastName(lastName.trim());
        lecturer.setEmail(email.trim());
        lecturer.setDepartment(department.trim());

        if (lecturerDao.update(lecturer)) {
            return OperationResult.success("Lecturer updated successfully.");
        }
        return OperationResult.failure("Failed to update lecturer.");
    }

    public OperationResult deleteLecturer(Lecturer lecturer) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        if (lecturer == null) {
            return OperationResult.failure("Please select a lecturer to delete.");
        }
        lecturerDao.delete(lecturer.getId());
        return OperationResult.success("Lecturer deleted successfully.");
    }

    private String validateLecturerFields(String firstName, String lastName, String email, String department) {
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return "Name fields cannot be empty.";
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return "Please enter a valid email address.";
        }
        if (ValidationUtil.isNullOrEmpty(department)) {
            return "Department cannot be empty.";
        }
        return null;
    }

    // ==================== Course CRUD ====================

    public List<Course> getAllCourses() {
        if (!SessionManager.isAdmin()) {
            return List.of();
        }
        return courseService.getAllCourses();
    }

    public OperationResult addCourse(String code, String name, String creditsStr,
                                     String capacityStr, Lecturer lecturer) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = courseService.addCourse(code, name, creditsStr, capacityStr, lecturer);
        return toOperationResult(result);
    }

    public OperationResult updateCourse(Course course, String code, String name,
                                        String creditsStr, String capacityStr, Lecturer lecturer) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = courseService.updateCourse(
            course, code, name, creditsStr, capacityStr, lecturer
        );
        return toOperationResult(result);
    }

    public OperationResult deleteCourse(Course course) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = courseService.deleteCourse(course);
        return toOperationResult(result);
    }

    // ==================== Enrollment CRUD ====================

    public List<Enrollment> getAllEnrollments() {
        if (!SessionManager.isAdmin()) {
            return List.of();
        }
        return enrollmentService.getAllEnrollments();
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        if (!SessionManager.isAdmin()) {
            return List.of();
        }
        return enrollmentService.getEnrollmentsByStudent(studentId);
    }

    public List<Enrollment> getEnrollmentsByCourse(int courseId) {
        if (!SessionManager.isAdmin()) {
            return List.of();
        }
        return enrollmentService.getEnrollmentsByCourse(courseId);
    }

    public OperationResult addEnrollment(Student student, Course course) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = enrollmentService.addEnrollment(student, course);
        return toOperationResult(result);
    }

    public OperationResult updateEnrollmentStatus(Enrollment enrollment, Enrollment.Status newStatus) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = enrollmentService.updateEnrollmentStatus(enrollment, newStatus);
        return toOperationResult(result);
    }

    public OperationResult deleteEnrollment(Enrollment enrollment) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        ServiceResult<Void> result = enrollmentService.deleteEnrollment(enrollment);
        return toOperationResult(result);
    }

    // ==================== Grade CRUD ====================

    public List<Grade> getAllGrades() {
        if (!SessionManager.isAdmin()) {
            return List.of();
        }
        return gradeDao.findAll();
    }

    public OperationResult addGrade(Enrollment enrollment, String gradeStr, String feedback) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        if (enrollment == null) {
            return OperationResult.failure("Please select an enrollment.");
        }
        double gradeValue = ValidationUtil.parseDoubleSafe(gradeStr);
        if (!ValidationUtil.isValidGrade(gradeValue)) {
            return OperationResult.failure("Grade must be between 0 and 100.");
        }
        if (gradeDao.existsForEnrollment(enrollment.getId())) {
            return OperationResult.failure("A grade already exists for this enrollment. Use 'Update Grade' instead.");
        }

        Grade grade = new Grade(
            enrollment.getId(), gradeValue,
            Grade.calculateGradeLetter(gradeValue),
            feedback.trim(), LocalDate.now()
        );
        if (gradeDao.create(grade) > 0) {
            return OperationResult.success("Grade assigned successfully.");
        }
        return OperationResult.failure("Failed to assign grade.");
    }

    public OperationResult updateGrade(Grade grade, String gradeStr, String feedback) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        if (grade == null) {
            return OperationResult.failure("Please select a grade to update.");
        }
        double gradeValue = ValidationUtil.parseDoubleSafe(gradeStr);
        if (!ValidationUtil.isValidGrade(gradeValue)) {
            return OperationResult.failure("Grade must be between 0 and 100.");
        }

        grade.setGradeValue(gradeValue);
        grade.setGradeLetter(Grade.calculateGradeLetter(gradeValue));
        grade.setFeedback(feedback.trim());
        grade.setGradedDate(LocalDate.now());

        if (gradeDao.update(grade)) {
            return OperationResult.success("Grade updated successfully.");
        }
        return OperationResult.failure("Failed to update grade.");
    }

    public OperationResult deleteGrade(Grade grade) {
        OperationResult auth = requireAdmin();
        if (auth != null) {
            return auth;
        }
        if (grade == null) {
            return OperationResult.failure("Please select a grade to delete.");
        }
        gradeDao.delete(grade.getId());
        return OperationResult.success("Grade deleted successfully.");
    }

    // ==================== Reports ====================

    public String generateReport(String reportType, Course course,
                                 LocalDate fromDate, LocalDate toDate, Student student) {
        if (!SessionManager.isAdmin()) {
            return "Access denied. Admin role is required.";
        }
        if (reportType == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("============================================\n");
        sb.append("  ").append(reportType.toUpperCase()).append("\n");
        sb.append("  Generated: ").append(LocalDate.now()).append("\n");
        sb.append("============================================\n\n");

        switch (reportType) {
            case "Average Grade by Course" -> generateAvgGradeReport(sb);
            case "Grade Distribution by Course" -> generateDistributionReport(sb, course);
            case "Enrollment Statistics" -> generateEnrollmentReport(sb);
            case "Grades by Date Range" -> generateDateRangeReport(sb, fromDate, toDate);
            case "Student Transcript" -> generateTranscriptReport(sb, student);
            default -> sb.append("Unknown report type.\n");
        }

        return sb.toString();
    }

    /**
     * Returns true if the report type requires a course filter.
     */
    public boolean requiresCourseFilter(String reportType) {
        return "Grade Distribution by Course".equals(reportType);
    }

    /**
     * Returns true if the report type requires date range filter.
     */
    public boolean requiresDateRangeFilter(String reportType) {
        return "Grades by Date Range".equals(reportType);
    }

    /**
     * Returns true if the report type requires a student filter.
     */
    public boolean requiresStudentFilter(String reportType) {
        return "Student Transcript".equals(reportType);
    }

    private void generateAvgGradeReport(StringBuilder sb) {
        Map<String, Double> averages = gradeDao.getAverageGradeByCourse();
        if (averages.isEmpty()) {
            sb.append("No grade data available.\n");
            return;
        }
        sb.append(String.format("%-35s  %s\n", "Course", "Average Grade"));
        sb.append("----------------------------------------------\n");
        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            sb.append(String.format("%-35s  %.1f (%s)\n",
                entry.getKey(), entry.getValue(),
                Grade.calculateGradeLetter(entry.getValue())));
        }
    }

    private void generateDistributionReport(StringBuilder sb, Course course) {
        if (course == null) {
            sb.append("No course selected.\n");
            return;
        }
        Map<String, Integer> distribution = gradeDao.getGradeDistribution(course.getId());
        Map<String, Object> stats = gradeDao.getCourseGradeStats(course.getId());

        sb.append("Course: ").append(course.getCourseCode())
          .append(" - ").append(course.getCourseName()).append("\n\n");

        sb.append("Grade Distribution:\n");
        sb.append("----------------------\n");
        for (String letter : new String[]{"A", "B", "C", "D", "F"}) {
            int count = distribution.getOrDefault(letter, 0);
            String bar = "#".repeat(count);
            sb.append(String.format("  %s: %d %s\n", letter, count, bar));
        }

        sb.append("\nStatistics:\n");
        sb.append("----------------------\n");
        sb.append(String.format("  Total Graded:  %d\n", stats.getOrDefault("total", 0)));
        sb.append(String.format("  Average:       %.1f\n", stats.getOrDefault("average", 0.0)));
        sb.append(String.format("  Maximum:       %.1f\n", stats.getOrDefault("maximum", 0.0)));
        sb.append(String.format("  Minimum:       %.1f\n", stats.getOrDefault("minimum", 0.0)));
    }

    private void generateEnrollmentReport(StringBuilder sb) {
        Map<String, Integer> statusCounts = gradeDao.getEnrollmentStatusCounts();
        sb.append("Enrollment Status Summary:\n");
        sb.append("--------------------------\n");
        int total = 0;
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            sb.append(String.format("  %-12s: %d\n", entry.getKey(), entry.getValue()));
            total += entry.getValue();
        }
        sb.append(String.format("\n  Total Enrollments: %d\n", total));

        sb.append("\nEnrollments per Course:\n");
        sb.append("--------------------------------------\n");
        List<Course> courses = courseService.getAllCourses();
        for (Course course : courses) {
            int count = courseService.getEnrollmentCount(course.getId());
            sb.append(String.format("  %-30s: %d / %d\n",
                course.getCourseCode() + " - " + course.getCourseName(),
                count, course.getMaxCapacity()));
        }
    }

    private OperationResult toOperationResult(ServiceResult<?> serviceResult) {
        if (serviceResult.isSuccess()) {
            return OperationResult.success(serviceResult.getMessage());
        }
        return OperationResult.failure(serviceResult.getMessage());
    }

    private OperationResult requireAdmin() {
        if (!SessionManager.isAdmin()) {
            return OperationResult.failure("Access denied. Admin role is required.");
        }
        return null;
    }

    private void generateDateRangeReport(StringBuilder sb, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            sb.append("Please select a valid date range.\n");
            return;
        }
        List<Grade> grades = gradeDao.findByDateRange(fromDate, toDate);
        sb.append(String.format("Period: %s to %s\n\n", fromDate, toDate));

        if (grades.isEmpty()) {
            sb.append("No grades found for the selected period.\n");
            return;
        }

        sb.append(String.format("%-18s %-10s %-6s %-6s %s\n",
            "Student", "Course", "Grade", "Letter", "Date"));
        sb.append("--------------------------------------------------------\n");
        for (Grade grade : grades) {
            sb.append(String.format("%-18s %-10s %-6.1f %-6s %s\n",
                grade.getStudentName(), grade.getCourseCode(),
                grade.getGradeValue(), grade.getGradeLetter(),
                grade.getGradedDate()));
        }
        sb.append(String.format("\nTotal Records: %d\n", grades.size()));
    }

    private void generateTranscriptReport(StringBuilder sb, Student student) {
        if (student == null) {
            sb.append("No student selected.\n");
            return;
        }
        List<Grade> grades = gradeDao.findByStudentId(student.getId());

        sb.append("Student: ").append(student.getFullName()).append("\n");
        sb.append("Student Number: ").append(student.getStudentNumber()).append("\n");
        sb.append("Programme: ").append(student.getProgramme()).append("\n\n");

        if (grades.isEmpty()) {
            sb.append("No grades recorded for this student.\n");
            return;
        }

        sb.append(String.format("%-10s %-25s %-8s %-6s %s\n",
            "Code", "Course Name", "Grade", "Letter", "Feedback"));
        sb.append("--------------------------------------------------------------\n");
        double totalGrade = 0;
        for (Grade grade : grades) {
            sb.append(String.format("%-10s %-25s %-8.1f %-6s %s\n",
                grade.getCourseCode(), grade.getCourseName(),
                grade.getGradeValue(), grade.getGradeLetter(),
                grade.getFeedback() != null ? grade.getFeedback() : ""));
            totalGrade += grade.getGradeValue();
        }
        double average = totalGrade / grades.size();
        sb.append("\n--------------------------------------------------------------\n");
        sb.append(String.format("Overall Average: %.1f (%s)\n", average,
            Grade.calculateGradeLetter(average)));
        sb.append(String.format("Total Courses Graded: %d\n", grades.size()));
    }

    /** Logs out the current user. */
    public void logout() {
        SessionManager.logout();
    }
}
