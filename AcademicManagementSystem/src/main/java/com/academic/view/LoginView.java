package com.academic.view;

import com.academic.dao.StudentDao;
import com.academic.dao.UserDao;
import com.academic.model.Student;
import com.academic.model.User;
import com.academic.util.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Login and registration view.
 * Provides authentication for both students and admins,
 * and allows new students to register.
 */
public class LoginView {

    private final Stage stage;
    private final BorderPane root;
    private final UserDao userDao;
    private final StudentDao studentDao;

    public LoginView(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.userDao = new UserDao();
        this.studentDao = new StudentDao();
        buildLoginForm();
    }

    public BorderPane getRoot() {
        return root;
    }

    /** Builds the main login form UI */
    private void buildLoginForm() {
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(40));
        loginBox.getStyleClass().add("login-box");

        // Title
        Text title = new Text("Academic Management System");
        title.getStyleClass().add("title-text");
        Text subtitle = new Text("Please sign in to continue");
        subtitle.getStyleClass().add("subtitle-text");

        // Username field
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(250);

        // Password field
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(250);

        // Login button
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(250);

        // Error label for validation messages
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");

        // Register link for new students
        Hyperlink registerLink = new Hyperlink("New student? Register here");

        // Default credentials hint
        Label hintLabel = new Label("Default admin: admin / admin123");
        hintLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");

        // Login action
        loginButton.setOnAction(e -> handleLogin(
            usernameField.getText(), passwordField.getText(), errorLabel
        ));

        // Allow login via Enter key
        passwordField.setOnAction(e -> handleLogin(
            usernameField.getText(), passwordField.getText(), errorLabel
        ));

        // Register action
        registerLink.setOnAction(e -> showRegisterForm());

        loginBox.getChildren().addAll(
            title, subtitle,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            loginButton, errorLabel,
            registerLink, hintLabel
        );

        root.setCenter(loginBox);
    }

    /** Validates credentials and navigates to the appropriate dashboard */
    private void handleLogin(String username, String password, Label errorLabel) {
        // Input validation
        if (ValidationUtil.isNullOrEmpty(username) || ValidationUtil.isNullOrEmpty(password)) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        // Authenticate against database
        String hash = PasswordUtil.hashPassword(password);
        User user = userDao.authenticate(username, hash);

        if (user == null) {
            errorLabel.setText("Invalid username or password.");
            return;
        }

        // Set the session
        SessionManager.login(user);

        // Route to the correct dashboard based on role
        if (user.getRole() == User.Role.STUDENT) {
            Student student = studentDao.findByUserId(user.getId());
            SessionManager.setCurrentStudent(student);
            showStudentDashboard();
        } else {
            showAdminDashboard();
        }
    }

    /** Shows the student registration form */
    private void showRegisterForm() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox registerBox = new VBox(12);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(30));
        registerBox.getStyleClass().add("login-box");

        Text title = new Text("Student Registration");
        title.getStyleClass().add("title-text");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (3-20 alphanumeric)");
        usernameField.setMaxWidth(280);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 characters)");
        passwordField.setMaxWidth(280);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        confirmPasswordField.setMaxWidth(280);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First name");
        firstNameField.setMaxWidth(280);

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last name");
        lastNameField.setMaxWidth(280);

        TextField emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setMaxWidth(280);

        TextField studentNumberField = new TextField();
        studentNumberField.setPromptText("Student number (e.g., SN12345)");
        studentNumberField.setMaxWidth(280);

        TextField programmeField = new TextField();
        programmeField.setPromptText("Programme (e.g., Computer Science)");
        programmeField.setMaxWidth(280);

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("primary-button");
        registerButton.setMaxWidth(280);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(280);

        Hyperlink backLink = new Hyperlink("Back to login");
        backLink.setOnAction(e -> buildLoginForm());

        registerButton.setOnAction(e -> handleRegister(
            usernameField.getText(), passwordField.getText(), confirmPasswordField.getText(),
            firstNameField.getText(), lastNameField.getText(), emailField.getText(),
            studentNumberField.getText(), programmeField.getText(), errorLabel
        ));

        registerBox.getChildren().addAll(
            title,
            usernameField, passwordField, confirmPasswordField,
            firstNameField, lastNameField, emailField,
            studentNumberField, programmeField,
            registerButton, errorLabel, backLink
        );

        scrollPane.setContent(registerBox);
        root.setCenter(scrollPane);
    }

    /** Validates all registration fields and creates the student account */
    private void handleRegister(String username, String password, String confirmPassword,
                                String firstName, String lastName, String email,
                                String studentNumber, String programme, Label errorLabel) {
        // Validate all fields one by one for clear error messages
        if (!ValidationUtil.isValidUsername(username)) {
            errorLabel.setText("Username must be 3-20 alphanumeric characters.");
            return;
        }
        if (!ValidationUtil.isValidPassword(password)) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            errorLabel.setText("Please enter your full name.");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }
        if (!ValidationUtil.isValidStudentNumber(studentNumber)) {
            errorLabel.setText("Student number must be 4-12 alphanumeric characters.");
            return;
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            errorLabel.setText("Please enter your programme.");
            return;
        }

        // Check if username is already taken
        if (userDao.findByUsername(username) != null) {
            errorLabel.setText("Username already taken. Please choose another.");
            return;
        }

        // Create user account
        String hash = PasswordUtil.hashPassword(password);
        User user = new User(username, hash, User.Role.STUDENT);
        int userId = userDao.create(user);

        if (userId == -1) {
            errorLabel.setText("Registration failed. Please try again.");
            return;
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
            errorLabel.setText("Registration failed. Student number may already exist.");
            return;
        }

        AlertUtil.showInfo("Success", "Registration successful! You can now login.");
        buildLoginForm();
    }

    /** Navigates to the student dashboard */
    private void showStudentDashboard() {
        StudentDashboardView dashboard = new StudentDashboardView(stage);
        Scene scene = new Scene(dashboard.getRoot(), 900, 650);
        scene.getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        stage.setTitle("Academic Management System - Student Dashboard");
        stage.setScene(scene);
    }

    /** Navigates to the admin dashboard */
    private void showAdminDashboard() {
        AdminDashboardView dashboard = new AdminDashboardView(stage);
        Scene scene = new Scene(dashboard.getRoot(), 1050, 700);
        scene.getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        stage.setTitle("Academic Management System - Admin Dashboard");
        stage.setScene(scene);
    }
}
