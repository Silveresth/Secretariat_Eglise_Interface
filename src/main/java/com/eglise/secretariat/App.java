package com.eglise.secretariat;

import com.eglise.secretariat.config.AppConfig;
import com.eglise.secretariat.utils.NavigationService;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(AppConfig.APP_TITLE);
        primaryStage.setResizable(false);
        
        // Load Application Icon
        try (InputStream iconStream = getClass().getResourceAsStream("/com/eglise/secretariat/images/logo.png")) {
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Note: Logo non chargé: " + e.getMessage());
        }

        NavigationService navigationService = NavigationService.getInstance();
        navigationService.setPrimaryStage(primaryStage);

        // Start on Login View
        navigationService.navigateToLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
