package com.academic.view;

import com.academic.controller.StudentController;
import com.academic.controller.StudentController.OperationResult;
import com.academic.model.*;
import com.academic.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Student dashboard view with tabs for:
 * - My Courses (enrolled courses with withdraw option)
 * - Available Courses (browse and enroll)
 * - My Grades (view grades and feedback)
 * - My Profile (view and update personal details)
 */
public class StudentDashboardView {

    private final Stage stage;
    private final BorderPane root;
    private final StudentController controller;
    private Student currentStudent;

    // Tables that need periodic refreshing
    private TableView<Enrollment> myCoursesTable;
    private TableView<Course> availableCoursesTable;
    private TableView<Grade> gradesTable;

    public StudentDashboardView(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.controller = new StudentController();
        this.currentStudent = controller.getCurrentStudent();
        buildUI();
    }

    public BorderPane getRoot() {
        return root;
    }

    /** Assembles the top bar and tab pane */
    private void buildUI() {
        HBox topBar = createTopBar();
        root.setTop(topBar);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
            createMyCoursesTab(),
            createAvailableCoursesTab(),
            createGradesTab(),
            createProfileTab()
        );

        root.setCenter(tabPane);
    }

    /** Creates the top navigation bar with welcome message and logout */
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.getStyleClass().add("top-bar");

        String name = currentStudent != null ? currentStudent.getFullName() : "Student";
        Label welcomeLabel = new Label("Welcome, " + name);
        welcomeLabel.getStyleClass().add("welcome-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("danger-button");
        logoutButton.setOnAction(e -> handleLogout());

        topBar.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        return topBar;
    }

    // ==================== My Courses Tab ====================

    /** Creates the tab showing currently enrolled courses */
    private Tab createMyCoursesTab() {
        Tab tab = new Tab("My Courses");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        myCoursesTable = new TableView<>();
        myCoursesTable.setPlaceholder(new Label("You are not enrolled in any courses."));

        TableColumn<Enrollment, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        codeCol.setPrefWidth(120);

        TableColumn<Enrollment, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(220);

        TableColumn<Enrollment, LocalDate> dateCol = new TableColumn<>("Enrollment Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));
        dateCol.setPrefWidth(130);

        TableColumn<Enrollment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        myCoursesTable.getColumns().addAll(codeCol, nameCol, dateCol, statusCol);

        Button withdrawButton = new Button("Withdraw from Selected Course");
        withdrawButton.getStyleClass().add("danger-button");
        withdrawButton.setOnAction(e -> handleWithdraw());

        refreshMyCourses();

        content.getChildren().addAll(myCoursesTable, withdrawButton);
        VBox.setVgrow(myCoursesTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    // ==================== Available Courses Tab ====================

    /** Creates the tab for browsing and enrolling in courses */
    private Tab createAvailableCoursesTab() {
        Tab tab = new Tab("Available Courses");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        availableCoursesTable = new TableView<>();
        availableCoursesTable.setPlaceholder(new Label("No courses available."));

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        codeCol.setPrefWidth(100);

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(220);

        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(70);

        TableColumn<Course, String> lecturerCol = new TableColumn<>("Lecturer");
        lecturerCol.setCellValueFactory(new PropertyValueFactory<>("lecturerName"));
        lecturerCol.setPrefWidth(150);

        TableColumn<Course, Integer> capacityCol = new TableColumn<>("Max Capacity");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("maxCapacity"));
        capacityCol.setPrefWidth(100);

        availableCoursesTable.getColumns().addAll(codeCol, nameCol, creditsCol, lecturerCol, capacityCol);

        Button enrollButton = new Button("Enroll in Selected Course");
        enrollButton.getStyleClass().add("primary-button");
        enrollButton.setOnAction(e -> handleEnroll());

        refreshAvailableCourses();

        content.getChildren().addAll(availableCoursesTable, enrollButton);
        VBox.setVgrow(availableCoursesTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    // ==================== Grades Tab ====================

    /** Creates the tab for viewing grades */
    private Tab createGradesTab() {
        Tab tab = new Tab("My Grades");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        gradesTable = new TableView<>();
        gradesTable.setPlaceholder(new Label("No grades available yet."));

        TableColumn<Grade, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        codeCol.setPrefWidth(120);

        TableColumn<Grade, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(200);

        TableColumn<Grade, Double> valueCol = new TableColumn<>("Grade (%)");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("gradeValue"));
        valueCol.setPrefWidth(90);

        TableColumn<Grade, String> letterCol = new TableColumn<>("Letter");
        letterCol.setCellValueFactory(new PropertyValueFactory<>("gradeLetter"));
        letterCol.setPrefWidth(70);

        TableColumn<Grade, String> feedbackCol = new TableColumn<>("Feedback");
        feedbackCol.setCellValueFactory(new PropertyValueFactory<>("feedback"));
        feedbackCol.setPrefWidth(200);

        TableColumn<Grade, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("gradedDate"));
        dateCol.setPrefWidth(110);

        gradesTable.getColumns().addAll(codeCol, nameCol, valueCol, letterCol, feedbackCol, dateCol);

        refreshGrades();

        content.getChildren().addAll(gradesTable);
        VBox.setVgrow(gradesTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    // ==================== Profile Tab ====================

    /** Creates the profile tab for viewing and updating personal details */
    private Tab createProfileTab() {
        Tab tab = new Tab("My Profile");
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        Text profileTitle = new Text("Student Profile");
        profileTitle.getStyleClass().add("section-title");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        TextField firstNameField = new TextField(currentStudent != null ? currentStudent.getFirstName() : "");
        TextField lastNameField = new TextField(currentStudent != null ? currentStudent.getLastName() : "");
        TextField emailField = new TextField(currentStudent != null ? currentStudent.getEmail() : "");
        Label studentNumberLabel = new Label(currentStudent != null ? currentStudent.getStudentNumber() : "");
        TextField programmeField = new TextField(currentStudent != null ? currentStudent.getProgramme() : "");

        form.addRow(0, new Label("First Name:"), firstNameField);
        form.addRow(1, new Label("Last Name:"), lastNameField);
        form.addRow(2, new Label("Email:"), emailField);
        form.addRow(3, new Label("Student Number:"), studentNumberLabel);
        form.addRow(4, new Label("Programme:"), programmeField);

        Button updateButton = new Button("Update Profile");
        updateButton.getStyleClass().add("primary-button");
        Label statusLabel = new Label();

        updateButton.setOnAction(e -> {
            if (currentStudent == null) return;

            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String programme = programmeField.getText().trim();

            OperationResult result = controller.updateProfile(
                currentStudent, firstName, lastName, email, programme
            );

            if (result.isSuccess()) {
                statusLabel.setStyle("-fx-text-fill: #27ae60;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
            statusLabel.setText(result.getMessage());
        });

        content.getChildren().addAll(profileTitle, form, updateButton, statusLabel);
        tab.setContent(content);
        return tab;
    }

    // ==================== Data Refresh Methods ====================

    private void refreshMyCourses() {
        if (currentStudent == null) return;
        ObservableList<Enrollment> enrollments = FXCollections.observableArrayList(
            controller.getMyEnrollments(currentStudent.getId())
        );
        myCoursesTable.setItems(enrollments);
    }

    private void refreshAvailableCourses() {
        ObservableList<Course> courses = FXCollections.observableArrayList(controller.getAllCourses());
        availableCoursesTable.setItems(courses);
    }

    private void refreshGrades() {
        if (currentStudent == null) return;
        ObservableList<Grade> grades = FXCollections.observableArrayList(
            controller.getMyGrades(currentStudent.getId())
        );
        gradesTable.setItems(grades);
    }

    // ==================== Event Handlers ====================

    /** Handles course enrollment with validation and capacity check */
    private void handleEnroll() {
        Course selected = availableCoursesTable.getSelectionModel().getSelectedItem();
        OperationResult result = controller.enroll(currentStudent, selected);

        if (result.isSuccess()) {
            AlertUtil.showInfo("Success", result.getMessage());
            refreshMyCourses();
            refreshAvailableCourses();
        } else {
            AlertUtil.showWarning("Cannot Enroll", result.getMessage());
        }
    }

    /** Handles course withdrawal with confirmation */
    private void handleWithdraw() {
        Enrollment selected = myCoursesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select an enrollment to withdraw from.");
            return;
        }

        if (AlertUtil.showConfirmation("Confirm Withdrawal",
                "Are you sure you want to withdraw from " + selected.getCourseCode() + "?")) {
            OperationResult result = controller.withdraw(selected);
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshMyCourses();
            } else {
                AlertUtil.showWarning("Cannot Withdraw", result.getMessage());
            }
        }
    }

    /** Handles logout by clearing session and returning to login */
    private void handleLogout() {
        controller.logout();
        LoginView loginView = new LoginView(stage);
        Scene scene = new Scene(loginView.getRoot(), 400, 550);
        scene.getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        stage.setTitle("Academic Management System");
        stage.setScene(scene);
    }
}
