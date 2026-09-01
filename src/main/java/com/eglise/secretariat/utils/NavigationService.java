package com.eglise.secretariat.utils;

import com.eglise.secretariat.config.AppConfig;
import com.eglise.secretariat.controllers.layout.MainLayoutController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NavigationService {

    public enum View {
        LOGIN("/com/eglise/secretariat/fxml/auth/LoginView.fxml"),
        MAIN_LAYOUT("/com/eglise/secretariat/fxml/layout/MainLayoutView.fxml"),
        DASHBOARD("/com/eglise/secretariat/fxml/dashboard/DashboardView.fxml"),
        FIDELES_LIST("/com/eglise/secretariat/fxml/fideles/FidelesListView.fxml"),
        FIDELE_DETAILS("/com/eglise/secretariat/fxml/fideles/FideleDetailsView.fxml"),
        FIDELE_WIZARD("/com/eglise/secretariat/fxml/fideles/FideleWizardView.fxml"),
        FIDELE_EDIT("/com/eglise/secretariat/fxml/fideles/FideleEditDialog.fxml"),
        MOUVEMENTS_OCR("/com/eglise/secretariat/fxml/mouvements/MouvementsOcrView.fxml"),
        DOCUMENTS_PDF("/com/eglise/secretariat/fxml/documents/DocumentsView.fxml"),
        PROFILE_SETTINGS("/com/eglise/secretariat/fxml/profile/ProfileSettingsView.fxml");

        private final String fxmlPath;

        View(String fxmlPath) {
            this.fxmlPath = fxmlPath;
        }

        public String getFxmlPath() {
            return fxmlPath;
        }
    }

    private static NavigationService instance;
    private Stage primaryStage;
    private Scene mainScene;
    private MainLayoutController mainLayoutController;
    private final Map<String, Object> navigationParameters = new HashMap<>();

    private NavigationService() {}

    public static synchronized NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setMinWidth(960);
        this.primaryStage.setMinHeight(600);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    public void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(View.LOGIN.getFxmlPath()));
            Parent root = loader.load();

            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double targetWidth = Math.min(960, visualBounds.getWidth() * 0.85);
            double targetHeight = Math.min(650, visualBounds.getHeight() * 0.85);

            if (mainScene == null) {
                mainScene = new Scene(root, targetWidth, targetHeight);
                mainScene.getStylesheets().add(getClass().getResource("/com/eglise/secretariat/css/main.css").toExternalForm());
                primaryStage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
                if (!primaryStage.isMaximized()) {
                    primaryStage.setWidth(targetWidth);
                    primaryStage.setHeight(targetHeight);
                    primaryStage.centerOnScreen();
                }
            }

            primaryStage.setTitle("Connexion - " + AppConfig.APP_TITLE);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            NotificationUtil.showError("Erreur de navigation", "Impossible de charger la page de connexion: " + e.getMessage());
        }
    }

    public void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(View.MAIN_LAYOUT.getFxmlPath()));
            Parent root = loader.load();
            this.mainLayoutController = loader.getController();

            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            double targetWidth = Math.min(1240, visualBounds.getWidth() * 0.95);
            double targetHeight = Math.min(740, visualBounds.getHeight() * 0.95);

            if (mainScene == null) {
                mainScene = new Scene(root, targetWidth, targetHeight);
                mainScene.getStylesheets().add(getClass().getResource("/com/eglise/secretariat/css/main.css").toExternalForm());
                primaryStage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
                if (!primaryStage.isMaximized()) {
                    primaryStage.setWidth(targetWidth);
                    primaryStage.setHeight(targetHeight);
                    primaryStage.centerOnScreen();
                }
            }

            primaryStage.setTitle(AppConfig.APP_TITLE);
            primaryStage.show();

            // Default view inside MainLayout: Dashboard
            navigateToContent(View.DASHBOARD);
        } catch (IOException e) {
            e.printStackTrace();
            NotificationUtil.showError("Erreur de navigation", "Impossible de charger l'interface principale: " + e.getMessage());
        }
    }

    public void navigateToContent(View view) {
        navigateToContent(view, null);
    }

    public void navigateToContent(View view, Map<String, Object> params) {
        if (params != null) {
            navigationParameters.clear();
            navigationParameters.putAll(params);
        }

        if (mainLayoutController != null) {
            mainLayoutController.loadView(view);
        }
    }

    public Object getParameter(String key) {
        return navigationParameters.get(key);
    }

    public void setParameter(String key, Object value) {
        navigationParameters.put(key, value);
    }

    public void clearParameters() {
        navigationParameters.clear();
    }
}
