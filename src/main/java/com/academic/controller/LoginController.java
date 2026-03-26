package com.academic.controller;

import com.academic.dao.UserDao;
import com.academic.model.Student;
import com.academic.model.User;
import com.academic.service.ServiceResult;
import com.academic.service.StudentService;
import com.academic.util.ErrorHandler;
import com.academic.util.PasswordUtil;
import com.academic.util.SessionManager;
import com.academic.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for login and registration operations.
 * Handles authentication, session management, and student registration
 * with all associated validation and business logic.
 */
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private final UserDao userDao;
    private final StudentService studentService;

    public LoginController() {
        this.userDao = new UserDao();
        this.studentService = new StudentService();
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
            ErrorHandler.logValidationFailure("login", "Missing username or password", username);
            return LoginResult.failure("Please enter both username and password.");
        }

        LOGGER.info("Login attempt for username: {}", username);
        String hash = PasswordUtil.hashPassword(password);
        User user = userDao.authenticate(username, hash);

        if (user == null) {
            ErrorHandler.logAuthEvent("LOGIN_FAILED", username, false, "Invalid credentials provided");
            LOGGER.warn("Failed login attempt for username: {}", username);
            return LoginResult.failure("Invalid username or password.");
        }

        SessionManager.login(user);
        ErrorHandler.logAuthEvent("LOGIN_SUCCESS", username, true, "User role: " + user.getRole());

        Student student = null;
        if (user.getRole() == User.Role.STUDENT) {
            student = studentService.findByUserId(user.getId());
            SessionManager.setCurrentStudent(student);
            LOGGER.info("Student session set for user ID: {}", user.getId());
        }

        LOGGER.info("User {} logged in successfully with role: {}", username, user.getRole());
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

        ServiceResult<Integer> result = studentService.registerStudentAccount(
            username, password, firstName, lastName, email, studentNumber, programme
        );
        if (!result.isSuccess()) {
            if ("Username already taken.".equals(result.getMessage())) {
                return RegisterResult.failure("Username already taken. Please choose another.");
            }
            if ("Failed to create student. Student number may already exist.".equals(result.getMessage())) {
                return RegisterResult.failure("Registration failed. Student number may already exist.");
            }
            if ("Failed to create user account.".equals(result.getMessage())) {
                return RegisterResult.failure("Registration failed. Please try again.");
            }
            return RegisterResult.failure(result.getMessage());
        }
        return RegisterResult.success();
    }
}
