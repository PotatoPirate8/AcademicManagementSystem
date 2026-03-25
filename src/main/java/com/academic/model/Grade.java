package com.academic.model;

import java.time.LocalDate;

/**
 * Represents a grade assigned to a student's enrollment.
 * Uses standard UK academic grading scale.
 */
public class Grade {

    private int id;
    private int enrollmentId;
    private double gradeValue;
    private String gradeLetter;
    private String feedback;
    private LocalDate gradedDate;
    // Transient display fields populated by JOIN queries
    private String studentName;
    private String courseCode;
    private String courseName;

    public Grade() {}

    public Grade(int enrollmentId, double gradeValue, String gradeLetter,
                 String feedback, LocalDate gradedDate) {
        this.enrollmentId = enrollmentId;
        this.gradeValue = gradeValue;
        this.gradeLetter = gradeLetter;
        this.feedback = feedback;
        this.gradedDate = gradedDate;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public double getGradeValue() { return gradeValue; }
    public void setGradeValue(double gradeValue) { this.gradeValue = gradeValue; }

    public String getGradeLetter() { return gradeLetter; }
    public void setGradeLetter(String gradeLetter) { this.gradeLetter = gradeLetter; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDate getGradedDate() { return gradedDate; }
    public void setGradedDate(LocalDate gradedDate) { this.gradedDate = gradedDate; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    /**
     * Converts a numeric grade to a letter grade using standard academic scale.
     * A: 70+, B: 60-69, C: 50-59, D: 40-49, F: below 40
     */
    public static String calculateGradeLetter(double gradeValue) {
        if (gradeValue >= 70) return "A";
        if (gradeValue >= 60) return "B";
        if (gradeValue >= 50) return "C";
        if (gradeValue >= 40) return "D";
        return "F";
    }

    @Override
    public String toString() {
        return gradeLetter + " (" + gradeValue + ")";
    }
}
