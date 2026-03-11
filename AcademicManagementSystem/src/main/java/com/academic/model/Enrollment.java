package com.academic.model;

import java.time.LocalDate;

/**
 * Represents a student's enrollment in a course.
 * Tracks enrollment date and current status.
 */
public class Enrollment {

    /** Possible states for an enrollment */
    public enum Status {
        ENROLLED, COMPLETED, WITHDRAWN
    }

    private int id;
    private int studentId;
    private int courseId;
    private LocalDate enrollmentDate;
    private Status status;
    // Transient display fields populated by JOIN queries
    private String studentName;
    private String courseCode;
    private String courseName;

    public Enrollment() {}

    public Enrollment(int studentId, int courseId, LocalDate enrollmentDate, Status status) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    @Override
    public String toString() {
        return "Enrollment #" + id + " [" + status + "]";
    }
}
