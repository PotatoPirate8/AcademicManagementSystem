package com.academic.view;

import com.academic.controller.AdminController;
import com.academic.controller.AdminController.OperationResult;
import com.academic.model.*;
import com.academic.util.*;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin dashboard view with tabs for:
 * - Students (CRUD)
 * - Lecturers (CRUD)
 * - Courses (CRUD)
 * - Enrollments (CRUD)
 * - Grades (CRUD)
 * - Reports (filtered academic reports)
 */
public class AdminDashboardView {

    private final Stage stage;
    private final BorderPane root;
    private final AdminController controller;

    // Tables for each CRUD section
    private TableView<Student> studentsTable;
    private TableView<Lecturer> lecturersTable;
    private TableView<Course> coursesTable;
    private TableView<Enrollment> enrollmentsTable;
    private TableView<Grade> gradesTable;

    public AdminDashboardView(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.controller = new AdminController();
        buildUI();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void buildUI() {
        HBox topBar = createTopBar();
        root.setTop(topBar);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
            createStudentsTab(),
            createLecturersTab(),
            createCoursesTab(),
            createEnrollmentsTab(),
            createGradesTab(),
            createReportsTab()
        );

        root.setCenter(tabPane);
    }

    /** Creates the top bar with title and logout button */
    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.getStyleClass().add("top-bar");

        Label welcomeLabel = new Label("Admin Dashboard");
        welcomeLabel.getStyleClass().add("welcome-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("danger-button");
        logoutButton.setOnAction(e -> handleLogout());

        topBar.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        return topBar;
    }

    // ==================== Students Tab ====================

    private Tab createStudentsTab() {
        Tab tab = new Tab("Students");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        studentsTable = new TableView<>();
        studentsTable.setPlaceholder(new Label("No students found."));

        TableColumn<Student, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Student, String> numCol = new TableColumn<>("Student No.");
        numCol.setCellValueFactory(new PropertyValueFactory<>("studentNumber"));
        numCol.setPrefWidth(100);

        TableColumn<Student, String> fnCol = new TableColumn<>("First Name");
        fnCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        fnCol.setPrefWidth(100);

        TableColumn<Student, String> lnCol = new TableColumn<>("Last Name");
        lnCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lnCol.setPrefWidth(100);

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);

        TableColumn<Student, String> progCol = new TableColumn<>("Programme");
        progCol.setCellValueFactory(new PropertyValueFactory<>("programme"));
        progCol.setPrefWidth(150);

        studentsTable.getColumns().addAll(idCol, numCol, fnCol, lnCol, emailCol, progCol);

        // Edit form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        TextField fnField = new TextField();
        fnField.setPromptText("First Name");
        TextField lnField = new TextField();
        lnField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField snField = new TextField();
        snField.setPromptText("Student Number");
        TextField progField = new TextField();
        progField.setPromptText("Programme");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        form.addRow(0, new Label("Username:"), usernameField, new Label("Password:"), passwordField);
        form.addRow(1, new Label(""), new Label(""), new Label("Confirm Password:"), confirmPasswordField);
        form.addRow(2, new Label("First Name:"), fnField, new Label("Last Name:"), lnField);
        form.addRow(3, new Label("Email:"), emailField, new Label("Student No:"), snField);
        form.addRow(4, new Label("Programme:"), progField);

