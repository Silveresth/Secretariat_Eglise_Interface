package com.eglise.secretariat.controllers.profile;

import com.eglise.secretariat.config.AppConfig;
import com.eglise.secretariat.config.SessionManager;
import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.services.AuthService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProfileSettingsController extends BaseController {

    // Tab 1: Profil Secrétaire
    @FXML private TextField txtNomComplet;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtMotDePasseActuel;
    @FXML private PasswordField txtNouveauMotDePasse;
    @FXML private Button btnSaveProfil;

    // Tab 2: Informations Église & Réseau
    @FXML private TextField txtNomEglise;
    @FXML private TextField txtAdresseEglise;
    @FXML private TextField txtTelephoneEglise;
    @FXML private TextField txtNumeroEnregistrement;
    @FXML private TextField txtApiBaseUrl;
    @FXML private Button btnSaveEglise;

    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Load session data
        SessionManager sm = SessionManager.getInstance();
        if (txtNomComplet != null) txtNomComplet.setText(sm.getUsername());
        if (txtEmail != null) txtEmail.setText(sm.getEmail() != null ? sm.getEmail() : "admin@eglise.org");

        // Load Church Config
        if (txtNomEglise != null) txtNomEglise.setText(AppConfig.getChurchName());
        if (txtAdresseEglise != null) txtAdresseEglise.setText(AppConfig.getChurchAddress());
        if (txtTelephoneEglise != null) txtTelephoneEglise.setText(AppConfig.getChurchPhone());
        if (txtNumeroEnregistrement != null) txtNumeroEnregistrement.setText(AppConfig.getChurchRegNumber());
        if (txtApiBaseUrl != null) txtApiBaseUrl.setText(AppConfig.getApiBaseUrl());
    }

    @FXML
    private void handleSaveProfil(ActionEvent event) {
        String username = txtNomComplet != null ? txtNomComplet.getText().trim() : "";
        String currentPass = txtMotDePasseActuel != null ? txtMotDePasseActuel.getText().trim() : "";
        String newPass = txtNouveauMotDePasse != null ? txtNouveauMotDePasse.getText().trim() : "";

        if (username.isEmpty()) {
            NotificationUtil.showWarning("Champ requis", "Le nom d'utilisateur ne peut pas être vide.");
            return;
        }

        if (currentPass.isEmpty()) {
            NotificationUtil.showWarning("Champ requis", "Veuillez renseigner votre mot de passe actuel.");
            return;
        }

        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (btnSaveProfil != null) btnSaveProfil.setDisable(true);

        authService.updateProfile(currentPass, username, newPass.isEmpty() ? null : newPass)
                .whenComplete((res, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        if (btnSaveProfil != null) btnSaveProfil.setDisable(false);

                        if (throwable != null) {
                            NotificationUtil.showError("Erreur", throwable.getMessage());
                        } else {
                            NotificationUtil.showSuccess("Profil mis à jour", "Vos informations ont été enregistrées.");
                            if (txtNouveauMotDePasse != null) txtNouveauMotDePasse.clear();
                        }
                    });
                });
    }

    @FXML
    private void handleSaveEglise(ActionEvent event) {
        if (txtNomEglise != null && !txtNomEglise.getText().trim().isEmpty()) {
            AppConfig.setChurchName(txtNomEglise.getText().trim());
        }
        if (txtAdresseEglise != null) {
            AppConfig.setChurchAddress(txtAdresseEglise.getText().trim());
        }
        if (txtTelephoneEglise != null) {
            AppConfig.setChurchPhone(txtTelephoneEglise.getText().trim());
        }
        if (txtNumeroEnregistrement != null) {
            AppConfig.setChurchRegNumber(txtNumeroEnregistrement.getText().trim());
        }
        if (txtApiBaseUrl != null && !txtApiBaseUrl.getText().trim().isEmpty()) {
            AppConfig.setApiBaseUrl(txtApiBaseUrl.getText().trim());
        }

        NotificationUtil.showSuccess("Paramètres enregistrés", "Les informations de l'église et la configuration API ont été mises à jour.");
    }
}
