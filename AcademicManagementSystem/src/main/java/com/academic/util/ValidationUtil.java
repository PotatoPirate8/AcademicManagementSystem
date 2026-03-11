package com.academic.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating user inputs across the application.
 * Uses regex patterns and range checks to ensure data integrity.
 */
public class ValidationUtil {

    // Static patterns stored for reuse (avoids recompilation)
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern STUDENT_NUMBER_PATTERN =
        Pattern.compile("^[A-Za-z0-9]{4,12}$");
    private static final Pattern COURSE_CODE_PATTERN =
        Pattern.compile("^[A-Z]{2,5}[0-9]{3,5}$");

    private ValidationUtil() {} // Prevent instantiation

    /** Checks if a string is null, empty, or contains only whitespace */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Validates an email address format */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /** Validates a student number (4-12 alphanumeric characters) */
    public static boolean isValidStudentNumber(String studentNumber) {
        if (isNullOrEmpty(studentNumber)) return false;
        return STUDENT_NUMBER_PATTERN.matcher(studentNumber).matches();
    }

    /** Validates a course code format (e.g., COMP1322, CS101, MATH10001) */
    public static boolean isValidCourseCode(String courseCode) {
        if (isNullOrEmpty(courseCode)) return false;
        return COURSE_CODE_PATTERN.matcher(courseCode).matches();
    }

    /** Validates that a grade value is within the 0-100 range */
    public static boolean isValidGrade(double grade) {
        return grade >= 0 && grade <= 100;
    }

    /** Validates that an integer is positive (greater than zero) */
    public static boolean isPositiveInteger(int value) {
        return value > 0;
    }

    /** Validates a username (3-20 alphanumeric characters or underscores) */
    public static boolean isValidUsername(String username) {
        if (isNullOrEmpty(username)) return false;
        return username.matches("^[A-Za-z0-9_]{3,20}$");
    }

    /** Validates a password (minimum 6 characters) */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Safely parses an integer from a string.
     * @return the parsed integer, or -1 if the string is not a valid integer
     */
    public static int parseIntSafe(String value) {
        if (isNullOrEmpty(value)) return -1;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Safely parses a double from a string.
     * @return the parsed double, or -1.0 if the string is not a valid number
     */
    public static double parseDoubleSafe(String value) {
        if (isNullOrEmpty(value)) return -1.0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }
}