        // Populate form when a row is selected
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fnField.setText(newVal.getFirstName());
                lnField.setText(newVal.getLastName());
                emailField.setText(newVal.getEmail());
                snField.setText(newVal.getStudentNumber());
                progField.setText(newVal.getProgramme());
            }
        });

        // Action buttons
        HBox buttons = new HBox(10);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("success-button");
        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("primary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        Button clearBtn = new Button("Clear");

        addBtn.setOnAction(e -> {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                AlertUtil.showError("Error", "Passwords do not match.");
                return;
            }
            OperationResult result = controller.addStudent(
                usernameField.getText(), passwordField.getText(),
                fnField.getText(), lnField.getText(),
                emailField.getText(), snField.getText(), progField.getText()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshStudents();
                clearFields(usernameField, passwordField, confirmPasswordField, fnField, lnField, emailField, snField, progField);
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        updateBtn.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            OperationResult result = controller.updateStudent(
                selected, fnField.getText(), lnField.getText(),
                emailField.getText(), snField.getText(), progField.getText()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshStudents();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a student to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete",
                    "Delete student " + selected.getFullName() + "? This will also remove their user account.")) {
                OperationResult result = controller.deleteStudent(selected);
                if (result.isSuccess()) {
                    refreshStudents();
                    clearFields(fnField, lnField, emailField, snField, progField);
                }
            }
        });

        clearBtn.setOnAction(e -> {
            clearFields(usernameField, passwordField, confirmPasswordField, fnField, lnField, emailField, snField, progField);
            studentsTable.getSelectionModel().clearSelection();
        });

        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        refreshStudents();

        content.getChildren().addAll(studentsTable, form, buttons);
        VBox.setVgrow(studentsTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private void refreshStudents() {
        studentsTable.setItems(FXCollections.observableArrayList(controller.getAllStudents()));
    }

    // ==================== Lecturers Tab ====================

    private Tab createLecturersTab() {
        Tab tab = new Tab("Lecturers");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        lecturersTable = new TableView<>();
        lecturersTable.setPlaceholder(new Label("No lecturers found."));

        TableColumn<Lecturer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Lecturer, String> fnCol = new TableColumn<>("First Name");
        fnCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        fnCol.setPrefWidth(120);

        TableColumn<Lecturer, String> lnCol = new TableColumn<>("Last Name");
        lnCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lnCol.setPrefWidth(120);

        TableColumn<Lecturer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Lecturer, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        deptCol.setPrefWidth(150);

        lecturersTable.getColumns().addAll(idCol, fnCol, lnCol, emailCol, deptCol);

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        TextField fnField = new TextField();
        fnField.setPromptText("First Name");
        TextField lnField = new TextField();
        lnField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField deptField = new TextField();
        deptField.setPromptText("Department");

        form.addRow(0, new Label("First Name:"), fnField, new Label("Last Name:"), lnField);
        form.addRow(1, new Label("Email:"), emailField, new Label("Department:"), deptField);

        lecturersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fnField.setText(newVal.getFirstName());
                lnField.setText(newVal.getLastName());
                emailField.setText(newVal.getEmail());
                deptField.setText(newVal.getDepartment());
            }
        });

        HBox buttons = new HBox(10);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("success-button");
        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("primary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        Button clearBtn = new Button("Clear");

        addBtn.setOnAction(e -> {
            OperationResult result = controller.addLecturer(
                fnField.getText(), lnField.getText(),
                emailField.getText(), deptField.getText()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshLecturers();
                clearFields(fnField, lnField, emailField, deptField);
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        updateBtn.setOnAction(e -> {
            Lecturer selected = lecturersTable.getSelectionModel().getSelectedItem();
            OperationResult result = controller.updateLecturer(
                selected, fnField.getText(), lnField.getText(),
                emailField.getText(), deptField.getText()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshLecturers();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Lecturer selected = lecturersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a lecturer to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete",
                    "Delete lecturer " + selected.getFullName() + "?")) {
                OperationResult result = controller.deleteLecturer(selected);
                if (result.isSuccess()) {
                    refreshLecturers();
                    clearFields(fnField, lnField, emailField, deptField);
                }
            }
        });

        clearBtn.setOnAction(e -> {
            clearFields(fnField, lnField, emailField, deptField);
            lecturersTable.getSelectionModel().clearSelection();
        });

        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        refreshLecturers();

        content.getChildren().addAll(lecturersTable, form, buttons);
        VBox.setVgrow(lecturersTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private void refreshLecturers() {
        lecturersTable.setItems(FXCollections.observableArrayList(controller.getAllLecturers()));
    }

    // ==================== Courses Tab ====================

    private Tab createCoursesTab() {
        Tab tab = new Tab("Courses");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        coursesTable = new TableView<>();
        coursesTable.setPlaceholder(new Label("No courses found."));

        TableColumn<Course, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        codeCol.setPrefWidth(100);

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        nameCol.setPrefWidth(180);

        TableColumn<Course, Integer> credCol = new TableColumn<>("Credits");
        credCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        credCol.setPrefWidth(70);

        TableColumn<Course, String> lecCol = new TableColumn<>("Lecturer");
        lecCol.setCellValueFactory(new PropertyValueFactory<>("lecturerName"));
        lecCol.setPrefWidth(150);

        TableColumn<Course, Integer> capCol = new TableColumn<>("Capacity");
        capCol.setCellValueFactory(new PropertyValueFactory<>("maxCapacity"));
        capCol.setPrefWidth(80);

        coursesTable.getColumns().addAll(idCol, codeCol, nameCol, credCol, lecCol, capCol);

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        TextField codeField = new TextField();
        codeField.setPromptText("Code (e.g., COMP1322)");
        TextField nameField = new TextField();
        nameField.setPromptText("Course Name");
        TextField creditsField = new TextField();
        creditsField.setPromptText("Credits");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Max Capacity");

        ComboBox<Lecturer> lecturerCombo = new ComboBox<>();
        lecturerCombo.setPromptText("Select Lecturer");
        lecturerCombo.setItems(FXCollections.observableArrayList(controller.getAllLecturers()));

        form.addRow(0, new Label("Course Code:"), codeField, new Label("Course Name:"), nameField);
        form.addRow(1, new Label("Credits:"), creditsField, new Label("Capacity:"), capacityField);
        form.addRow(2, new Label("Lecturer:"), lecturerCombo);

        coursesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                codeField.setText(newVal.getCourseCode());
                nameField.setText(newVal.getCourseName());
                creditsField.setText(String.valueOf(newVal.getCredits()));
                capacityField.setText(String.valueOf(newVal.getMaxCapacity()));
                // Select the matching lecturer in the dropdown
                for (Lecturer l : lecturerCombo.getItems()) {
                    if (l.getId() == newVal.getLecturerId()) {
                        lecturerCombo.getSelectionModel().select(l);
                        break;
                    }
                }
            }
        });

        HBox buttons = new HBox(10);
        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("success-button");
        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("primary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        Button clearBtn = new Button("Clear");

        addBtn.setOnAction(e -> {
            OperationResult result = controller.addCourse(
                codeField.getText(), nameField.getText(),
                creditsField.getText(), capacityField.getText(),
                lecturerCombo.getSelectionModel().getSelectedItem()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshCourses();
                clearCourseForm(codeField, nameField, creditsField, capacityField, lecturerCombo);
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        updateBtn.setOnAction(e -> {
            Course selected = coursesTable.getSelectionModel().getSelectedItem();
            OperationResult result = controller.updateCourse(
                selected, codeField.getText(), nameField.getText(),
                creditsField.getText(), capacityField.getText(),
                lecturerCombo.getSelectionModel().getSelectedItem()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshCourses();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Course selected = coursesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a course to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete",
                    "Delete course " + selected.getCourseCode() + "?")) {
                controller.deleteCourse(selected);
                refreshCourses();
                clearCourseForm(codeField, nameField, creditsField, capacityField, lecturerCombo);
            }
        });

        clearBtn.setOnAction(e -> clearCourseForm(codeField, nameField, creditsField, capacityField, lecturerCombo));

        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        refreshCourses();

        content.getChildren().addAll(coursesTable, form, buttons);
        VBox.setVgrow(coursesTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private void clearCourseForm(TextField code, TextField name, TextField credits,
                                  TextField capacity, ComboBox<Lecturer> lecturerCombo) {
        clearFields(code, name, credits, capacity);
        lecturerCombo.getSelectionModel().clearSelection();
        coursesTable.getSelectionModel().clearSelection();
    }

    private void refreshCourses() {
        coursesTable.setItems(FXCollections.observableArrayList(controller.getAllCourses()));
    }

    // ==================== Enrollments Tab ====================

    private Tab createEnrollmentsTab() {
        Tab tab = new Tab("Enrollments");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        enrollmentsTable = new TableView<>();
        enrollmentsTable.setPlaceholder(new Label("No enrollments found."));

        TableColumn<Enrollment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Enrollment, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentCol.setPrefWidth(150);

        TableColumn<Enrollment, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        codeCol.setPrefWidth(100);

        TableColumn<Enrollment, String> courseCol = new TableColumn<>("Course Name");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseCol.setPrefWidth(180);

        TableColumn<Enrollment, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));
        dateCol.setPrefWidth(100);

        TableColumn<Enrollment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        enrollmentsTable.getColumns().addAll(idCol, studentCol, codeCol, courseCol, dateCol, statusCol);

        // Form for adding/updating enrollments
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        ComboBox<Student> studentCombo = new ComboBox<>();
        studentCombo.setPromptText("Select Student");
        studentCombo.setItems(FXCollections.observableArrayList(controller.getAllStudents()));

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Select Course");
        courseCombo.setItems(FXCollections.observableArrayList(controller.getAllCourses()));

        ComboBox<Enrollment.Status> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(Enrollment.Status.values()));
        statusCombo.setValue(Enrollment.Status.ENROLLED);

        form.addRow(0, new Label("Student:"), studentCombo, new Label("Course:"), courseCombo);
        form.addRow(1, new Label("Status:"), statusCombo);

        HBox buttons = new HBox(10);
        Button addBtn = new Button("Add Enrollment");
        addBtn.getStyleClass().add("success-button");
        Button updateStatusBtn = new Button("Update Status");
        updateStatusBtn.getStyleClass().add("primary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");

        addBtn.setOnAction(e -> {
            Student student = studentCombo.getSelectionModel().getSelectedItem();
            Course course = courseCombo.getSelectionModel().getSelectedItem();
            OperationResult result = controller.addEnrollment(student, course);
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshEnrollments();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        updateStatusBtn.setOnAction(e -> {
            Enrollment selected = enrollmentsTable.getSelectionModel().getSelectedItem();
            Enrollment.Status newStatus = statusCombo.getValue();
            OperationResult result = controller.updateEnrollmentStatus(selected, newStatus);
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshEnrollments();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Enrollment selected = enrollmentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select an enrollment to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete", "Delete this enrollment?")) {
                controller.deleteEnrollment(selected);
                refreshEnrollments();
            }
        });

        buttons.getChildren().addAll(addBtn, updateStatusBtn, deleteBtn);
        refreshEnrollments();

        content.getChildren().addAll(enrollmentsTable, form, buttons);
        VBox.setVgrow(enrollmentsTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private void refreshEnrollments() {
        enrollmentsTable.setItems(FXCollections.observableArrayList(controller.getAllEnrollments()));
    }

    // ==================== Grades Tab ====================

    private Tab createGradesTab() {
        Tab tab = new Tab("Grades");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        gradesTable = new TableView<>();
        gradesTable.setPlaceholder(new Label("No grades found."));

        TableColumn<Grade, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Grade, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentCol.setPrefWidth(140);

        TableColumn<Grade, String> codeCol = new TableColumn<>("Course");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        codeCol.setPrefWidth(90);

        TableColumn<Grade, Double> valueCol = new TableColumn<>("Grade (%)");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("gradeValue"));
        valueCol.setPrefWidth(80);

        TableColumn<Grade, String> letterCol = new TableColumn<>("Letter");
        letterCol.setCellValueFactory(new PropertyValueFactory<>("gradeLetter"));
        letterCol.setPrefWidth(60);

        TableColumn<Grade, String> feedbackCol = new TableColumn<>("Feedback");
        feedbackCol.setCellValueFactory(new PropertyValueFactory<>("feedback"));
        feedbackCol.setPrefWidth(190);

        TableColumn<Grade, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("gradedDate"));
        dateCol.setPrefWidth(100);

        gradesTable.getColumns().addAll(idCol, studentCol, codeCol, valueCol, letterCol, feedbackCol, dateCol);

        // Form for grade assignment
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        ComboBox<Enrollment> enrollmentCombo = new ComboBox<>();
        enrollmentCombo.setPromptText("Select Enrollment");
        enrollmentCombo.setItems(FXCollections.observableArrayList(controller.getAllEnrollments()));
        enrollmentCombo.setPrefWidth(300);

        TextField gradeField = new TextField();
        gradeField.setPromptText("Grade (0-100)");

        TextArea feedbackArea = new TextArea();
        feedbackArea.setPromptText("Feedback (optional)");
        feedbackArea.setPrefRowCount(2);
        feedbackArea.setMaxWidth(400);

        form.addRow(0, new Label("Enrollment:"), enrollmentCombo);
        form.addRow(1, new Label("Grade (%):"), gradeField);
        form.addRow(2, new Label("Feedback:"), feedbackArea);

        // Fill form on table selection
        gradesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                gradeField.setText(String.valueOf(newVal.getGradeValue()));
                feedbackArea.setText(newVal.getFeedback() != null ? newVal.getFeedback() : "");
            }
        });

        HBox buttons = new HBox(10);
        Button addBtn = new Button("Assign Grade");
        addBtn.getStyleClass().add("success-button");
        Button updateBtn = new Button("Update Grade");
        updateBtn.getStyleClass().add("primary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        Button clearBtn = new Button("Clear");

        addBtn.setOnAction(e -> {
            Enrollment enrollment = enrollmentCombo.getSelectionModel().getSelectedItem();
            OperationResult result = controller.addGrade(
                enrollment, gradeField.getText(), feedbackArea.getText()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshGrades();
                clearGradeForm(gradeField, feedbackArea, enrollmentCombo);
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        updateBtn.setOnAction(e -> {
            Grade selected = gradesTable.getSelectionModel().getSelectedItem();
            OperationResult result = controller.updateGrade(
                selected, gradeField.getText(), feedbackArea.getText()
            );
            if (result.isSuccess()) {
                AlertUtil.showInfo("Success", result.getMessage());
                refreshGrades();
            } else {
                AlertUtil.showError("Error", result.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            Grade selected = gradesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a grade to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete", "Delete this grade?")) {
                controller.deleteGrade(selected);
                refreshGrades();
                clearGradeForm(gradeField, feedbackArea, enrollmentCombo);
            }
        });

        clearBtn.setOnAction(e -> clearGradeForm(gradeField, feedbackArea, enrollmentCombo));

        buttons.getChildren().addAll(addBtn, updateBtn, deleteBtn, clearBtn);
        refreshGrades();

        content.getChildren().addAll(gradesTable, form, buttons);
        VBox.setVgrow(gradesTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private void clearGradeForm(TextField gradeField, TextArea feedbackArea,
                                 ComboBox<Enrollment> enrollCombo) {
        gradeField.clear();
        feedbackArea.clear();
        enrollCombo.getSelectionModel().clearSelection();
        gradesTable.getSelectionModel().clearSelection();
    }

    private void refreshGrades() {
        gradesTable.setItems(FXCollections.observableArrayList(controller.getAllGrades()));
    }

    // ==================== Reports Tab ====================

    /** Creates the reports tab with report type selection, dynamic filters, and text output */
    private Tab createReportsTab() {
        Tab tab = new Tab("Reports");

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Text reportTitle = new Text("Academic Reports");
        reportTitle.getStyleClass().add("section-title");

        // Report type selection
        HBox reportSelector = new HBox(10);
        reportSelector.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.setItems(FXCollections.observableArrayList(
            "Average Grade by Course",
            "Grade Distribution by Course",
            "Enrollment Statistics",
            "Grades by Date Range",
            "Student Transcript"
        ));
        reportTypeCombo.setPromptText("Select Report Type");
        reportSelector.getChildren().addAll(new Label("Report Type:"), reportTypeCombo);

        // Dynamic filter area (populated based on selected report type)
        VBox filterBox = new VBox(10);
        filterBox.setPadding(new Insets(5));

        // Report output display
        TextArea reportOutput = new TextArea();
        reportOutput.setEditable(false);
        reportOutput.setPrefRowCount(20);
        reportOutput.getStyleClass().add("report-output");

        Button generateBtn = new Button("Generate Report");
        generateBtn.getStyleClass().add("primary-button");

        // Update filter controls when report type changes
        reportTypeCombo.setOnAction(e -> {
            filterBox.getChildren().clear();
            String selected = reportTypeCombo.getValue();
            if (selected == null) return;

            switch (selected) {
                case "Grade Distribution by Course" -> {
                    ComboBox<Course> courseCombo = new ComboBox<>();
                    courseCombo.setPromptText("Select Course");
                    courseCombo.setItems(FXCollections.observableArrayList(controller.getAllCourses()));
                    courseCombo.setId("courseFilter");
                    filterBox.getChildren().addAll(new Label("Filter by Course:"), courseCombo);
                }
                case "Grades by Date Range" -> {
                    DatePicker fromDate = new DatePicker(LocalDate.now().minusMonths(6));
                    fromDate.setId("fromDate");
                    DatePicker toDate = new DatePicker(LocalDate.now());
                    toDate.setId("toDate");
                    HBox dateRange = new HBox(10,
                        new Label("From:"), fromDate, new Label("To:"), toDate);
                    filterBox.getChildren().add(dateRange);
                }
                case "Student Transcript" -> {
                    ComboBox<Student> studentCombo = new ComboBox<>();
                    studentCombo.setPromptText("Select Student");
                    studentCombo.setItems(FXCollections.observableArrayList(controller.getAllStudents()));
                    studentCombo.setId("studentFilter");
                    filterBox.getChildren().addAll(new Label("Select Student:"), studentCombo);
                }
            }
        });

        generateBtn.setOnAction(e -> {
            String reportType = reportTypeCombo.getValue();
            if (reportType == null) {
                AlertUtil.showWarning("No Selection", "Please select a report type.");
                return;
            }
            generateReport(reportType, filterBox, reportOutput);
        });

        content.getChildren().addAll(reportTitle, reportSelector, filterBox, generateBtn, reportOutput);
        VBox.setVgrow(reportOutput, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    /** Generates the selected report and writes it to the output area */
    private void generateReport(String reportType, VBox filterBox, TextArea output) {
        // Extract filter values from the dynamic filter controls
        Course course = null;
        LocalDate fromDate = null;
        LocalDate toDate = null;
        Student student = null;

        if (controller.requiresCourseFilter(reportType)) {
            ComboBox<Course> courseCombo = findComboBox(filterBox, "courseFilter");
            if (courseCombo == null || courseCombo.getValue() == null) {
                AlertUtil.showWarning("Filter Required", "Please select a course.");
                return;
            }
            course = courseCombo.getValue();
        } else if (controller.requiresDateRangeFilter(reportType)) {
            DatePicker fromPicker = findDatePicker(filterBox, "fromDate");
            DatePicker toPicker = findDatePicker(filterBox, "toDate");
            if (fromPicker == null || toPicker == null ||
                fromPicker.getValue() == null || toPicker.getValue() == null) {
                AlertUtil.showWarning("Filter Required", "Please select a date range.");
                return;
            }
            fromDate = fromPicker.getValue();
            toDate = toPicker.getValue();
        } else if (controller.requiresStudentFilter(reportType)) {
            ComboBox<Student> studentCombo = findComboBox(filterBox, "studentFilter");
            if (studentCombo == null || studentCombo.getValue() == null) {
                AlertUtil.showWarning("Filter Required", "Please select a student.");
                return;
            }
            student = studentCombo.getValue();
        }

        String reportText = controller.generateReport(reportType, course, fromDate, toDate, student);
        if (reportText != null) {
            output.setText(reportText);
        }
    }

    // ==================== Utility Methods ====================

    /** Helper: Finds a ComboBox by its fx:id within the filter box */
    @SuppressWarnings("unchecked")
    private <T> ComboBox<T> findComboBox(VBox filterBox, String id) {
        for (Node node : filterBox.getChildren()) {
            if (node instanceof ComboBox<?> && id.equals(node.getId())) {
                return (ComboBox<T>) node;
            }
        }
        return null;
    }

    /** Helper: Finds a DatePicker by its fx:id within nested HBox in filter box */
    private DatePicker findDatePicker(VBox filterBox, String id) {
        for (Node node : filterBox.getChildren()) {
            if (node instanceof HBox hbox) {
                for (Node child : hbox.getChildren()) {
                    if (child instanceof DatePicker && id.equals(child.getId())) {
                        return (DatePicker) child;
                    }
                }
            }
        }
        return null;
    }

    /** Clears the text in multiple TextFields */
    private void clearFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    /** Handles logout */
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
