package com.academic.controller;

import com.academic.dao.*;
import com.academic.model.*;
import com.academic.util.PasswordUtil;
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

    private final UserDao userDao;
    private final StudentDao studentDao;
    private final LecturerDao lecturerDao;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;

    public AdminController() {
        this.userDao = new UserDao();
        this.studentDao = new StudentDao();
        this.lecturerDao = new LecturerDao();
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

    // ==================== Student CRUD ====================

    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    public OperationResult updateStudent(Student student, String firstName, String lastName,
                                         String email, String studentNumber, String programme) {
        if (student == null) {
            return OperationResult.failure("Please select a student to update.");
        }
        // Validate fields
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return OperationResult.failure("Name fields cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return OperationResult.failure("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidStudentNumber(studentNumber)) {
            return OperationResult.failure("Student number must be 4-12 alphanumeric characters.");
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            return OperationResult.failure("Programme cannot be empty.");
        }

        student.setFirstName(firstName.trim());
        student.setLastName(lastName.trim());
        student.setEmail(email.trim());
        student.setStudentNumber(studentNumber.trim().toUpperCase());
        student.setProgramme(programme.trim());

        if (studentDao.update(student)) {
            return OperationResult.success("Student updated successfully.");
        }
        return OperationResult.failure("Failed to update student.");
    }

    public OperationResult addStudent(String username, String password, String firstName,
                                       String lastName, String email, String studentNumber, String programme) {
        if (!ValidationUtil.isValidUsername(username)) {
            return OperationResult.failure("Username must be 3-20 alphanumeric characters.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            return OperationResult.failure("Password must be at least 6 characters.");
        }
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return OperationResult.failure("Name fields cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return OperationResult.failure("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidStudentNumber(studentNumber)) {
            return OperationResult.failure("Student number must be 4-12 alphanumeric characters.");
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            return OperationResult.failure("Programme cannot be empty.");
        }
        if (userDao.findByUsername(username) != null) {
            return OperationResult.failure("Username already taken.");
        }

        String hash = PasswordUtil.hashPassword(password);
        User user = new User(username, hash, User.Role.STUDENT);
        int userId = userDao.create(user);
        if (userId == -1) {
            return OperationResult.failure("Failed to create user account.");
        }

        Student student = new Student(
            userId, firstName.trim(), lastName.trim(),
            email.trim(), studentNumber.trim().toUpperCase(), programme.trim()
        );
        int studentId = studentDao.create(student);
        if (studentId == -1) {
            userDao.delete(userId);
            return OperationResult.failure("Failed to create student. Student number may already exist.");
        }
        return OperationResult.success("Student added successfully.");
    }

    public OperationResult deleteStudent(Student student) {
        if (student == null) {
            return OperationResult.failure("Please select a student to delete.");
        }
        studentDao.delete(student.getId());
        userDao.delete(student.getUserId());
        return OperationResult.success("Student deleted successfully.");
    }

    // ==================== Lecturer CRUD ====================

    public List<Lecturer> getAllLecturers() {
        return lecturerDao.findAll();
    }

    public OperationResult addLecturer(String firstName, String lastName, String email, String department) {
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
        return courseDao.findAll();
    }

    public OperationResult addCourse(String code, String name, String creditsStr,
                                     String capacityStr, Lecturer lecturer) {
        String validationError = validateCourseFields(code, name, creditsStr, capacityStr, lecturer);
        if (validationError != null) {
            return OperationResult.failure(validationError);
        }

        Course course = new Course(
            code.trim().toUpperCase(), name.trim(),
            ValidationUtil.parseIntSafe(creditsStr),
            lecturer.getId(),
            ValidationUtil.parseIntSafe(capacityStr)
        );
        if (courseDao.create(course) > 0) {
            return OperationResult.success("Course added successfully.");
        }
        return OperationResult.failure("Failed to add course. Code may already exist.");
    }

    public OperationResult updateCourse(Course course, String code, String name,
                                        String creditsStr, String capacityStr, Lecturer lecturer) {
        if (course == null) {
            return OperationResult.failure("Please select a course to update.");
        }
        String validationError = validateCourseFields(code, name, creditsStr, capacityStr, lecturer);
        if (validationError != null) {
            return OperationResult.failure(validationError);
        }

        course.setCourseCode(code.trim().toUpperCase());
        course.setCourseName(name.trim());
        course.setCredits(ValidationUtil.parseIntSafe(creditsStr));
        course.setMaxCapacity(ValidationUtil.parseIntSafe(capacityStr));
        course.setLecturerId(lecturer.getId());

        if (courseDao.update(course)) {
            return OperationResult.success("Course updated successfully.");
        }
        return OperationResult.failure("Failed to update course.");
    }

    public OperationResult deleteCourse(Course course) {
        if (course == null) {
            return OperationResult.failure("Please select a course to delete.");
        }
        courseDao.delete(course.getId());
        return OperationResult.success("Course deleted successfully.");
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

    // ==================== Enrollment CRUD ====================

    public List<Enrollment> getAllEnrollments() {
        return enrollmentDao.findAll();
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        return enrollmentDao.findByStudentId(studentId);
    }

    public List<Enrollment> getEnrollmentsByCourse(int courseId) {
        return enrollmentDao.findByCourseId(courseId);
    }

    public OperationResult addEnrollment(Student student, Course course) {
        if (student == null || course == null) {
            return OperationResult.failure("Please select both a student and a course.");
        }
        if (enrollmentDao.isEnrolled(student.getId(), course.getId())) {
            return OperationResult.failure("This student is already enrolled in this course.");
        }

        Enrollment enrollment = new Enrollment(
            student.getId(), course.getId(), LocalDate.now(), Enrollment.Status.ENROLLED
        );
        if (enrollmentDao.create(enrollment) > 0) {
            return OperationResult.success("Enrollment created successfully.");
        }
        return OperationResult.failure("Failed to create enrollment.");
    }

    public OperationResult updateEnrollmentStatus(Enrollment enrollment, Enrollment.Status newStatus) {
        if (enrollment == null) {
            return OperationResult.failure("Please select an enrollment to update.");
        }
        if (newStatus == null) {
            return OperationResult.failure("Please select a status.");
        }

        if (enrollmentDao.updateStatus(enrollment.getId(), newStatus)) {
            return OperationResult.success("Enrollment status updated.");
        }
        return OperationResult.failure("Failed to update status.");
    }

    public OperationResult deleteEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            return OperationResult.failure("Please select an enrollment to delete.");
        }
        enrollmentDao.delete(enrollment.getId());
        return OperationResult.success("Enrollment deleted successfully.");
    }

    // ==================== Grade CRUD ====================

    public List<Grade> getAllGrades() {
        return gradeDao.findAll();
    }

    public OperationResult addGrade(Enrollment enrollment, String gradeStr, String feedback) {
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
        if (grade == null) {
            return OperationResult.failure("Please select a grade to delete.");
        }
        gradeDao.delete(grade.getId());
        return OperationResult.success("Grade deleted successfully.");
    }

    // ==================== Reports ====================

    public String generateReport(String reportType, Course course,
                                 LocalDate fromDate, LocalDate toDate, Student student) {
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
        List<Course> courses = courseDao.findAll();
        for (Course course : courses) {
            int count = courseDao.getEnrollmentCount(course.getId());
            sb.append(String.format("  %-30s: %d / %d\n",
                course.getCourseCode() + " - " + course.getCourseName(),
                count, course.getMaxCapacity()));
        }
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
