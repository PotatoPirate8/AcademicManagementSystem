package com.academic.view;

import com.academic.dao.*;
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
import java.util.Map;

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
    private final UserDao userDao;
    private final StudentDao studentDao;
    private final LecturerDao lecturerDao;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeDao gradeDao;

    // Tables for each CRUD section
    private TableView<Student> studentsTable;
    private TableView<Lecturer> lecturersTable;
    private TableView<Course> coursesTable;
    private TableView<Enrollment> enrollmentsTable;
    private TableView<Grade> gradesTable;

    public AdminDashboardView(Stage stage) {
        this.stage = stage;
        this.root = new BorderPane();
        this.userDao = new UserDao();
        this.studentDao = new StudentDao();
        this.lecturerDao = new LecturerDao();
        this.courseDao = new CourseDao();
        this.enrollmentDao = new EnrollmentDao();
        this.gradeDao = new GradeDao();
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

        form.addRow(0, new Label("First Name:"), fnField, new Label("Last Name:"), lnField);
        form.addRow(1, new Label("Email:"), emailField, new Label("Student No:"), snField);
        form.addRow(2, new Label("Programme:"), progField);

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
        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("primary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("danger-button");
        Button clearBtn = new Button("Clear");

        updateBtn.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a student to update.");
                return;
            }
            if (!validateStudentForm(fnField, lnField, emailField, snField, progField)) return;

            selected.setFirstName(fnField.getText().trim());
            selected.setLastName(lnField.getText().trim());
            selected.setEmail(emailField.getText().trim());
            selected.setStudentNumber(snField.getText().trim().toUpperCase());
            selected.setProgramme(progField.getText().trim());

            if (studentDao.update(selected)) {
                AlertUtil.showInfo("Success", "Student updated successfully.");
                refreshStudents();
            } else {
                AlertUtil.showError("Error", "Failed to update student.");
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
                studentDao.delete(selected.getId());
                userDao.delete(selected.getUserId());
                refreshStudents();
                clearFields(fnField, lnField, emailField, snField, progField);
            }
        });

        clearBtn.setOnAction(e -> {
            clearFields(fnField, lnField, emailField, snField, progField);
            studentsTable.getSelectionModel().clearSelection();
        });

        buttons.getChildren().addAll(updateBtn, deleteBtn, clearBtn);
        refreshStudents();

        content.getChildren().addAll(studentsTable, form, buttons);
        VBox.setVgrow(studentsTable, Priority.ALWAYS);
        tab.setContent(content);
        return tab;
    }

    private boolean validateStudentForm(TextField fn, TextField ln, TextField email,
                                         TextField sn, TextField prog) {
        if (ValidationUtil.isNullOrEmpty(fn.getText()) || ValidationUtil.isNullOrEmpty(ln.getText())) {
            AlertUtil.showError("Validation Error", "Name fields cannot be empty.");
            return false;
        }
        if (!ValidationUtil.isValidEmail(email.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a valid email address.");
            return false;
        }
        if (!ValidationUtil.isValidStudentNumber(sn.getText())) {
            AlertUtil.showError("Validation Error", "Student number must be 4-12 alphanumeric characters.");
            return false;
        }
        if (ValidationUtil.isNullOrEmpty(prog.getText())) {
            AlertUtil.showError("Validation Error", "Programme cannot be empty.");
            return false;
        }
        return true;
    }

    private void refreshStudents() {
        studentsTable.setItems(FXCollections.observableArrayList(studentDao.findAll()));
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
            if (!validateLecturerForm(fnField, lnField, emailField, deptField)) return;
            Lecturer lecturer = new Lecturer(
                fnField.getText().trim(), lnField.getText().trim(),
                emailField.getText().trim(), deptField.getText().trim()
            );
            if (lecturerDao.create(lecturer) > 0) {
                AlertUtil.showInfo("Success", "Lecturer added successfully.");
                refreshLecturers();
                clearFields(fnField, lnField, emailField, deptField);
            } else {
                AlertUtil.showError("Error", "Failed to add lecturer. Email may already exist.");
            }
        });

        updateBtn.setOnAction(e -> {
            Lecturer selected = lecturersTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a lecturer to update.");
                return;
            }
            if (!validateLecturerForm(fnField, lnField, emailField, deptField)) return;
            selected.setFirstName(fnField.getText().trim());
            selected.setLastName(lnField.getText().trim());
            selected.setEmail(emailField.getText().trim());
            selected.setDepartment(deptField.getText().trim());
            if (lecturerDao.update(selected)) {
                AlertUtil.showInfo("Success", "Lecturer updated successfully.");
                refreshLecturers();
            } else {
                AlertUtil.showError("Error", "Failed to update lecturer.");
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
                lecturerDao.delete(selected.getId());
                refreshLecturers();
                clearFields(fnField, lnField, emailField, deptField);
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

    private boolean validateLecturerForm(TextField fn, TextField ln, TextField email, TextField dept) {
        if (ValidationUtil.isNullOrEmpty(fn.getText()) || ValidationUtil.isNullOrEmpty(ln.getText())) {
            AlertUtil.showError("Validation Error", "Name fields cannot be empty.");
            return false;
        }
        if (!ValidationUtil.isValidEmail(email.getText())) {
            AlertUtil.showError("Validation Error", "Please enter a valid email address.");
            return false;
        }
        if (ValidationUtil.isNullOrEmpty(dept.getText())) {
            AlertUtil.showError("Validation Error", "Department cannot be empty.");
            return false;
        }
        return true;
    }

    private void refreshLecturers() {
        lecturersTable.setItems(FXCollections.observableArrayList(lecturerDao.findAll()));
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
        lecturerCombo.setItems(FXCollections.observableArrayList(lecturerDao.findAll()));

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
            if (!validateCourseForm(codeField, nameField, creditsField, capacityField, lecturerCombo)) return;
            Course course = new Course(
                codeField.getText().trim().toUpperCase(),
                nameField.getText().trim(),
                ValidationUtil.parseIntSafe(creditsField.getText()),
                lecturerCombo.getSelectionModel().getSelectedItem().getId(),
                ValidationUtil.parseIntSafe(capacityField.getText())
            );
            if (courseDao.create(course) > 0) {
                AlertUtil.showInfo("Success", "Course added successfully.");
                refreshCourses();
                clearCourseForm(codeField, nameField, creditsField, capacityField, lecturerCombo);
            } else {
                AlertUtil.showError("Error", "Failed to add course. Code may already exist.");
            }
        });

        updateBtn.setOnAction(e -> {
            Course selected = coursesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a course to update.");
                return;
            }
            if (!validateCourseForm(codeField, nameField, creditsField, capacityField, lecturerCombo)) return;
            selected.setCourseCode(codeField.getText().trim().toUpperCase());
            selected.setCourseName(nameField.getText().trim());
            selected.setCredits(ValidationUtil.parseIntSafe(creditsField.getText()));
            selected.setMaxCapacity(ValidationUtil.parseIntSafe(capacityField.getText()));
            selected.setLecturerId(lecturerCombo.getSelectionModel().getSelectedItem().getId());
            if (courseDao.update(selected)) {
                AlertUtil.showInfo("Success", "Course updated successfully.");
                refreshCourses();
            } else {
                AlertUtil.showError("Error", "Failed to update course.");
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
                courseDao.delete(selected.getId());
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

    private boolean validateCourseForm(TextField code, TextField name, TextField credits,
                                        TextField capacity, ComboBox<Lecturer> lecturerCombo) {
        if (!ValidationUtil.isValidCourseCode(code.getText().trim().toUpperCase())) {
            AlertUtil.showError("Validation Error",
                "Course code must be 2-5 letters followed by 3-5 digits (e.g., COMP1322).");
            return false;
        }
        if (ValidationUtil.isNullOrEmpty(name.getText())) {
            AlertUtil.showError("Validation Error", "Course name cannot be empty.");
            return false;
        }
        int cred = ValidationUtil.parseIntSafe(credits.getText());
        if (!ValidationUtil.isPositiveInteger(cred)) {
            AlertUtil.showError("Validation Error", "Credits must be a positive number.");
            return false;
        }
        int cap = ValidationUtil.parseIntSafe(capacity.getText());
        if (!ValidationUtil.isPositiveInteger(cap)) {
            AlertUtil.showError("Validation Error", "Capacity must be a positive number.");
            return false;
        }
        if (lecturerCombo.getSelectionModel().getSelectedItem() == null) {
            AlertUtil.showError("Validation Error", "Please select a lecturer.");
            return false;
        }
        return true;
    }

    private void clearCourseForm(TextField code, TextField name, TextField credits,
                                  TextField capacity, ComboBox<Lecturer> lecturerCombo) {
        clearFields(code, name, credits, capacity);
        lecturerCombo.getSelectionModel().clearSelection();
        coursesTable.getSelectionModel().clearSelection();
    }

    private void refreshCourses() {
        coursesTable.setItems(FXCollections.observableArrayList(courseDao.findAll()));
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
        studentCombo.setItems(FXCollections.observableArrayList(studentDao.findAll()));

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setPromptText("Select Course");
        courseCombo.setItems(FXCollections.observableArrayList(courseDao.findAll()));

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
            if (student == null || course == null) {
                AlertUtil.showWarning("Validation Error", "Please select both a student and a course.");
                return;
            }
            if (enrollmentDao.isEnrolled(student.getId(), course.getId())) {
                AlertUtil.showWarning("Already Enrolled", "This student is already enrolled in this course.");
                return;
            }
            Enrollment enrollment = new Enrollment(
                student.getId(), course.getId(), LocalDate.now(), Enrollment.Status.ENROLLED
            );
            if (enrollmentDao.create(enrollment) > 0) {
                AlertUtil.showInfo("Success", "Enrollment created successfully.");
                refreshEnrollments();
            } else {
                AlertUtil.showError("Error", "Failed to create enrollment.");
            }
        });

        updateStatusBtn.setOnAction(e -> {
            Enrollment selected = enrollmentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select an enrollment to update.");
                return;
            }
            Enrollment.Status newStatus = statusCombo.getValue();
            if (newStatus == null) {
                AlertUtil.showWarning("Validation Error", "Please select a status.");
                return;
            }
            if (enrollmentDao.updateStatus(selected.getId(), newStatus)) {
                AlertUtil.showInfo("Success", "Enrollment status updated.");
                refreshEnrollments();
            } else {
                AlertUtil.showError("Error", "Failed to update status.");
            }
        });

        deleteBtn.setOnAction(e -> {
            Enrollment selected = enrollmentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select an enrollment to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete", "Delete this enrollment?")) {
                enrollmentDao.delete(selected.getId());
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
        enrollmentsTable.setItems(FXCollections.observableArrayList(enrollmentDao.findAll()));
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
        enrollmentCombo.setItems(FXCollections.observableArrayList(enrollmentDao.findAll()));
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
            if (enrollment == null) {
                AlertUtil.showWarning("Validation Error", "Please select an enrollment.");
                return;
            }
            double gradeValue = ValidationUtil.parseDoubleSafe(gradeField.getText());
            if (!ValidationUtil.isValidGrade(gradeValue)) {
                AlertUtil.showError("Validation Error", "Grade must be between 0 and 100.");
                return;
            }
            if (gradeDao.existsForEnrollment(enrollment.getId())) {
                AlertUtil.showWarning("Grade Exists",
                    "A grade already exists for this enrollment. Use 'Update Grade' instead.");
                return;
            }
            Grade grade = new Grade(
                enrollment.getId(), gradeValue,
                Grade.calculateGradeLetter(gradeValue),
                feedbackArea.getText().trim(),
                LocalDate.now()
            );
            if (gradeDao.create(grade) > 0) {
                AlertUtil.showInfo("Success", "Grade assigned successfully.");
                refreshGrades();
                clearGradeForm(gradeField, feedbackArea, enrollmentCombo);
            } else {
                AlertUtil.showError("Error", "Failed to assign grade.");
            }
        });

        updateBtn.setOnAction(e -> {
            Grade selected = gradesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a grade to update.");
                return;
            }
            double gradeValue = ValidationUtil.parseDoubleSafe(gradeField.getText());
            if (!ValidationUtil.isValidGrade(gradeValue)) {
                AlertUtil.showError("Validation Error", "Grade must be between 0 and 100.");
                return;
            }
            selected.setGradeValue(gradeValue);
            selected.setGradeLetter(Grade.calculateGradeLetter(gradeValue));
            selected.setFeedback(feedbackArea.getText().trim());
            selected.setGradedDate(LocalDate.now());
            if (gradeDao.update(selected)) {
                AlertUtil.showInfo("Success", "Grade updated successfully.");
                refreshGrades();
            } else {
                AlertUtil.showError("Error", "Failed to update grade.");
            }
        });

        deleteBtn.setOnAction(e -> {
            Grade selected = gradesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtil.showWarning("No Selection", "Please select a grade to delete.");
                return;
            }
            if (AlertUtil.showConfirmation("Confirm Delete", "Delete this grade?")) {
                gradeDao.delete(selected.getId());
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
        gradesTable.setItems(FXCollections.observableArrayList(gradeDao.findAll()));
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
                    courseCombo.setItems(FXCollections.observableArrayList(courseDao.findAll()));
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
                    studentCombo.setItems(FXCollections.observableArrayList(studentDao.findAll()));
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
        StringBuilder sb = new StringBuilder();
        sb.append("============================================\n");
        sb.append("  ").append(reportType.toUpperCase()).append("\n");
        sb.append("  Generated: ").append(LocalDate.now()).append("\n");
        sb.append("============================================\n\n");

        switch (reportType) {
            case "Average Grade by Course" -> generateAvgGradeReport(sb);
            case "Grade Distribution by Course" -> generateDistributionReport(sb, filterBox);
            case "Enrollment Statistics" -> generateEnrollmentReport(sb);
            case "Grades by Date Range" -> generateDateRangeReport(sb, filterBox);
            case "Student Transcript" -> generateTranscriptReport(sb, filterBox);
            default -> sb.append("Unknown report type.\n");
        }

        output.setText(sb.toString());
    }

    /** Report: Average grade per course */
    private void generateAvgGradeReport(StringBuilder sb) {
        Map<String, Double> averages = gradeDao.getAverageGradeByCourse();
        if (averages.isEmpty()) {
            sb.append("No grade data available.\n");
            return;
        }
        sb.append(String.format("%-35s  %s\n", "Course", "Average Grade"));
        sb.append("----------------------------------------------\n");
        for (Map.Entry<String, Double> entry : averages.entrySet()) {
            sb.append(String.format("%-35s  %.1f (%s)\n",
                entry.getKey(), entry.getValue(),
                Grade.calculateGradeLetter(entry.getValue())));
        }
    }

    /** Report: Grade distribution for a selected course */
    private void generateDistributionReport(StringBuilder sb, VBox filterBox) {
        ComboBox<Course> courseCombo = findComboBox(filterBox, "courseFilter");
        if (courseCombo == null || courseCombo.getValue() == null) {
            AlertUtil.showWarning("Filter Required", "Please select a course.");
            return;
        }
        Course course = courseCombo.getValue();
        Map<String, Integer> distribution = gradeDao.getGradeDistribution(course.getId());
        Map<String, Object> stats = gradeDao.getCourseGradeStats(course.getId());

        sb.append("Course: ").append(course.getCourseCode())
          .append(" - ").append(course.getCourseName()).append("\n\n");

        sb.append("Grade Distribution:\n");
        sb.append("----------------------\n");
        for (String letter : new String[]{"A", "B", "C", "D", "F"}) {
            int count = distribution.getOrDefault(letter, 0);
            String bar = "#".repeat(count);
            sb.append(String.format("  %s: %d %s\n", letter, count, bar));
        }

        sb.append("\nStatistics:\n");
        sb.append("----------------------\n");
        sb.append(String.format("  Total Graded:  %d\n", stats.getOrDefault("total", 0)));
        sb.append(String.format("  Average:       %.1f\n", stats.getOrDefault("average", 0.0)));
        sb.append(String.format("  Maximum:       %.1f\n", stats.getOrDefault("maximum", 0.0)));
        sb.append(String.format("  Minimum:       %.1f\n", stats.getOrDefault("minimum", 0.0)));
    }

    /** Report: Enrollment counts by status and per course */
    private void generateEnrollmentReport(StringBuilder sb) {
        Map<String, Integer> statusCounts = gradeDao.getEnrollmentStatusCounts();
        sb.append("Enrollment Status Summary:\n");
        sb.append("--------------------------\n");
        int total = 0;
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            sb.append(String.format("  %-12s: %d\n", entry.getKey(), entry.getValue()));
            total += entry.getValue();
        }
        sb.append(String.format("\n  Total Enrollments: %d\n", total));

        sb.append("\nEnrollments per Course:\n");
        sb.append("--------------------------------------\n");
        List<Course> courses = courseDao.findAll();
        for (Course course : courses) {
            int count = courseDao.getEnrollmentCount(course.getId());
            sb.append(String.format("  %-30s: %d / %d\n",
                course.getCourseCode() + " - " + course.getCourseName(),
                count, course.getMaxCapacity()));
        }
    }

    /** Report: Grades filtered by date range */
    private void generateDateRangeReport(StringBuilder sb, VBox filterBox) {
        DatePicker fromPicker = findDatePicker(filterBox, "fromDate");
        DatePicker toPicker = findDatePicker(filterBox, "toDate");
        if (fromPicker == null || toPicker == null ||
            fromPicker.getValue() == null || toPicker.getValue() == null) {
            AlertUtil.showWarning("Filter Required", "Please select a date range.");
            return;
        }

        List<Grade> grades = gradeDao.findByDateRange(fromPicker.getValue(), toPicker.getValue());
        sb.append(String.format("Period: %s to %s\n\n", fromPicker.getValue(), toPicker.getValue()));

        if (grades.isEmpty()) {
            sb.append("No grades found for the selected period.\n");
            return;
        }

        sb.append(String.format("%-18s %-10s %-6s %-6s %s\n",
            "Student", "Course", "Grade", "Letter", "Date"));
        sb.append("--------------------------------------------------------\n");
        for (Grade grade : grades) {
            sb.append(String.format("%-18s %-10s %-6.1f %-6s %s\n",
                grade.getStudentName(), grade.getCourseCode(),
                grade.getGradeValue(), grade.getGradeLetter(),
                grade.getGradedDate()));
        }
        sb.append(String.format("\nTotal Records: %d\n", grades.size()));
    }

    /** Report: Full transcript for a selected student */
    private void generateTranscriptReport(StringBuilder sb, VBox filterBox) {
        ComboBox<Student> studentCombo = findComboBox(filterBox, "studentFilter");
        if (studentCombo == null || studentCombo.getValue() == null) {
            AlertUtil.showWarning("Filter Required", "Please select a student.");
            return;
        }
        Student student = studentCombo.getValue();
        List<Grade> grades = gradeDao.findByStudentId(student.getId());

        sb.append("Student: ").append(student.getFullName()).append("\n");
        sb.append("Student Number: ").append(student.getStudentNumber()).append("\n");
        sb.append("Programme: ").append(student.getProgramme()).append("\n\n");

        if (grades.isEmpty()) {
            sb.append("No grades recorded for this student.\n");
            return;
        }

        sb.append(String.format("%-10s %-25s %-8s %-6s %s\n",
            "Code", "Course Name", "Grade", "Letter", "Feedback"));
        sb.append("--------------------------------------------------------------\n");
        double totalGrade = 0;
        for (Grade grade : grades) {
            sb.append(String.format("%-10s %-25s %-8.1f %-6s %s\n",
                grade.getCourseCode(), grade.getCourseName(),
                grade.getGradeValue(), grade.getGradeLetter(),
                grade.getFeedback() != null ? grade.getFeedback() : ""));
            totalGrade += grade.getGradeValue();
        }
        double average = totalGrade / grades.size();
        sb.append("\n--------------------------------------------------------------\n");
        sb.append(String.format("Overall Average: %.1f (%s)\n", average,
            Grade.calculateGradeLetter(average)));
        sb.append(String.format("Total Courses Graded: %d\n", grades.size()));
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
        SessionManager.logout();
        LoginView loginView = new LoginView(stage);
        Scene scene = new Scene(loginView.getRoot(), 400, 550);
        scene.getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );
        stage.setTitle("Academic Management System");
        stage.setScene(scene);
    }
}
