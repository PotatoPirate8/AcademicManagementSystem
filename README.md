# Academic Management System

A JavaFX desktop application for managing students, lecturers, courses, enrollments, and grades. Built with Java 23, SQLite, and Maven following the Model-View-Controller (MVC) architecture.

---

## Table of Contents

1. [Technologies](#technologies)
2. [Project Structure](#project-structure)
3. [Architecture](#architecture-mvc)
4. [Database Schema](#database-schema)
5. [Authentication & Security](#authentication--security)
6. [Features: Student Dashboard](#features--student-dashboard)
7. [Features: Admin Dashboard](#features--admin-dashboard)
8. [Validation Rules](#validation-rules)
9. [Grade Scale](#grade-scale)
10. [Prerequisites](#prerequisites)
11. [Building and Running](#building-and-running)
12. [Testing](#testing)
13. [Default Accounts & Sample Data](#default-accounts--sample-data)
14. [Design Decisions](#design-decisions)
15. [Project TODO Backlog](#project-todo-backlog)

---

## Technologies

| Component        | Technology              | Version  |
|------------------|-------------------------|----------|
| Language         | Java                    | 23       |
| UI Framework     | JavaFX                  | 23.0.1   |
| Database         | SQLite (sqlite-jdbc)    | 3.44.1.0 |
| Testing          | JUnit Jupiter           | 5.10.1   |
| Build Tool       | Apache Maven (Wrapper)  | 3.9.6    |
| Compiler Plugin  | maven-compiler-plugin   | 3.12.1   |
| Packaging        | maven-shade-plugin      | 3.5.1    |

---

## Project TODO Backlog

See [TODO.md](TODO.md) for the ranked and tagged roadmap.

---

## Project Structure

```
AcademicManagementSystem/
├── pom.xml                          # Maven build configuration
├── mvnw.cmd                         # Maven Wrapper (no Maven install needed)
├── .mvn/wrapper/                    # Maven Wrapper support files
└── src/
    ├── main/
    │   ├── java/com/academic/
    │   │   ├── App.java             # JavaFX Application entry point
    │   │   ├── Launcher.java        # Fat JAR launcher (bypasses JavaFX module restriction)
    │   │   ├── controller/          # Controllers (business logic + validation)
    │   │   │   ├── LoginController.java
    │   │   │   ├── StudentController.java
    │   │   │   └── AdminController.java
    │   │   ├── model/               # Data models (POJOs)
    │   │   │   ├── User.java        # User account (with Role enum: STUDENT, ADMIN)
    │   │   │   ├── Student.java     # Student profile (linked to User)
    │   │   │   ├── Lecturer.java    # Lecturer record
    │   │   │   ├── Course.java      # Course with capacity and lecturer assignment
    │   │   │   ├── Enrollment.java  # Student-Course relationship (with Status enum)
    │   │   │   └── Grade.java       # Grade with auto letter-grade calculation
    │   │   ├── dao/                 # Data Access Objects (SQLite operations)
    │   │   │   ├── DatabaseManager.java  # Singleton DB connection + schema init
    │   │   │   ├── UserDao.java
    │   │   │   ├── StudentDao.java
    │   │   │   ├── LecturerDao.java
    │   │   │   ├── CourseDao.java
    │   │   │   ├── EnrollmentDao.java
    │   │   │   └── GradeDao.java
    │   │   ├── view/                # JavaFX views (programmatic UI, no FXML)
    │   │   │   ├── LoginView.java
    │   │   │   ├── StudentDashboardView.java
    │   │   │   └── AdminDashboardView.java
    │   │   └── util/                # Utility classes
    │   │       ├── ValidationUtil.java   # Input validation with regex patterns
    │   │       ├── PasswordUtil.java     # SHA-256 password hashing
    │   │       ├── SessionManager.java   # Static session state management
    │   │       └── AlertUtil.java        # JavaFX dialog helpers
    │   └── resources/
    │       └── css/style.css        # Application stylesheet
    └── test/java/com/academic/      # JUnit 5 tests
        ├── model/ModelTest.java
        ├── util/ValidationUtilTest.java
        ├── util/PasswordUtilTest.java
        └── dao/
            ├── UserDaoTest.java
            ├── StudentDaoTest.java
            ├── CourseDaoTest.java
            └── GradeDaoTest.java
```

---

## Architecture (MVC)

The application follows a strict **Model-View-Controller** pattern with an additional **DAO** (Data Access Object) layer for database access:

```
┌──────────────────────────────────────────────────────────┐
│  View (JavaFX UI)                                        │
│  LoginView / StudentDashboardView / AdminDashboardView   │
│  - Builds UI components programmatically (no FXML)       │
│  - Delegates ALL actions to Controllers                  │
│  - Displays results and error messages via AlertUtil     │
└─────────────────────┬────────────────────────────────────┘
                      │ calls
┌─────────────────────▼────────────────────────────────────┐
│  Controller (Business Logic)                             │
│  LoginController / StudentController / AdminController   │
│  - Validates inputs using ValidationUtil                 │
│  - Enforces business rules (capacity, duplicates, etc.)  │
│  - Returns OperationResult (success/failure + message)   │
│  - Coordinates multiple DAO calls                        │
└─────────────────────┬────────────────────────────────────┘
                      │ calls
┌─────────────────────▼────────────────────────────────────┐
│  DAO (Data Access Objects)                               │
│  UserDao / StudentDao / LecturerDao / CourseDao /        │
│  EnrollmentDao / GradeDao                                │
│  - Executes parameterised SQL via PreparedStatement      │
│  - Maps ResultSet rows to Model objects                  │
│  - Uses DatabaseManager singleton for connection         │
└─────────────────────┬────────────────────────────────────┘
                      │ reads/writes
┌─────────────────────▼────────────────────────────────────┐
│  Model (POJOs)                                           │
│  User / Student / Lecturer / Course / Enrollment / Grade │
│  - Plain Java objects with getters/setters               │
│  - Enums: User.Role, Enrollment.Status                   │
│  - Grade.calculateGradeLetter() static helper            │
└──────────────────────────────────────────────────────────┘
```

### Data Flow Example: Student Enrolment

1. **StudentDashboardView**: User selects a course and clicks "Enroll"
2. **StudentController.enroll()**: Validates the selection, checks `EnrollmentDao.isEnrolled()` for duplicates, checks `CourseDao.getEnrollmentCount()` against `course.getMaxCapacity()`
3. **EnrollmentDao.create()**: Inserts a new row into the `enrollments` table with status `ENROLLED`
4. **View**: Receives `OperationResult`, shows success/error alert, refreshes the table

### Key Design Principles

- **Views never access DAOs directly**: all database operations go through Controllers
- **Controllers return `OperationResult`**: a simple success/failure object with a user-facing message, keeping the View free of business logic
- **All SQL uses `PreparedStatement`**: preventing SQL injection
- **Singleton `DatabaseManager`**: ensures a single shared connection throughout the application lifecycle

---

## Database Schema

The application uses a file-based SQLite database (`academic_system.db`), created automatically on first run. All tables are created via `CREATE TABLE IF NOT EXISTS` in `DatabaseManager.initializeSchema()`.

### Entity-Relationship Overview

```
users 1──1 students ──┐
                      ├── enrollments 1──1 grades
lecturers 1──* courses ─┘
```

### Table Definitions

#### `users`: Authentication accounts

| Column         | Type    | Constraints                              |
|----------------|---------|------------------------------------------|
| `id`           | INTEGER | PRIMARY KEY AUTOINCREMENT                |
| `username`     | TEXT    | UNIQUE NOT NULL                          |
| `password_hash`| TEXT    | NOT NULL (SHA-256 hash)                  |
| `role`         | TEXT    | NOT NULL, CHECK(`STUDENT` or `ADMIN`)    |

#### `students`: Student profiles

| Column           | Type    | Constraints                              |
|------------------|---------|------------------------------------------|
| `id`             | INTEGER | PRIMARY KEY AUTOINCREMENT                |
| `user_id`        | INTEGER | UNIQUE NOT NULL, FK → `users.id`         |
| `first_name`     | TEXT    | NOT NULL                                 |
| `last_name`      | TEXT    | NOT NULL                                 |
| `email`          | TEXT    | NOT NULL                                 |
| `student_number` | TEXT    | UNIQUE NOT NULL                          |
| `programme`      | TEXT    | NOT NULL                                 |

#### `lecturers`: Lecturer records

| Column       | Type    | Constraints                              |
|--------------|---------|------------------------------------------|
| `id`         | INTEGER | PRIMARY KEY AUTOINCREMENT                |
| `first_name` | TEXT    | NOT NULL                                 |
| `last_name`  | TEXT    | NOT NULL                                 |
| `email`      | TEXT    | UNIQUE NOT NULL                          |
| `department` | TEXT    | NOT NULL                                 |

#### `courses`: Course catalogue

| Column         | Type    | Constraints                              |
|----------------|---------|------------------------------------------|
| `id`           | INTEGER | PRIMARY KEY AUTOINCREMENT                |
| `course_code`  | TEXT    | UNIQUE NOT NULL                          |
| `course_name`  | TEXT    | NOT NULL                                 |
| `credits`      | INTEGER | NOT NULL                                 |
| `lecturer_id`  | INTEGER | FK → `lecturers.id`                      |
| `max_capacity` | INTEGER | NOT NULL                                 |

#### `enrollments`: Student-Course relationships

| Column            | Type    | Constraints                                           |
|-------------------|---------|-------------------------------------------------------|
| `id`              | INTEGER | PRIMARY KEY AUTOINCREMENT                             |
| `student_id`      | INTEGER | NOT NULL, FK → `students.id`                          |
| `course_id`       | INTEGER | NOT NULL, FK → `courses.id`                           |
| `enrollment_date` | TEXT    | NOT NULL (ISO date string)                            |
| `status`          | TEXT    | NOT NULL, CHECK(`ENROLLED`, `COMPLETED`, `WITHDRAWN`) |

- **UNIQUE constraint** on (`student_id`, `course_id`): a student can only have one enrollment per course.

#### `grades`: Assessment results

| Column          | Type    | Constraints                              |
|-----------------|---------|------------------------------------------|
| `id`            | INTEGER | PRIMARY KEY AUTOINCREMENT                |
| `enrollment_id` | INTEGER | UNIQUE NOT NULL, FK → `enrollments.id`   |
| `grade_value`   | REAL    | NOT NULL, CHECK(0 ≤ value ≤ 100)         |
| `grade_letter`  | TEXT    | NOT NULL                                 |
| `feedback`      | TEXT    |                                          |
| `graded_date`   | TEXT    | NOT NULL (ISO date string)               |

- **UNIQUE constraint** on `enrollment_id`: each enrollment has at most one grade.

---

## Authentication & Security

### Password Hashing

Passwords are hashed using **SHA-256** via `PasswordUtil`:

- `hashPassword(String password)` → returns the hex-encoded SHA-256 digest
- `verifyPassword(String password, String storedHash)` → hashes the input and compares with the stored hash

Plaintext passwords are **never stored** in the database.

### Login Flow

1. User enters username and password on `LoginView`
2. `LoginController.login()` calls `UserDao.authenticate(username, hashedPassword)`
3. On success, `SessionManager` stores the current `User` (and `Student` if the role is `STUDENT`)
4. The view navigates to `AdminDashboardView` (for admins) or `StudentDashboardView` (for students)

### Registration Flow

1. User clicks "Register" on `LoginView` and fills in all fields
2. `LoginController.register()` validates all fields, checks for duplicate username
3. Creates a `User` record, then a linked `Student` record
4. On `Student` creation failure, the `User` record is rolled back (deleted)

### Session Management

`SessionManager` is a static utility that holds:
- `currentUser`: the authenticated `User` object
- `currentStudent`: the `Student` profile (null for admin users)
- `logout()`: clears both fields

---

## Features: Student Dashboard

The student dashboard provides four tabs. Full **CRUD** operations are available for the student's own enrollment data:

### My Courses Tab (Read, Update, Delete)

| Operation | Description |
|-----------|-------------|
| **Read**  | Displays a table of the student's enrollments with course code, course name, enrollment date, and status |
| **Update (Withdraw)** | Changes enrollment status from `ENROLLED` to `WITHDRAWN`. Only active enrollments can be withdrawn |
| **Delete** | Permanently removes a non-active enrollment record (must be `WITHDRAWN` or `COMPLETED` first) |

### Available Courses Tab (Create)

| Operation | Description |
|-----------|-------------|
| **Read**  | Displays all courses with code, name, credits, lecturer, and capacity |
| **Create (Enroll)** | Creates a new enrollment for the selected course. Checks for: duplicate enrollment, course capacity |

### My Grades Tab (Read)

- Displays grades with course code, course name, grade value (0–100), letter grade, feedback, and graded date
- Grades are assigned by admins and are read-only for students

### My Profile Tab (Update)

- Displays and allows editing of: first name, last name, email, and programme
- Student number is displayed but is read-only
- Input validation is enforced before saving

### CRUD Coverage for All Entities (System-wide)

| Entity     | Student Role | Admin Role |
|------------|--------------|------------|
| User Account | ❌ | Create / Read / Update password / Delete |
| Student Profile | Read own / Update own | Create / Read / Update / Delete |
| Lecturer | ❌ | Create / Read / Update / Delete |
| Course | Read | Create / Read / Update / Delete |
| Enrollment | Create / Read own / Update (withdraw) / Delete non-active own | Create / Read / Update / Delete |
| Grade | Read own | Create / Read / Update / Delete |

---

## Features: Admin Dashboard

The admin dashboard provides six tabs with full CRUD operations on all entities:

### Students Tab

| Operation | Description |
|-----------|-------------|
| **Create (Add)** | Creates a new user account + student profile. Requires: username, password, first name, last name, email, student number, programme. Validates all fields and checks for duplicate usernames/student numbers. On failure, rolls back the user creation |
| **Read** | Displays all students in a table with ID, name, email, student number, and programme |
| **Update** | Edits the selected student's name, email, student number, and programme |
| **Delete** | Removes the student record and associated user account |

### Lecturers Tab

| Operation | Description |
|-----------|-------------|
| **Create** | Adds a new lecturer with first name, last name, email, and department |
| **Read** | Displays all lecturers in a table |
| **Update** | Edits the selected lecturer's details |
| **Delete** | Removes the lecturer record |

### Courses Tab

| Operation | Description |
|-----------|-------------|
| **Create** | Adds a new course with code, name, credits, lecturer (dropdown), and max capacity |
| **Read** | Displays all courses with lecturer names |
| **Update** | Edits the selected course's details including lecturer reassignment |
| **Delete** | Removes the course record |

### Enrollments Tab

| Operation | Description |
|-----------|-------------|
| **Create** | Enrolls a student in a course. Uses **bidirectional filtering**: selecting a student filters courses to show that student's enrolled courses, and selecting a course filters students to show those enrolled in that course. A "Clear" button resets both filters |
| **Read** | Displays all enrollments with student name, course code, date, and status |
| **Update** | Changes enrollment status (`ENROLLED` / `WITHDRAWN` / `COMPLETED`) |
| **Delete** | Removes the enrollment record |

### Grades Tab

| Operation | Description |
|-----------|-------------|
| **Create** | Assigns a grade (0–100) to an enrollment. Letter grade is auto-calculated. Feedback is optional. Prevents duplicate grades per enrollment |
| **Read** | Displays all grades with student name, course, value, letter, feedback, and date |
| **Update** | Edits the grade value, feedback, and recalculates the letter grade |
| **Delete** | Removes the grade record |

### Reports Tab

Five report types are available, each with contextual filters:

| Report | Filter Required | Description |
|--------|----------------|-------------|
| **Average Grade by Course** | None | Lists each course with its average grade and letter equivalent |
| **Grade Distribution by Course** | Course dropdown | Shows A/B/C/D/F distribution as a histogram, plus statistics (total, average, max, min) |
| **Enrollment Statistics** | None | Summary of enrollment statuses (Enrolled/Completed/Withdrawn) plus per-course enrollment vs capacity |
| **Grades by Date Range** | From/To date pickers | Lists all grades within the selected period with student, course, grade, and date |
| **Student Transcript** | Student dropdown | Full transcript for a student: name, student number, programme, all course grades, and GPA |

---

## Validation Rules

All user input is validated via `ValidationUtil` before reaching the database:

| Field | Rule | Regex / Check |
|-------|------|---------------|
| Username | 3–20 alphanumeric characters | `^[a-zA-Z0-9]{3,20}$` |
| Password | At least 6 characters | Length ≥ 6 |
| Email | Standard email format | `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$` |
| Student Number | 4–12 alphanumeric characters | `^[A-Za-z0-9]{4,12}$` |
| Course Code | 2–5 letters followed by 3–5 digits | `^[A-Za-z]{2,5}\\d{3,5}$` |
| Grade Value | Numeric, 0 ≤ value ≤ 100 | `isValidGrade()` |
| Credits / Capacity | Positive integer | `isPositiveInteger()` |
| Name / Programme / Department | Non-empty after trim | `isNullOrEmpty()` |

Utility parsing methods (`parseIntSafe`, `parseDoubleSafe`) return `-1` on invalid input rather than throwing exceptions.

---

## Grade Scale

Letter grades are automatically calculated by `Grade.calculateGradeLetter()`:

| Letter | Range   |
|--------|---------|
| A      | 70–100  |
| B      | 60–69   |
| C      | 50–59   |
| D      | 40–49   |
| F      | 0–39    |

---

## Prerequisites

- **Java 23** (JDK) installed and available on PATH

Maven is **not** required: the included Maven Wrapper (`mvnw.cmd`) downloads it automatically.

---

## Building and Running

All commands should be run from the `AcademicManagementSystem/` directory.

### Set JAVA_HOME (if not already set)

```powershell
$env:JAVA_HOME = "<path-to-your-jdk-23>"
```

To set it permanently:

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "<path-to-your-jdk-23>", "User")
```

### Compile

```powershell
.\mvnw.cmd compile
```

### Run the application (via Maven)

```powershell
.\mvnw.cmd javafx:run
```

### Build a portable fat JAR

```powershell
.\mvnw.cmd package -DskipTests
```

This produces `target/academic-management-system-1.0-SNAPSHOT.jar` (~22 MB) containing all dependencies. The fat JAR is built using `maven-shade-plugin` with `Launcher` as the main class (a non-JavaFX class that calls `App.main()` to bypass JavaFX module restrictions). Run it on any machine with Java 23:

```
java -jar academic-management-system-1.0-SNAPSHOT.jar
```

---

## Testing

The test suite uses **JUnit 5** and covers models, utilities, and DAO layers:

```powershell
.\mvnw.cmd test
```

### Test Classes

| Test Class | Layer | What It Tests |
|------------|-------|---------------|
| `ModelTest` | Model | Constructors, getters/setters, `toString()`, and enum values for all 6 model classes |
| `ValidationUtilTest` | Util | Email, username, password, student number, course code validation; edge cases for parsing and null/empty checks |
| `PasswordUtilTest` | Util | SHA-256 hashing, hash consistency, password verification, empty/null input handling |
| `UserDaoTest` | DAO | User CRUD: create, authenticate, find by ID/username, update, delete |
| `StudentDaoTest` | DAO | Student CRUD: create, find by ID/user ID, find all, update, delete |
| `CourseDaoTest` | DAO | Course CRUD: create, find by ID, find all, update, delete, enrollment count |
| `GradeDaoTest` | DAO | Grade CRUD: create, find all, find by student, update, delete; report queries (averages, distribution, statistics, date range, transcript) |

DAO tests use `DatabaseManager` with an in-memory SQLite database to isolate tests from production data.

---

## Default Accounts & Sample Data

### Login Credentials

| Role    | Username | Password     |
|---------|----------|--------------|
| Admin   | admin    | admin123     |
| Student | jdoe     | password123  |
| Student | asmith   | password123  |
| Student | bwilson  | password123  |
| Student | clee     | password123  |
| Student | dpatel   | password123  |

New students can also self-register through the login screen.

### Seeded Data (created on first run)

The database is automatically populated with sample data when tables are empty:

| Entity | Count | Details |
|--------|-------|---------|
| **Lecturers** | 4 | James Smith (Computer Science), Sarah Johnson (Computer Science), Michael Brown (Mathematics), Emily Davis (Electronics) |
| **Students** | 5 | John Doe, Alice Smith, Ben Wilson, Clara Lee, Dev Patel: all in Computer Science programme |
| **Courses** | 5 | COMP1322 (Databases, 15 credits, cap 50), COMP1206 (Programming, 15 credits, cap 60), COMP1216 (Software Modelling, 15 credits, cap 45), MATH1060 (Calculus, 15 credits, cap 100), ELEC1201 (Digital Systems, 15 credits, cap 40) |
| **Enrollments** | 12 | Mix of `ENROLLED`, `COMPLETED`, and `WITHDRAWN` statuses across students and courses |
| **Grades** | 5 | Assigned to completed enrollments with values ranging from 45 to 82, with feedback |

---

## Design Decisions

### Programmatic UI (No FXML)

All views are built programmatically in Java rather than using FXML files. This keeps the entire UI definition alongside its event-handling logic and avoids the need for a separate FXML editor or Scene Builder.

### Launcher Class for Fat JAR

JavaFX applications cannot be launched directly from a shaded JAR because the `javafx-maven-plugin` expects the main class to extend `Application`. The `Launcher` class is a plain Java class that simply calls `App.main()`, bypassing this restriction.

### OperationResult Pattern

Controllers return `OperationResult` objects instead of throwing exceptions or returning raw booleans. Each result carries a `success` flag and a user-facing `message`, allowing the View to display feedback consistently without parsing error types.

### Bidirectional Enrollment Filtering

The admin enrollment form uses bidirectional combo-box filtering: selecting a student filters the course dropdown to show only that student's enrolled courses, and vice versa. A `boolean[]` flag prevents infinite recursion between the two listeners. A "Clear" button resets both dropdowns.

### Singleton DatabaseManager

A single `DatabaseManager` instance manages the SQLite connection throughout the application lifecycle. The schema is initialised with `CREATE TABLE IF NOT EXISTS`, making the application self-bootstrapping: no manual database setup is needed.

### CSS Theming

The application uses a custom CSS stylesheet (`style.css`) with a consistent colour scheme:
- **Primary colour**: `#3498db` (blue) for buttons, selected tabs, and focused inputs
- **Success colour**: `#27ae60` (green) for action buttons
- **Danger colour**: `#e74c3c` (red) for delete buttons and error text
- **Dark header**: `#2c3e50` for the navigation bar and titles
- **Font**: Segoe UI with Consolas for report output
