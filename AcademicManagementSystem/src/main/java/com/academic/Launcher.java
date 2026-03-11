package com.academic;

/**
 * Launcher class for the fat JAR.
 * JavaFX requires the main class to NOT extend Application
 * when running from a shaded/fat JAR. This class delegates
 * to App.main() to bypass that restriction.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
