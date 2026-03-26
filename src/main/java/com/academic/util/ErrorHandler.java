package com.academic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized error handling and logging utility.
 * Provides consistent methods for logging errors, warnings, and debug information
 * across the application to speed up debugging and make failures easier to diagnose.
 */
public class ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    /**
     * Logs an error and returns a user-friendly error message.
     * 
     * @param context Description of what was being attempted
     * @param exception The exception that occurred
     * @return A user-friendly error message
     */
    public static String handleError(String context, Exception exception) {
        LOGGER.error("Error in {}: {}", context, exception.getMessage(), exception);
        return "An error occurred while " + context + ". Please try again or contact support.";
    }

    /**
     * Logs an error with additional context details.
     * 
     * @param context Description of what was being attempted
     * @param exception The exception that occurred
     * @param contextDetails Additional context information (e.g., user ID, data)
     * @return A user-friendly error message
     */
    public static String handleError(String context, Exception exception, String contextDetails) {
        LOGGER.error("Error in {}: {} [Context: {}]", context, exception.getMessage(), contextDetails, exception);
        return "An error occurred while " + context + ". Please try again or contact support.";
    }

    /**
     * Logs a warning with additional details.
     * 
     * @param message The warning message
     * @param details Additional context information
     */
    public static void logWarning(String message, String details) {
        LOGGER.warn("{} [Details: {}]", message, details);
    }

    /**
     * Logs a warning.
     * 
     * @param message The warning message
     */
    public static void logWarning(String message) {
        LOGGER.warn(message);
    }

    /**
     * Logs informational message (e.g., successful operations).
     * 
     * @param context The context of the operation
     * @param message The message
     */
    public static void logInfo(String context, String message) {
        LOGGER.info("[{}] {}", context, message);
    }

    /**
     * Logs debug information.
     * 
     * @param context The context of the operation
     * @param message The debug message
     */
    public static void logDebug(String context, String message) {
        LOGGER.debug("[{}] {}", context, message);
    }

    /**
     * Logs debug information with exception stack trace.
     * 
     * @param context The context of the operation
     * @param message The debug message
     * @param exception The exception to log
     */
    public static void logDebug(String context, String message, Exception exception) {
        LOGGER.debug("[{}] {} Cause: {}", context, message, exception.getMessage(), exception);
    }

    /**
     * Logs a database operation result.
     * 
     * @param operation The type of operation (e.g., "INSERT", "UPDATE", "DELETE")
     * @param entityType The entity type being modified
     * @param entityId The entity ID
     * @param success Whether the operation was successful
     * @param exception Optional exception if operation failed
     */
    public static void logDatabaseOperation(String operation, String entityType, long entityId, boolean success, Exception exception) {
        if (success) {
            LOGGER.info("DB: {} {} with ID {} - Success", operation, entityType, entityId);
        } else {
            LOGGER.error("DB: {} {} with ID {} - Failed: {}", operation, entityType, entityId, 
                exception != null ? exception.getMessage() : "Unknown error", exception);
        }
    }

    /**
     * Logs an authentication or authorization event.
     * 
     * @param event The event type (e.g., "LOGIN", "LOGOUT", "ACCESS_DENIED")
     * @param username The username involved
     * @param success Whether the event was successful
     * @param message Additional message
     */
    public static void logAuthEvent(String event, String username, boolean success, String message) {
        String status = success ? "Success" : "Failed";
        LOGGER.info("AUTH: {} for user '{}' - {} [{}]", event, username, status, message);
    }

    /**
     * Logs validation failures.
     * 
     * @param fieldName The field that failed validation
     * @param reason The reason it failed
     * @param attemptedValue The value that was attempted
     */
    public static void logValidationFailure(String fieldName, String reason, String attemptedValue) {
        LOGGER.warn("Validation failed for field '{}': {} (value: {})", fieldName, reason, attemptedValue);
    }

    /**
     * Gets a logger for a specific class (useful for custom logging in specific classes).
     * 
     * @param clazz The class to create a logger for
     * @return A Logger instance for the class
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
