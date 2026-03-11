package com.academic.controller;

import com.academic.dao.StudentDao;
import com.academic.dao.UserDao;
import com.academic.model.Student;
import com.academic.model.User;
import com.academic.util.PasswordUtil;
import com.academic.util.SessionManager;
import com.academic.util.ValidationUtil;

/**
 * Controller for login and registration operations.
 * Handles authentication, session management, and student registration
 * with all associated validation and business logic.
 */
public class LoginController {

    private final UserDao userDao;
    private final StudentDao studentDao;

    public LoginController() {
        this.userDao = new UserDao();
        this.studentDao = new StudentDao();
    }

    /**
     * Result object for login attempts.
     */
    public static class LoginResult {
        private final boolean success;
        private final String errorMessage;
        private final User user;
        private final Student student;

        private LoginResult(boolean success, String errorMessage, User user, Student student) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.user = user;
            this.student = student;
        }

        public static LoginResult failure(String message) {
            return new LoginResult(false, message, null, null);
        }

        public static LoginResult success(User user, Student student) {
            return new LoginResult(true, null, user, student);
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public User getUser() { return user; }
        public Student getStudent() { return student; }
    }

    /**
     * Result object for registration attempts.
     */
    public static class RegisterResult {
        private final boolean success;
        private final String errorMessage;

        private RegisterResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public static RegisterResult failure(String message) {
            return new RegisterResult(false, message);
        }

        public static RegisterResult success() {
            return new RegisterResult(true, null);
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Authenticates a user and sets the session.
     */
    public LoginResult login(String username, String password) {
        if (ValidationUtil.isNullOrEmpty(username) || ValidationUtil.isNullOrEmpty(password)) {
            return LoginResult.failure("Please enter both username and password.");
        }

        String hash = PasswordUtil.hashPassword(password);
        User user = userDao.authenticate(username, hash);

        if (user == null) {
            return LoginResult.failure("Invalid username or password.");
        }

        SessionManager.login(user);

        Student student = null;
        if (user.getRole() == User.Role.STUDENT) {
            student = studentDao.findByUserId(user.getId());
            SessionManager.setCurrentStudent(student);
        }

        return LoginResult.success(user, student);
    }

    /**
     * Validates all registration fields and creates a new student account.
     */
    public RegisterResult register(String username, String password, String confirmPassword,
                                   String firstName, String lastName, String email,
                                   String studentNumber, String programme) {
        // Validate all fields
        if (!ValidationUtil.isValidUsername(username)) {
            return RegisterResult.failure("Username must be 3-20 alphanumeric characters.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            return RegisterResult.failure("Password must be at least 6 characters.");
        }
        if (!password.equals(confirmPassword)) {
            return RegisterResult.failure("Passwords do not match.");
        }
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return RegisterResult.failure("Please enter your full name.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return RegisterResult.failure("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidStudentNumber(studentNumber)) {
            return RegisterResult.failure("Student number must be 4-12 alphanumeric characters.");
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            return RegisterResult.failure("Please enter your programme.");
        }

        // Check if username is already taken
        if (userDao.findByUsername(username) != null) {
            return RegisterResult.failure("Username already taken. Please choose another.");
        }

        // Create user account
        String hash = PasswordUtil.hashPassword(password);
        User user = new User(username, hash, User.Role.STUDENT);
        int userId = userDao.create(user);

        if (userId == -1) {
            return RegisterResult.failure("Registration failed. Please try again.");
        }

        // Create student profile
        Student student = new Student(
            userId, firstName.trim(), lastName.trim(),
            email.trim(), studentNumber.trim().toUpperCase(), programme.trim()
        );
        int studentId = studentDao.create(student);

        if (studentId == -1) {
            // Rollback user if student creation fails
            userDao.delete(userId);
            return RegisterResult.failure("Registration failed. Student number may already exist.");
        }

        return RegisterResult.success();
    }
}
