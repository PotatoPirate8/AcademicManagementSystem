package com.academic;

import com.academic.dao.DatabaseManager;
import com.academic.view.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for the Academic Management System.
 * Initializes the SQLite database and shows the login screen.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database connection and schema
        DatabaseManager.getInstance();

        // Show login view
        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView.getRoot(), 400, 550);
        scene.getStylesheets().add(
            getClass().getResource("/css/style.css").toExternalForm()
        );

        primaryStage.setTitle("Academic Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Close the database connection on application exit
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
