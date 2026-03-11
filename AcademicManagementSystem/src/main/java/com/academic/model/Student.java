package com.academic.model;

/**
 * Represents a student in the academic system.
 * Linked to a User account via userId.
 */
public class Student {

    private int id;
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String studentNumber;
    private String programme;

    public Student() {}

    public Student(int userId, String firstName, String lastName,
                   String email, String studentNumber, String programme) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.studentNumber = studentNumber;
        this.programme = programme;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }

    /** Returns full name for display purposes */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return studentNumber + " - " + getFullName();
    }
}
