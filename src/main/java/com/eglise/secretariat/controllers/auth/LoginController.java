package com.eglise.secretariat.controllers.auth;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.services.AuthService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController extends BaseController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginBtn;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label errorLabel;
    @FXML private VBox errorBox;

    private final AuthService authService = new AuthService();
    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        if (errorBox != null) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
        }

        // Sync password fields
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }

    @FXML
    private void handleTogglePassword(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";

        if (email.isEmpty()) {
            showError("Veuillez saisir votre identifiant ou adresse email.");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError("Veuillez saisir votre mot de passe.");
            passwordField.requestFocus();
            return;
        }

        hideError();
        setLoading(true);

        authService.login(email, password)
                .whenComplete((authResponse, throwable) -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        if (throwable != null) {
                            showError(extractCleanErrorMessage(throwable));
                        } else {
                            NotificationUtil.showSuccess("Connexion réussie", "Bienvenue sur le secrétariat de l'église.");
                            navigationService.navigateToMain();
                        }
                    });
                });
    }

    private String extractCleanErrorMessage(Throwable throwable) {
        if (throwable == null) return "Échec de connexion au serveur.";
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String msg = current.getMessage();
        if (msg == null || msg.isBlank()) {
            return "Identifiant ou mot de passe incorrect.";
        }
        if (msg.contains(": ")) {
            msg = msg.substring(msg.lastIndexOf(": ") + 2).trim();
        }
        return msg;
    }

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
        if (loginBtn != null) {
            loginBtn.setDisable(loading);
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        }
        if (errorBox != null) {
            errorBox.setVisible(true);
            errorBox.setManaged(true);
        }
    }

    private void hideError() {
        if (errorBox != null) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
        }
    }
}
