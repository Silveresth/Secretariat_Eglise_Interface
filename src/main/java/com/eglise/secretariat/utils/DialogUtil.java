package com.eglise.secretariat.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.Optional;

public class DialogUtil {

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title != null ? title : "Confirmation");
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }

    private static void styleDialog(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        try {
            String cssPath = DialogUtil.class.getResource("/com/eglise/secretariat/css/main.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
            dialogPane.getStyleClass().add("custom-dialog");
        } catch (Exception ignored) {}
    }
}
