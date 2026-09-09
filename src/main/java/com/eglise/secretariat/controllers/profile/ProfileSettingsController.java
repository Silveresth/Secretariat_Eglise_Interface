package com.eglise.secretariat.controllers.profile;

import com.eglise.secretariat.config.SessionManager;
import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.services.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ProfileSettingsController extends BaseController {

    // View Card Elements
    @FXML private VBox cardProfileView;
    @FXML private Label lblAvatarInitials;
    @FXML private Label lblDisplayUsername;
    @FXML private Label lblDisplayRole;
    @FXML private Label valUsername;
    @FXML private Label valRole;
    @FXML private Button btnEditProfile;

    // Success Banner in View Card
    @FXML private HBox successBanner;
    @FXML private Label successBannerLabel;

    // Edit Card Elements
    @FXML private VBox cardProfileEdit;
    @FXML private TextField txtNomComplet;
    @FXML private Label errUsername;

    // Mot de passe actuel
    @FXML private PasswordField txtMotDePasseActuel;
    @FXML private TextField txtMotDePasseActuelVisible;
    @FXML private Button toggleCurrentPasswordBtn;
    @FXML private Label errCurrentPassword;

    // Nouveau mot de passe
    @FXML private PasswordField txtNouveauMotDePasse;
    @FXML private TextField txtNouveauMotDePasseVisible;
    @FXML private Button toggleNewPasswordBtn;
    @FXML private Label errNewPassword;

    @FXML private Button btnSaveProfil;
    @FXML private Button btnCancelEdit;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();
    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;

    @FXML
    public void initialize() {
        // Sync password fields
        if (txtMotDePasseActuelVisible != null && txtMotDePasseActuel != null) {
            txtMotDePasseActuelVisible.textProperty().bindBidirectional(txtMotDePasseActuel.textProperty());
        }
        if (txtNouveauMotDePasseVisible != null && txtNouveauMotDePasse != null) {
            txtNouveauMotDePasseVisible.textProperty().bindBidirectional(txtNouveauMotDePasse.textProperty());
        }

        refreshProfileViewData();
    }

    private void refreshProfileViewData() {
        SessionManager sm = SessionManager.getInstance();
        String username = sm.getUsername();
        String role = sm.getRole() != null ? sm.getRole() : "ADMIN";

        if (lblDisplayUsername != null) lblDisplayUsername.setText(username);
        if (lblDisplayRole != null) lblDisplayRole.setText(role);
        if (valUsername != null) valUsername.setText(username);
        if (valRole != null) valRole.setText(role.toUpperCase());

        // Set Initials
        if (lblAvatarInitials != null) {
            if (username != null && !username.trim().isEmpty()) {
                String[] parts = username.trim().split("\\s+");
                if (parts.length >= 2) {
                    lblAvatarInitials.setText((parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase());
                } else if (username.length() >= 2) {
                    lblAvatarInitials.setText(username.substring(0, 2).toUpperCase());
                } else {
                    lblAvatarInitials.setText(username.substring(0, 1).toUpperCase());
                }
            } else {
                lblAvatarInitials.setText("SG");
            }
        }
    }

    @FXML
    private void handleCloseSuccessBanner(ActionEvent event) {
        if (successBanner != null) {
            successBanner.setVisible(false);
            successBanner.setManaged(false);
        }
    }

    @FXML
    private void handleToggleCurrentPassword(ActionEvent event) {
        isCurrentPasswordVisible = !isCurrentPasswordVisible;
        if (isCurrentPasswordVisible) {
            txtMotDePasseActuel.setVisible(false);
            txtMotDePasseActuel.setManaged(false);
            txtMotDePasseActuelVisible.setVisible(true);
            txtMotDePasseActuelVisible.setManaged(true);
            toggleCurrentPasswordBtn.setText("🙈");
        } else {
            txtMotDePasseActuelVisible.setVisible(false);
            txtMotDePasseActuelVisible.setManaged(false);
            txtMotDePasseActuel.setVisible(true);
            txtMotDePasseActuel.setManaged(true);
            toggleCurrentPasswordBtn.setText("👁");
        }
    }

    @FXML
    private void handleToggleNewPassword(ActionEvent event) {
        isNewPasswordVisible = !isNewPasswordVisible;
        if (isNewPasswordVisible) {
            txtNouveauMotDePasse.setVisible(false);
            txtNouveauMotDePasse.setManaged(false);
            txtNouveauMotDePasseVisible.setVisible(true);
            txtNouveauMotDePasseVisible.setManaged(true);
            toggleNewPasswordBtn.setText("🙈");
        } else {
            txtNouveauMotDePasseVisible.setVisible(false);
            txtNouveauMotDePasseVisible.setManaged(false);
            txtNouveauMotDePasse.setVisible(true);
            txtNouveauMotDePasse.setManaged(true);
            toggleNewPasswordBtn.setText("👁");
        }
    }

    @FXML
    private void handleShowEditForm(ActionEvent event) {
        SessionManager sm = SessionManager.getInstance();
        if (txtNomComplet != null) txtNomComplet.setText(sm.getUsername());
        if (txtMotDePasseActuel != null) txtMotDePasseActuel.clear();
        if (txtNouveauMotDePasse != null) txtNouveauMotDePasse.clear();

        clearErrors();

        // Reset visibility toggles
        isCurrentPasswordVisible = false;
        isNewPasswordVisible = false;
        if (txtMotDePasseActuel != null) { txtMotDePasseActuel.setVisible(true); txtMotDePasseActuel.setManaged(true); }
        if (txtMotDePasseActuelVisible != null) { txtMotDePasseActuelVisible.setVisible(false); txtMotDePasseActuelVisible.setManaged(false); }
        if (toggleCurrentPasswordBtn != null) toggleCurrentPasswordBtn.setText("👁");

        if (txtNouveauMotDePasse != null) { txtNouveauMotDePasse.setVisible(true); txtNouveauMotDePasse.setManaged(true); }
        if (txtNouveauMotDePasseVisible != null) { txtNouveauMotDePasseVisible.setVisible(false); txtNouveauMotDePasseVisible.setManaged(false); }
        if (toggleNewPasswordBtn != null) toggleNewPasswordBtn.setText("👁");

        setEditMode(true);
    }

    @FXML
    private void handleCancelEdit(ActionEvent event) {
        clearErrors();
        setEditMode(false);
    }

    private void setEditMode(boolean editMode) {
        if (cardProfileView != null) {
            cardProfileView.setVisible(!editMode);
            cardProfileView.setManaged(!editMode);
        }
        if (cardProfileEdit != null) {
            cardProfileEdit.setVisible(editMode);
            cardProfileEdit.setManaged(editMode);
        }
    }

    private void clearErrors() {
        if (errUsername != null) { errUsername.setText(""); errUsername.setVisible(false); errUsername.setManaged(false); }
        if (errCurrentPassword != null) { errCurrentPassword.setText(""); errCurrentPassword.setVisible(false); errCurrentPassword.setManaged(false); }
        if (errNewPassword != null) { errNewPassword.setText(""); errNewPassword.setVisible(false); errNewPassword.setManaged(false); }
    }

    private void showFieldError(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    @FXML
    private void handleSaveProfil(ActionEvent event) {
        clearErrors();

        String newUsername = txtNomComplet != null ? txtNomComplet.getText().trim() : "";
        String currentPass = txtMotDePasseActuel != null ? txtMotDePasseActuel.getText().trim() : "";
        String newPass = txtNouveauMotDePasse != null ? txtNouveauMotDePasse.getText().trim() : "";

        boolean hasError = false;

        if (newUsername.isEmpty()) {
            showFieldError(errUsername, "Le nom d'utilisateur ne peut pas être vide.");
            hasError = true;
        }

        if (currentPass.isEmpty()) {
            showFieldError(errCurrentPassword, "Veuillez saisir votre mot de passe actuel.");
            hasError = true;
        }

        if (hasError) return;

        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (btnSaveProfil != null) btnSaveProfil.setDisable(true);
        if (btnCancelEdit != null) btnCancelEdit.setDisable(true);

        authService.updateProfile(currentPass, newUsername, newPass.isEmpty() ? null : newPass)
                .whenComplete((res, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        if (btnSaveProfil != null) btnSaveProfil.setDisable(false);
                        if (btnCancelEdit != null) btnCancelEdit.setDisable(false);

                        if (throwable != null) {
                            String errorMsg = extractCleanErrorMessage(throwable);
                            showFieldError(errCurrentPassword, errorMsg);
                        } else {
                            refreshProfileViewData();
                            setEditMode(false);

                            // Afficher la bannière de succès avec bouton de fermeture
                            if (successBanner != null) {
                                if (successBannerLabel != null) {
                                    successBannerLabel.setText("Vos informations de profil ont été modifiées avec succès.");
                                }
                                successBanner.setVisible(true);
                                successBanner.setManaged(true);
                            }
                        }
                    });
                });
    }

    private String extractCleanErrorMessage(Throwable throwable) {
        if (throwable == null) return "Mot de passe actuel incorrect.";
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String msg = current.getMessage();
        if (msg == null || msg.isBlank() || msg.contains("401") || msg.contains("Unauthorized") || msg.contains("Bad credentials") || msg.contains("incorrect")) {
            return "Mot de passe actuel incorrect.";
        }
        if (msg.contains(": ")) {
            msg = msg.substring(msg.lastIndexOf(": ") + 2).trim();
        }
        return msg;
    }
}



