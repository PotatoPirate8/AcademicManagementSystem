# Structured Logging & Error Handling Guide

## Overview

This project uses **SLF4J** with **Logback** for structured logging and centralized error handling through the `ErrorHandler` utility class. This enables consistent, debuggable logging across the application.

## Setup

The logging configuration is in `src/main/resources/logback.xml` and includes:
- **Console output** for development
- **Rolling file appenders** for production logs (daily rollover + 10MB per file)
- **Error-specific file** that captures ERROR and FATAL level logs only
- **Package-specific log levels** for granular control

## Using ErrorHandler

The `ErrorHandler` utility class provides static methods for consistent error handling and logging:

### 1. Handle Errors with User-Friendly Messages

```java
try {
    // database or IO operation
} catch (SQLException e) {
    String userMessage = ErrorHandler.handleError("updating student record", e);
    // Use userMessage in UI or API response
}
```

### 2. Log Authentication Events

```java
// Successful login
ErrorHandler.logAuthEvent("LOGIN", "jdoe", true, "Authentication successful");

// Failed login
ErrorHandler.logAuthEvent("LOGIN", "jdoe", false, "Invalid credentials");
```

### 3. Log Validation Failures

```java
if (!email.contains("@")) {
    ErrorHandler.logValidationFailure("email", "Invalid format", email);
}
```

### 4. Log Database Operations

```java
try {
    int userId = createUserInDatabase(user);
    ErrorHandler.logDatabaseOperation("INSERT", "User", userId, true, null);
} catch (SQLException e) {
    ErrorHandler.logDatabaseOperation("INSERT", "User", -1, false, e);
}
```

### 5. Log Info Messages

```java
ErrorHandler.logInfo("CourseEnrollment", "Student enrolled in COMP1322");
```

### 6. Log Warnings

```java
ErrorHandler.logWarning("High database query time detected", "Query took 5000ms");
```

### 7. Get Custom Logger for Specific Classes

```java
Logger logger = ErrorHandler.getLogger(MyClass.class);
logger.debug("Custom debug message");
```

## Using SLF4J Directly

For more control, get a logger directly:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);
    
    public void myMethod() {
        LOGGER.info("Processing started");
        LOGGER.debug("Details: {}", myVariable);
        LOGGER.warn("This might be an issue");
        LOGGER.error("An error occurred", exception);
    }
}
```

## Log Levels

| Level | Usage | Example |
|-------|-------|---------|
| **DEBUG** | Detailed diagnostic information | Parameter values, method entry/exit |
| **INFO** | General informational messages | Successful operations, state changes |
| **WARN** | Warning conditions | Deprecated usage, fallback behavior |
| **ERROR** | Error conditions | Exceptions, failed operations |

## Log Output

### Console (Development)
```
2026-03-25 17:30:45.123 [JavaFX Application Thread] INFO  com.academic.dao.UserDao - User created successfully: username=jdoe, id=1
```

### Files (Production - `logs/` directory)
- `application.log` - All application logs (rotated daily or at 10MB)
- `error.log` - Only ERROR and FATAL level logs

### Log File Pattern
```
2026-03-25 17:30:45.123 [JavaFX Application Thread] INFO  [com.academic.dao.UserDao] - User created successfully: username=jdoe, id=1
```

## Package-Specific Log Levels

Configured in `logback.xml`:
- `com.academic.dao` - DEBUG (detailed database logging)
- `com.academic.service` - INFO (service operations)
- `com.academic.controller` - INFO (controller operations)
- `com.academic.util` - DEBUG (utility operations)

To change log levels, edit `logback.xml` and redeploy.

## Best Practices

1. **Use ErrorHandler for user-facing operations** - Standardizes error messages
2. **Log authentication events** - Helps with security audits
3. **Log database operations** - Tracks data modifications
4. **Avoid logging sensitive data** - Don't log passwords, full emails, etc.
5. **Use appropriate log levels** - Don't spam INFO logs with debug details
6. **Include context** - Use log parameters for traceability

## Example: Complete Error Handling Flow

```java
public class StudentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StudentService.class);
    
    public ServiceResult<Student> enrollStudent(int studentId, int courseId) {
        try {
            LOGGER.info("Enrolling student {} in course {}", studentId, courseId);
            
            Student student = studentDao.findById(studentId);
            if (student == null) {
                ErrorHandler.logWarning("Student not found", "Student ID: " + studentId);
                return ServiceResult.failure("Student not found");
            }
            
            enrollmentDao.create(new Enrollment(studentId, courseId));
            ErrorHandler.logDatabaseOperation("INSERT", "Enrollment", courseId, true, null);
            
            LOGGER.info("Student {} successfully enrolled in course {}", studentId, courseId);
            return ServiceResult.success(student);
            
        } catch (SQLException e) {
            ErrorHandler.logDatabaseOperation("INSERT", "Enrollment", courseId, false, e);
            return ServiceResult.failure(ErrorHandler.handleError("enrolling student", e));
        }
    }
}
```

## Troubleshooting

**Q: Logs aren't appearing in console?**
- Check that logback.xml is in `src/main/resources/` and is on the classpath
- Verify the log level in logback.xml isn't set too high

**Q: Error log files aren't being created?**
- Ensure the `logs/` directory exists or is writable
- Check logback configuration for file appender settings

**Q: How to temporarily increase logging verbosity?**
- Change log levels in logback.xml:
  ```xml
  <logger name="com.academic.dao" level="TRACE" additivity="false">
  ```

**Q: How to disable console output?**
- Remove or comment out the CONSOLE appender reference in root logger:
  ```xml
  <root level="INFO">
      <!-- <appender-ref ref="CONSOLE"/> -->
      <appender-ref ref="FILE"/>
  </root>
  ```
