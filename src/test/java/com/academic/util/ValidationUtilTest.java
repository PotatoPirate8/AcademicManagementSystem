package com.academic.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationUtil methods.
 */
class ValidationUtilTest {

    // ==================== isNullOrEmpty ====================

    @Test
    void testIsNullOrEmptyWithNull() {
        assertTrue(ValidationUtil.isNullOrEmpty(null));
    }

    @Test
    void testIsNullOrEmptyWithEmptyString() {
        assertTrue(ValidationUtil.isNullOrEmpty(""));
    }

    @Test
    void testIsNullOrEmptyWithWhitespace() {
        assertTrue(ValidationUtil.isNullOrEmpty("   "));
    }

    @Test
    void testIsNullOrEmptyWithValidString() {
        assertFalse(ValidationUtil.isNullOrEmpty("hello"));
    }

    // ==================== isValidEmail ====================

    @Test
    void testValidEmails() {
        assertTrue(ValidationUtil.isValidEmail("user@example.com"));
        assertTrue(ValidationUtil.isValidEmail("test.user@uni.ac.uk"));
        assertTrue(ValidationUtil.isValidEmail("name+tag@domain.org"));
    }

    @Test
    void testInvalidEmails() {
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail("invalid"));
        assertFalse(ValidationUtil.isValidEmail("@example.com"));
        assertFalse(ValidationUtil.isValidEmail("user@"));
    }

    // ==================== isValidStudentNumber ====================

    @Test
    void testValidStudentNumbers() {
        assertTrue(ValidationUtil.isValidStudentNumber("SN12345"));
        assertTrue(ValidationUtil.isValidStudentNumber("ABCD"));
        assertTrue(ValidationUtil.isValidStudentNumber("123456789012")); // 12 chars
    }

    @Test
    void testInvalidStudentNumbers() {
        assertFalse(ValidationUtil.isValidStudentNumber(""));
        assertFalse(ValidationUtil.isValidStudentNumber(null));
        assertFalse(ValidationUtil.isValidStudentNumber("AB"));   // too short
        assertFalse(ValidationUtil.isValidStudentNumber("AB!@#")); // special chars
        assertFalse(ValidationUtil.isValidStudentNumber("1234567890123")); // 13 chars, too long
    }

    // ==================== isValidCourseCode ====================

    @Test
    void testValidCourseCodes() {
        assertTrue(ValidationUtil.isValidCourseCode("COMP1322"));
        assertTrue(ValidationUtil.isValidCourseCode("CS101"));
        assertTrue(ValidationUtil.isValidCourseCode("MATH10001"));
    }

    @Test
    void testInvalidCourseCodes() {
        assertFalse(ValidationUtil.isValidCourseCode(""));
        assertFalse(ValidationUtil.isValidCourseCode(null));
        assertFalse(ValidationUtil.isValidCourseCode("123ABC"));
        assertFalse(ValidationUtil.isValidCourseCode("C1"));
        assertFalse(ValidationUtil.isValidCourseCode("comp1322")); // lowercase letters
    }

    // ==================== isValidGrade ====================

    @Test
    void testValidGrades() {
        assertTrue(ValidationUtil.isValidGrade(0));
        assertTrue(ValidationUtil.isValidGrade(50));
        assertTrue(ValidationUtil.isValidGrade(100));
        assertTrue(ValidationUtil.isValidGrade(99.9));
    }

    @Test
    void testInvalidGrades() {
        assertFalse(ValidationUtil.isValidGrade(-1));
        assertFalse(ValidationUtil.isValidGrade(-0.01));
        assertFalse(ValidationUtil.isValidGrade(100.01));
        assertFalse(ValidationUtil.isValidGrade(101));
    }

    // ==================== isPositiveInteger ====================

    @Test
    void testPositiveIntegers() {
        assertTrue(ValidationUtil.isPositiveInteger(1));
        assertTrue(ValidationUtil.isPositiveInteger(100));
    }

    @Test
    void testNonPositiveIntegers() {
        assertFalse(ValidationUtil.isPositiveInteger(0));
        assertFalse(ValidationUtil.isPositiveInteger(-5));
    }

    // ==================== isValidUsername ====================

    @Test
    void testValidUsernames() {
        assertTrue(ValidationUtil.isValidUsername("admin"));
        assertTrue(ValidationUtil.isValidUsername("user_123"));
        assertTrue(ValidationUtil.isValidUsername("abc")); // minimum 3 chars
    }

    @Test
    void testInvalidUsernames() {
        assertFalse(ValidationUtil.isValidUsername(""));
        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername("ab"));          // too short
        assertFalse(ValidationUtil.isValidUsername("user name"));   // contains space
        assertFalse(ValidationUtil.isValidUsername("user@name"));   // special chars
    }

    // ==================== isValidPassword ====================

    @Test
    void testValidPasswords() {
        assertTrue(ValidationUtil.isValidPassword("password"));
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertTrue(ValidationUtil.isValidPassword("abcdef")); // exactly 6
    }

    @Test
    void testInvalidPasswords() {
        assertFalse(ValidationUtil.isValidPassword("12345"));  // too short
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword(null));
    }

    // ==================== parseIntSafe ====================

    @Test
    void testParseIntSafeValid() {
        assertEquals(42, ValidationUtil.parseIntSafe("42"));
        assertEquals(0, ValidationUtil.parseIntSafe("0"));
        assertEquals(-5, ValidationUtil.parseIntSafe("-5"));
    }

    @Test
    void testParseIntSafeInvalid() {
        assertEquals(-1, ValidationUtil.parseIntSafe("abc"));
        assertEquals(-1, ValidationUtil.parseIntSafe(null));
        assertEquals(-1, ValidationUtil.parseIntSafe(""));
        assertEquals(-1, ValidationUtil.parseIntSafe("12.5"));
    }

    // ==================== parseDoubleSafe ====================

    @Test
    void testParseDoubleSafeValid() {
        assertEquals(3.14, ValidationUtil.parseDoubleSafe("3.14"), 0.001);
        assertEquals(0.0, ValidationUtil.parseDoubleSafe("0"), 0.001);
        assertEquals(100.0, ValidationUtil.parseDoubleSafe("100"), 0.001);
    }

    @Test
    void testParseDoubleSafeInvalid() {
        assertEquals(-1.0, ValidationUtil.parseDoubleSafe("abc"), 0.001);
        assertEquals(-1.0, ValidationUtil.parseDoubleSafe(null), 0.001);
        assertEquals(-1.0, ValidationUtil.parseDoubleSafe(""), 0.001);
    }
}
