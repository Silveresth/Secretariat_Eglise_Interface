package com.eglise.secretariat.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class DialogUtil {

    public static boolean showConfirmation(String title, String header, String content) {
        if (header != null && (header.toLowerCase().contains("suppr") || header.toLowerCase().contains("delete")) ||
            (title != null && (title.toLowerCase().contains("suppr") || title.toLowerCase().contains("delete")))) {
            return showDeleteConfirmation(title, header, content);
        }
        return showStandardConfirmation(title, header, content);
    }

    public static boolean showDeleteConfirmation(String title, String headerText, String contentText) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(title != null ? title : "Confirmation de suppression");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setHeaderText(null);
        dialogPane.setGraphic(null);

        try {
            String cssPath = DialogUtil.class.getResource("/com/eglise/secretariat/css/main.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
        } catch (Exception ignored) {}

        // Root Container Box
        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setPrefWidth(480);
        root.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 12px; " +
            "-fx-font-family: 'Segoe UI', 'Inter', sans-serif;"
        );

        // Header with Icon Circle Badge
        HBox headerBox = new HBox(14);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(48, 48);
        iconCircle.setMaxSize(48, 48);
        iconCircle.setStyle(
            "-fx-background-color: #fee2e2; " +
            "-fx-background-radius: 50%; " +
            "-fx-alignment: center; " +
            "-fx-border-color: #fca5a5; " +
            "-fx-border-radius: 50%; " +
            "-fx-border-width: 1px;"
        );
        Label iconLbl = new Label("🗑️");
        iconLbl.setStyle("-fx-font-size: 22px;");
        iconCircle.getChildren().add(iconLbl);

        VBox titleCol = new VBox(4);
        titleCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleCol, Priority.ALWAYS);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLbl = new Label(headerText != null && !headerText.isBlank() ? headerText : "Confirmer la suppression");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #991b1b;");

        Label badgeLbl = new Label("Action Irréversible");
        badgeLbl.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 10.5px; -fx-font-weight: bold; -fx-padding: 3px 9px; -fx-background-radius: 12px;");

        titleRow.getChildren().addAll(titleLbl, badgeLbl);

        Label subheadLbl = new Label("Cette action supprimera définitivement les données sélectionnées.");
        subheadLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        titleCol.getChildren().addAll(titleRow, subheadLbl);
        headerBox.getChildren().addAll(iconCircle, titleCol);

        // Content Message Card
        VBox messageBox = new VBox(6);
        messageBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14px 16px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-border-width: 1px;");

        Label msgLbl = new Label(contentText != null ? contentText : "Voulez-vous vraiment procéder à la suppression ?");
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-font-weight: 500;");

        messageBox.getChildren().add(msgLbl);

        // Action Buttons Toolbar
        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(6, 0, 0, 0));

        Button cancelBtn = new Button("❌ Annuler");
        cancelBtn.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #cbd5e1; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-text-fill: #334155; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 9px 18px; " +
            "-fx-cursor: hand;"
        );

        Button deleteBtn = new Button("🗑️ Oui, Supprimer");
        deleteBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c); " +
            "-fx-text-fill: #ffffff; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 9px 20px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(220, 38, 38, 0.25), 6, 0, 0, 2);"
        );

        cancelBtn.setOnAction(e -> {
            dialog.setResult(false);
            dialog.close();
        });

        deleteBtn.setOnAction(e -> {
            dialog.setResult(true);
            dialog.close();
        });

        actionsBox.getChildren().addAll(cancelBtn, deleteBtn);

        root.getChildren().addAll(headerBox, messageBox, actionsBox);
        dialogPane.setContent(root);

        // Hide standard close buttons
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtnNode = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtnNode != null) {
            closeBtnNode.setVisible(false);
            closeBtnNode.setManaged(false);
        }

        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false);
    }

    private static boolean showStandardConfirmation(String title, String headerText, String contentText) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(title != null ? title : "Confirmation");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setHeaderText(null);
        dialogPane.setGraphic(null);

        try {
            String cssPath = DialogUtil.class.getResource("/com/eglise/secretariat/css/main.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
        } catch (Exception ignored) {}

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setPrefWidth(480);
        root.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-background-radius: 12px; " +
            "-fx-font-family: 'Segoe UI', 'Inter', sans-serif;"
        );

        HBox headerBox = new HBox(14);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(48, 48);
        iconCircle.setMaxSize(48, 48);
        iconCircle.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 50%; -fx-alignment: center; -fx-border-color: #bfdbfe; -fx-border-radius: 50%; -fx-border-width: 1px;");
        Label iconLbl = new Label("❓");
        iconLbl.setStyle("-fx-font-size: 22px;");
        iconCircle.getChildren().add(iconLbl);

        VBox titleCol = new VBox(4);
        titleCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleCol, Priority.ALWAYS);

        Label titleLbl = new Label(headerText != null && !headerText.isBlank() ? headerText : "Confirmation requise");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00236f;");

        titleCol.getChildren().add(titleLbl);
        headerBox.getChildren().addAll(iconCircle, titleCol);

        VBox messageBox = new VBox(6);
        messageBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14px 16px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-border-width: 1px;");

        Label msgLbl = new Label(contentText != null ? contentText : "Voulez-vous valider cette action ?");
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");

        messageBox.getChildren().add(msgLbl);

        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Non, Annuler");
        cancelBtn.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 9px 18px; -fx-cursor: hand;");

        Button confirmBtn = new Button("✓ Oui, Valider");
        confirmBtn.setStyle("-fx-background-color: linear-gradient(to right, #00236f, #1e3a8a); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8px; -fx-padding: 9px 20px; -fx-cursor: hand;");

        cancelBtn.setOnAction(e -> {
            dialog.setResult(false);
            dialog.close();
        });

        confirmBtn.setOnAction(e -> {
            dialog.setResult(true);
            dialog.close();
        });

        actionsBox.getChildren().addAll(cancelBtn, confirmBtn);

        root.getChildren().addAll(headerBox, messageBox, actionsBox);
        dialogPane.setContent(root);

        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtnNode = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtnNode != null) {
            closeBtnNode.setVisible(false);
            closeBtnNode.setManaged(false);
        }

        Optional<Boolean> result = dialog.showAndWait();
        return result.orElse(false);
    }

    public static void showAlert(Alert.AlertType type, String title, String headerText, String contentText) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title != null ? title : "Information");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setHeaderText(null);
        dialogPane.setGraphic(null);

        try {
            String cssPath = DialogUtil.class.getResource("/com/eglise/secretariat/css/main.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
        } catch (Exception ignored) {}

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setPrefWidth(460);
        root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px;");

        String icon = type == Alert.AlertType.ERROR ? "❌" : (type == Alert.AlertType.WARNING ? "⚠️" : "ℹ️");
        String iconBg = type == Alert.AlertType.ERROR ? "#fee2e2" : (type == Alert.AlertType.WARNING ? "#fef3c7" : "#eff6ff");
        String titleColor = type == Alert.AlertType.ERROR ? "#991b1b" : (type == Alert.AlertType.WARNING ? "#92400e" : "#00236f");

        HBox headerBox = new HBox(14);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(44, 44);
        iconCircle.setMaxSize(44, 44);
        iconCircle.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 50%; -fx-alignment: center;");
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 20px;");
        iconCircle.getChildren().add(iconLbl);

        Label titleLbl = new Label(headerText != null && !headerText.isBlank() ? headerText : title);
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + titleColor + ";");

        headerBox.getChildren().addAll(iconCircle, titleLbl);

        VBox messageBox = new VBox(6);
        messageBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12px 14px; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-width: 1px;");

        Label msgLbl = new Label(contentText != null ? contentText : "");
        msgLbl.setWrapText(true);
        msgLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b;");
        messageBox.getChildren().add(msgLbl);

        HBox actionsBox = new HBox();
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        Button okBtn = new Button("D'accord");
        okBtn.setStyle("-fx-background-color: #00236f; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-padding: 8px 20px; -fx-background-radius: 6px; -fx-cursor: hand;");
        okBtn.setOnAction(e -> dialog.close());
        actionsBox.getChildren().add(okBtn);

        root.getChildren().addAll(headerBox, messageBox, actionsBox);
        dialogPane.setContent(root);

        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtnNode = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtnNode != null) {
            closeBtnNode.setVisible(false);
            closeBtnNode.setManaged(false);
        }

        dialog.showAndWait();
    }
}
