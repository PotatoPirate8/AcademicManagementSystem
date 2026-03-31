package com.academic.util;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * Reusable password input with show/hide toggle.
 * Uses an eye icon when password is hidden and slashed-eye when visible.
 */
public class PasswordToggleField {

    private static final String EYE_ICON = "\uD83D\uDC41";

    private final HBox container;
    private final PasswordField passwordField;
    private final TextField visibleField;
    private final Button toggleButton;
    private final Label slashOverlay;

    public PasswordToggleField() {
        this.passwordField = new PasswordField();
        this.visibleField = new TextField();
        this.toggleButton = new Button();
        this.container = new HBox(0);
        Label eyeLabel = new Label(EYE_ICON);
        this.slashOverlay = new Label("╱");

        eyeLabel.getStyleClass().add("password-eye-icon");
        slashOverlay.getStyleClass().add("password-eye-slash");
        slashOverlay.setVisible(false);

        StackPane iconPane = new StackPane(eyeLabel, slashOverlay);
        iconPane.getStyleClass().add("password-eye-icon-container");
        toggleButton.setGraphic(iconPane);

        visibleField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleField.setVisible(false);
        visibleField.setManaged(false);

        toggleButton.getStyleClass().add("password-toggle-button");
        toggleButton.setFocusTraversable(false);

        toggleButton.setOnAction(e -> toggleVisibility());

        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(visibleField, Priority.ALWAYS);

        container.getChildren().addAll(passwordField, visibleField, toggleButton);
        container.getStyleClass().add("password-toggle-container");
    }

    public HBox getNode() {
        return container;
    }

    public String getText() {
        return passwordField.getText();
    }

    public void clear() {
        passwordField.clear();
    }

    public void setPromptText(String promptText) {
        passwordField.setPromptText(promptText);
        visibleField.setPromptText(promptText);
    }

    public void setMaxWidth(double maxWidth) {
        container.setMaxWidth(maxWidth);
    }

    public void setOnAction(Runnable action) {
        passwordField.setOnAction(e -> action.run());
        visibleField.setOnAction(e -> action.run());
    }

    private void toggleVisibility() {
        boolean showingPassword = visibleField.isVisible();
        boolean showPassword = !showingPassword;

        visibleField.setVisible(showPassword);
        visibleField.setManaged(showPassword);
        passwordField.setVisible(!showPassword);
        passwordField.setManaged(!showPassword);
        slashOverlay.setVisible(showPassword);

        if (showPassword) {
            visibleField.requestFocus();
            visibleField.positionCaret(visibleField.getText().length());
        } else {
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }
}
