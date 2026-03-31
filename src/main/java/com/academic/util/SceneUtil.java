package com.academic.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utility methods for scene creation while preserving current stage resolution.
 */
public final class SceneUtil {

    private SceneUtil() {
        // Utility class
    }

    public static Scene createScenePreservingSize(Stage stage,
                                                  Parent root,
                                                  double fallbackWidth,
                                                  double fallbackHeight) {
        double width = fallbackWidth;
        double height = fallbackHeight;

        if (stage != null && stage.getScene() != null) {
            double currentWidth = stage.getScene().getWidth();
            double currentHeight = stage.getScene().getHeight();
            if (currentWidth > 0 && currentHeight > 0) {
                width = currentWidth;
                height = currentHeight;
            }
        }

        return new Scene(root, width, height);
    }
}