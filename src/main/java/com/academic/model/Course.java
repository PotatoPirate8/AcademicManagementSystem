package com.academic.model;

/**
 * Represents a course offered in the academic system.
 * Each course is assigned to a lecturer and has a maximum capacity.
 */
public class Course {

    private int id;
    private String courseCode;
    private String courseName;
    private int credits;
    private int lecturerId;
    private int maxCapacity;
    private String lecturerName; // Transient field for display

    public Course() {}

    public Course(String courseCode, String courseName, int credits,
                  int lecturerId, int maxCapacity) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.lecturerId = lecturerId;
        this.maxCapacity = maxCapacity;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getLecturerId() { return lecturerId; }
    public void setLecturerId(int lecturerId) { this.lecturerId = lecturerId; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}
