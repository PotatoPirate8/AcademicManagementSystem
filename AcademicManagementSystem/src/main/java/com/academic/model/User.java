package com.academic.model;

/**
 * Represents a user account in the system.
 * Users can have either STUDENT or ADMIN role.
 */
public class User {

    /** Defines the privilege levels in the system */
    public enum Role {
        STUDENT, ADMIN
    }

    private int id;
    private String username;
    private String passwordHash;
    private Role role;

    public User() {}

    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public User(int id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
