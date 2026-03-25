package com.academic.util;

import com.academic.model.Student;
import com.academic.model.User;

/**
 * Manages the current user session.
 * Stores the logged-in user and their associated student profile (if applicable).
 */
public class SessionManager {

    private static User currentUser;
    private static Student currentStudent;

    private SessionManager() {} // Prevent instantiation

    /** Sets the current logged-in user */
    public static void login(User user) {
        currentUser = user;
    }

    /** Sets the student profile for the current session */
    public static void setCurrentStudent(Student student) {
        currentStudent = student;
    }

    /** Returns the currently logged-in user */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** Returns the student profile of the current user (null if admin) */
    public static Student getCurrentStudent() {
        return currentStudent;
    }

    /** Checks if the current user has admin privileges */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    /** Checks if the current user has student privileges */
    public static boolean isStudent() {
        return currentUser != null && currentUser.getRole() == User.Role.STUDENT;
    }

    /** Checks if the current user has lecturer privileges */
    public static boolean isLecturer() {
        return currentUser != null && currentUser.getRole() == User.Role.LECTURER;
    }

    /** Checks if a user is currently logged in */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Clears all session data on logout */
    public static void logout() {
        currentUser = null;
        currentStudent = null;
    }
}
