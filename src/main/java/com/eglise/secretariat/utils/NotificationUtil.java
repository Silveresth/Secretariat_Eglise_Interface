package com.eglise.secretariat.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotificationUtil {

    public enum ToastType {
        SUCCESS("#10b981", "✓", "Succès"),
        INFO("#38bdf8", "ℹ", "Information"),
        WARNING("#fbbf24", "⚠", "Attention"),
        ERROR("#f87171", "✕", "Erreur");

        final String iconColor;
        final String icon;
        final String defaultTitle;

        ToastType(String iconColor, String icon, String defaultTitle) {
            this.iconColor = iconColor;
            this.icon = icon;
            this.defaultTitle = defaultTitle;
        }
    }

    public static void showSuccess(String title, String message) {
        show(ToastType.SUCCESS, title, message, 3500);
    }

    public static void showInfo(String title, String message) {
        show(ToastType.INFO, title, message, 3000);
    }

    public static void showWarning(String title, String message) {
        show(ToastType.WARNING, title, message, 4500);
    }

    public static void showError(String title, String message) {
        show(ToastType.ERROR, title, message, 5500);
    }

    private static void show(ToastType type, String title, String message, int durationMs) {
        runOnFx(() -> {
            try {
                Stage stage = NavigationService.getInstance().getPrimaryStage();
                if (stage == null || !stage.isShowing()) return;

                Popup popup = new Popup();
                popup.setAutoHide(true);

                // Container Box
                HBox root = new HBox(12);
                root.setAlignment(Pos.CENTER_LEFT);
                root.setMinWidth(300);
                root.setMaxWidth(440);
                root.setPadding(new Insets(12, 18, 12, 16));

                // Clean, elegant Dark Slate modern theme (No ugly colored left bar)
                root.setStyle(
                    "-fx-background-color: #0f172a;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: #334155;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-border-width: 1px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.45), 16, 0.15, 0, 4);"
                );

                // Icon circle
                StackPane iconCircle = new StackPane();
                iconCircle.setMinSize(28, 28);
                iconCircle.setMaxSize(28, 28);
                iconCircle.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 50%;");

                Label iconLbl = new Label(type.icon);
                iconLbl.setStyle(
                    "-fx-text-fill: " + type.iconColor + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;"
                );
                iconCircle.getChildren().add(iconLbl);

                // Text column
                VBox textCol = new VBox(2);
                textCol.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(textCol, Priority.ALWAYS);

                Label titleLbl = new Label(title != null && !title.isBlank() ? title : type.defaultTitle);
                titleLbl.setStyle(
                    "-fx-text-fill: #ffffff;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;"
                );

                Label msgLbl = new Label(message != null ? message : "");
                msgLbl.setWrapText(true);
                msgLbl.setMaxWidth(340);
                msgLbl.setStyle(
                    "-fx-text-fill: #cbd5e1;" +
                    "-fx-font-size: 12px;"
                );

                textCol.getChildren().addAll(titleLbl, msgLbl);
                root.getChildren().addAll(iconCircle, textCol);

                // Click to close
                root.setOnMouseClicked(e -> popup.hide());
                root.setCursor(Cursor.HAND);

                popup.getContent().add(root);

                // Calculate top-right position relative to application window
                double x = stage.getX() + stage.getWidth() - 460;
                double y = stage.getY() + 45;
                if (x < stage.getX() + 10) x = stage.getX() + 10;
                if (y < stage.getY() + 10) y = stage.getY() + 10;

                popup.show(stage, x, y);

                // Smooth Animation
                root.setOpacity(0);
                root.setTranslateY(-8);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(180), root);
                fadeIn.setToValue(1.0);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(180), root);
                slideIn.setToY(0);

                PauseTransition stay = new PauseTransition(Duration.millis(durationMs));

                FadeTransition fadeOut = new FadeTransition(Duration.millis(250), root);
                fadeOut.setToValue(0.0);

                SequentialTransition seq = new SequentialTransition(fadeIn, stay, fadeOut);
                seq.setOnFinished(e -> popup.hide());
                seq.play();
                slideIn.play();
            } catch (Exception ignored) {}
        });
    }

    private static void runOnFx(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
