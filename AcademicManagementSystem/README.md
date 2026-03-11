# Academic Management System

A JavaFX desktop application for managing students, lecturers, courses, enrollments, and grades. Built with Java 23, SQLite, and Maven.

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
    │   │   │   ├── User.java
    │   │   │   ├── Student.java
    │   │   │   ├── Lecturer.java
    │   │   │   ├── Course.java
    │   │   │   ├── Enrollment.java
    │   │   │   └── Grade.java
    │   │   ├── dao/                 # Data Access Objects (SQLite operations)
    │   │   │   ├── DatabaseManager.java
    │   │   │   ├── UserDao.java
    │   │   │   ├── StudentDao.java
    │   │   │   ├── LecturerDao.java
    │   │   │   ├── CourseDao.java
    │   │   │   ├── EnrollmentDao.java
    │   │   │   └── GradeDao.java
    │   │   ├── view/                # JavaFX views (UI only)
    │   │   │   ├── LoginView.java
    │   │   │   ├── StudentDashboardView.java
    │   │   │   └── AdminDashboardView.java
    │   │   └── util/                # Utility classes
    │   │       ├── ValidationUtil.java
    │   │       ├── PasswordUtil.java
    │   │       ├── SessionManager.java
    │   │       └── AlertUtil.java
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

## Architecture (MVC)

The application follows the **Model-View-Controller** pattern:

- **Model**: Plain Java objects representing database entities (`User`, `Student`, `Course`, etc.)
- **View**: JavaFX UI classes that build the interface and delegate all actions to controllers
- **Controller**: Business logic, validation, and DAO coordination. Views never access the database directly.

```
View (UI) → Controller (logic) → DAO (database) → Model (data)
```

## Prerequisites

- **Java 23** (JDK) installed and available on PATH

Maven is **not** required the included Maven Wrapper (`mvnw.cmd`) downloads it automatically.

## Building and Running

All commands should be run from the `AcademicManagementSystem/` directory.

### Set JAVA_HOME (if not already set)

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
```

To set it permanently:

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-23", "User")
```

### Compile

```powershell
.\mvnw.cmd compile
```

### Run the application (via Maven)

```powershell
.\mvnw.cmd javafx:run
```

### Run tests

```powershell
.\mvnw.cmd test
```

### Build a portable fat JAR

```powershell
.\mvnw.cmd package -DskipTests
```

This produces `target/academic-management-system-1.0-SNAPSHOT.jar` (~22 MB) containing all dependencies. Run it on any machine with Java 23:

```
java -jar academic-management-system-1.0-SNAPSHOT.jar
```

## Default Login

| Role    | Username | Password     |
|---------|----------|--------------|
| Admin   | admin    | admin123     |
| Student | jdoe     | password123  |
| Student | asmith   | password123  |
| Student | bwilson  | password123  |
| Student | clee     | password123  |
| Student | dpatel   | password123  |

New students can also register through the login screen.

## Sample Data

The database is seeded with dummy data on first run:

- **4 Lecturers** — James Smith, Sarah Johnson, Michael Brown, Emily Davis
- **5 Students** — John Doe, Alice Smith, Ben Wilson, Clara Lee, Dev Patel
- **5 Courses** — COMP1322, COMP1206, COMP1216, MATH1060, ELEC1201
- **12 Enrollments** — mix of Enrolled, Completed, and Withdrawn statuses
- **5 Grades** — with feedback and letter grades

## Features

### Student Dashboard
- **My Courses**: View enrolled courses, withdraw from courses
- **Available Courses**: Browse all courses, enroll (with capacity checks)
- **My Grades**: View grades, feedback, and letter grades
- **My Profile**: Update personal details (name, email, programme)

### Admin Dashboard
- **Students**: View, update, and delete student records
- **Lecturers**: Full CRUD (add, update, delete)
- **Courses**: Full CRUD with lecturer assignment and capacity settings
- **Enrollments**: Create enrollments, update status (Enrolled/Withdrawn/Completed)
- **Grades**: Assign and update grades (0–100) with automatic letter grade calculation
- **Reports**: Five report types with filters:
  - Average Grade by Course
  - Grade Distribution by Course
  - Enrollment Statistics
  - Grades by Date Range
  - Student Transcript

## Grade Scale

| Letter | Range   |
|--------|---------|
| A      | 70–100  |
| B      | 60–69   |
| C      | 50–59   |
| D      | 40–49   |
| F      | 0–39    |

## Technologies

- Java 23
- JavaFX 23.0.1
- SQLite (via sqlite-jdbc 3.44.1.0)
- JUnit 5.10.1
- Maven 3.9.6 (via Maven Wrapper)
