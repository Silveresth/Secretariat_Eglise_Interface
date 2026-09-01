package com.eglise.secretariat.controllers.fideles;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.models.enums.FrequenceDime;
import com.eglise.secretariat.models.enums.Sexe;
import com.eglise.secretariat.models.enums.Statut;
import com.eglise.secretariat.services.FideleService;
import com.eglise.secretariat.utils.NavigationService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FideleWizardController extends BaseController {

    // Stepper indicators
    @FXML private Label step1Circle;
    @FXML private Label step2Circle;
    @FXML private Label step3Circle;
    @FXML private Label step4Circle;

    // Step panels
    @FXML private VBox step1Panel;
    @FXML private VBox step2Panel;
    @FXML private VBox step3Panel;
    @FXML private VBox step4Panel;

    // Actions
    @FXML private Button prevStepBtn;
    @FXML private Button nextStepBtn;
    @FXML private Button submitBtn;
    @FXML private ProgressIndicator loadingIndicator;

    // Step 1 Fields : Identité
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenoms;
    @FXML private ComboBox<Sexe> comboSexe;
    @FXML private DatePicker dpDateNaissance;
    @FXML private TextField txtLieuNaissance;
    @FXML private TextField txtEthnie;
    @FXML private TextField txtProfession;
    @FXML private TextField txtNiveauEtude;

    // Step 2 Fields : Contacts & Filiation
    @FXML private TextField txtTelephone;
    @FXML private TextField txtContactMoov;
    @FXML private TextField txtEmail;
    @FXML private TextField txtQuartier;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtPrefectureRegion;
    @FXML private TextField txtNomPere;
    @FXML private TextField txtPrenomPere;
    @FXML private TextField txtNomMere;
    @FXML private TextField txtPrenomMere;

    // Step 3 Fields : Matrimoniale & Famille
    @FXML private ComboBox<Statut> comboStatutMatrimonial;
    @FXML private DatePicker dpDateMariage;
    @FXML private TextField txtEgliseMariage;
    @FXML private TextField txtPasteurMariage;
    @FXML private TextField txtNomConjoint;
    @FXML private TextField txtConfessionConjoint;
    @FXML private Spinner<Integer> spinGarcons;
    @FXML private Spinner<Integer> spinFilles;

    // Step 4 Fields : Spirituel & Dîme
    @FXML private DatePicker dpDateConversion;
    @FXML private TextField txtEgliseConversion;
    @FXML private CheckBox chkBaptise;
    @FXML private DatePicker dpDateBapteme;
    @FXML private TextField txtLieuBapteme;
    @FXML private TextField txtPasteurBapteme;
    @FXML private DatePicker dpDateBaptemeEsprit;
    @FXML private TextField txtAncienneDenomination;
    @FXML private CheckBox chkLettreRecommandation;
    @FXML private DatePicker dpDateIntegration;
    @FXML private CheckBox chkCarteMembre;
    @FXML private CheckBox chkCarnetDime;
    @FXML private CheckBox chkPayeDimes;
    @FXML private ComboBox<FrequenceDime> comboFrequenceDime;

    private int currentStep = 1;
    private final FideleService fideleService = new FideleService();

    @FXML
    public void initialize() {
        // Enums ComboBox setup
        if (comboSexe != null) {
            comboSexe.setItems(FXCollections.observableArrayList(Sexe.values()));
            comboSexe.setValue(Sexe.MASCULIN);
        }
        if (comboStatutMatrimonial != null) {
            comboStatutMatrimonial.setItems(FXCollections.observableArrayList(Statut.values()));
            comboStatutMatrimonial.setValue(Statut.CELIBATAIRE);
        }
        if (comboFrequenceDime != null) {
            comboFrequenceDime.setItems(FXCollections.observableArrayList(FrequenceDime.values()));
            comboFrequenceDime.setValue(FrequenceDime.REGULIEREMENT);
        }

        // Spinners
        if (spinGarcons != null) spinGarcons.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        if (spinFilles != null) spinFilles.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        // Default integration date = today
        if (dpDateIntegration != null) dpDateIntegration.setValue(LocalDate.now());

        updateStepView();
    }

    @FXML
    private void handleNextStep(ActionEvent event) {
        if (validateStep(currentStep)) {
            if (currentStep < 4) {
                currentStep++;
                updateStepView();
            }
        }
    }

    @FXML
    private void handlePrevStep(ActionEvent event) {
        if (currentStep > 1) {
            currentStep--;
            updateStepView();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigationService.navigateToContent(NavigationService.View.FIDELES_LIST);
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        if (!validateAllSteps()) return;

        FideleDto dto = buildDtoFromForm();

        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (submitBtn != null) submitBtn.setDisable(true);

        fideleService.createFidele(dto)
                .whenComplete((created, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        if (submitBtn != null) submitBtn.setDisable(false);

                        if (throwable != null) {
                            String errorMsg = throwable.getMessage();
                            if (throwable.getCause() != null && throwable.getCause().getMessage() != null) {
                                errorMsg = throwable.getCause().getMessage();
                            }
                            NotificationUtil.showError("Erreur d'inscription", errorMsg != null ? errorMsg : "Impossible d'enregistrer le fidèle.");
                        } else if (created != null && created.getId() != null) {
                            NotificationUtil.showSuccess("Inscription réussie", "Le fidèle " + dto.getNomComplet() + " a été enregistré avec succès.");
                            Map<String, Object> params = new HashMap<>();
                            params.put("fideleId", created.getId());
                            navigationService.navigateToContent(NavigationService.View.FIDELE_DETAILS, params);
                        } else {
                            NotificationUtil.showSuccess("Inscription réussie", "Le fidèle a été enregistré avec succès.");
                            navigationService.navigateToContent(NavigationService.View.FIDELES_LIST);
                        }
                    });
                });
    }

    private boolean validateStep(int step) {
        if (step == 1) {
            if (txtNom == null || txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
                NotificationUtil.showWarning("Champ requis", "Le nom de famille est obligatoire.");
                if (txtNom != null) txtNom.requestFocus();
                return false;
            }
            if (txtPrenoms == null || txtPrenoms.getText() == null || txtPrenoms.getText().trim().isEmpty()) {
                NotificationUtil.showWarning("Champ requis", "Le(s) prénom(s) sont obligatoires.");
                if (txtPrenoms != null) txtPrenoms.requestFocus();
                return false;
            }
        }
        return true;
    }

    private boolean validateAllSteps() {
        if (txtNom == null || txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            NotificationUtil.showWarning("Champ requis", "Le nom de famille est obligatoire (Étape 1).");
            currentStep = 1;
            updateStepView();
            if (txtNom != null) txtNom.requestFocus();
            return false;
        }
        if (txtPrenoms == null || txtPrenoms.getText() == null || txtPrenoms.getText().trim().isEmpty()) {
            NotificationUtil.showWarning("Champ requis", "Le(s) prénom(s) sont obligatoires (Étape 1).");
            currentStep = 1;
            updateStepView();
            if (txtPrenoms != null) txtPrenoms.requestFocus();
            return false;
        }
        return true;
    }

    private FideleDto buildDtoFromForm() {
        FideleDto dto = new FideleDto();
        dto.setNom(txtNom != null && txtNom.getText() != null ? txtNom.getText().trim().toUpperCase() : "");
        dto.setPrenoms(txtPrenoms != null && txtPrenoms.getText() != null ? txtPrenoms.getText().trim() : "");
        dto.setSexe(comboSexe != null && comboSexe.getValue() != null ? comboSexe.getValue() : Sexe.MASCULIN);
        dto.setDateNaissance(dpDateNaissance != null ? dpDateNaissance.getValue() : null);
        dto.setLieuNaissance(txtLieuNaissance != null && txtLieuNaissance.getText() != null ? txtLieuNaissance.getText().trim() : "");
        dto.setEthnie(txtEthnie != null && txtEthnie.getText() != null ? txtEthnie.getText().trim() : "");
        dto.setProfession(txtProfession != null && txtProfession.getText() != null ? txtProfession.getText().trim() : "");
        dto.setNiveauEtude(txtNiveauEtude != null && txtNiveauEtude.getText() != null ? txtNiveauEtude.getText().trim() : "");

        dto.setTelephone(txtTelephone != null && txtTelephone.getText() != null ? txtTelephone.getText().trim() : "");
        dto.setContactMoov(txtContactMoov != null && txtContactMoov.getText() != null ? txtContactMoov.getText().trim() : "");
        dto.setEmail(txtEmail != null && txtEmail.getText() != null ? txtEmail.getText().trim() : "");
        dto.setQuartier(txtQuartier != null && txtQuartier.getText() != null && !txtQuartier.getText().isBlank() ? txtQuartier.getText().trim() : "Adidogomé");
        dto.setAdresse(txtAdresse != null && txtAdresse.getText() != null ? txtAdresse.getText().trim() : "");
        dto.setPrefectureRegion(txtPrefectureRegion != null && txtPrefectureRegion.getText() != null ? txtPrefectureRegion.getText().trim() : "");
        dto.setNomPere(txtNomPere != null && txtNomPere.getText() != null ? txtNomPere.getText().trim() : "");
        dto.setPrenomPere(txtPrenomPere != null && txtPrenomPere.getText() != null ? txtPrenomPere.getText().trim() : "");
        dto.setNomMere(txtNomMere != null && txtNomMere.getText() != null ? txtNomMere.getText().trim() : "");
        dto.setPrenomMere(txtPrenomMere != null && txtPrenomMere.getText() != null ? txtPrenomMere.getText().trim() : "");

        dto.setStatutMatrimonial(comboStatutMatrimonial != null && comboStatutMatrimonial.getValue() != null ? comboStatutMatrimonial.getValue() : Statut.CELIBATAIRE);
        dto.setDateMariage(dpDateMariage != null ? dpDateMariage.getValue() : null);
        dto.setEgliseMariage(txtEgliseMariage != null && txtEgliseMariage.getText() != null ? txtEgliseMariage.getText().trim() : "");
        dto.setPasteurMariage(txtPasteurMariage != null && txtPasteurMariage.getText() != null ? txtPasteurMariage.getText().trim() : "");
        dto.setNomConjoint(txtNomConjoint != null && txtNomConjoint.getText() != null ? txtNomConjoint.getText().trim() : "");
        dto.setConfessionFoiConjoint(txtConfessionConjoint != null && txtConfessionConjoint.getText() != null ? txtConfessionConjoint.getText().trim() : "");
        dto.setNombreGarcons(spinGarcons != null && spinGarcons.getValue() != null ? spinGarcons.getValue() : 0);
        dto.setNombreFilles(spinFilles != null && spinFilles.getValue() != null ? spinFilles.getValue() : 0);

        dto.setDateConversion(dpDateConversion != null ? dpDateConversion.getValue() : null);
        dto.setEgliseConversion(txtEgliseConversion != null && txtEgliseConversion.getText() != null ? txtEgliseConversion.getText().trim() : "");
        dto.setBaptise(chkBaptise != null && chkBaptise.isSelected());
        dto.setDateBapteme(dpDateBapteme != null ? dpDateBapteme.getValue() : null);
        dto.setLieuBapteme(txtLieuBapteme != null && txtLieuBapteme.getText() != null ? txtLieuBapteme.getText().trim() : "");
        dto.setPasteurBapteme(txtPasteurBapteme != null && txtPasteurBapteme.getText() != null ? txtPasteurBapteme.getText().trim() : "");
        dto.setDateBaptemeEsprit(dpDateBaptemeEsprit != null ? dpDateBaptemeEsprit.getValue() : null);
        dto.setAncienneDenomination(txtAncienneDenomination != null && txtAncienneDenomination.getText() != null ? txtAncienneDenomination.getText().trim() : "");
        dto.setLettreRecommandationPresentee(chkLettreRecommandation != null && chkLettreRecommandation.isSelected());
        dto.setDateIntegrationAdidogome(dpDateIntegration != null && dpDateIntegration.getValue() != null ? dpDateIntegration.getValue() : LocalDate.now());

        dto.setCarteMembreValide(chkCarteMembre != null ? chkCarteMembre.isSelected() : true);
        dto.setCarnetDimeValide(chkCarnetDime != null ? chkCarnetDime.isSelected() : true);
        dto.setPayeDimes(chkPayeDimes != null ? chkPayeDimes.isSelected() : true);
        dto.setFrequenceDime(comboFrequenceDime != null && comboFrequenceDime.getValue() != null ? comboFrequenceDime.getValue() : FrequenceDime.REGULIEREMENT);
        dto.setActif(true);

        return dto;
    }

    private void updateStepView() {
        step1Panel.setVisible(currentStep == 1);
        step1Panel.setManaged(currentStep == 1);
        step2Panel.setVisible(currentStep == 2);
        step2Panel.setManaged(currentStep == 2);
        step3Panel.setVisible(currentStep == 3);
        step3Panel.setManaged(currentStep == 3);
        step4Panel.setVisible(currentStep == 4);
        step4Panel.setManaged(currentStep == 4);

        updateCircle(step1Circle, currentStep >= 1, currentStep == 1);
        updateCircle(step2Circle, currentStep >= 2, currentStep == 2);
        updateCircle(step3Circle, currentStep >= 3, currentStep == 3);
        updateCircle(step4Circle, currentStep >= 4, currentStep == 4);

        if (prevStepBtn != null) prevStepBtn.setDisable(currentStep == 1);
        if (nextStepBtn != null) {
            nextStepBtn.setVisible(currentStep < 4);
            nextStepBtn.setManaged(currentStep < 4);
        }
        if (submitBtn != null) {
            submitBtn.setVisible(currentStep == 4);
            submitBtn.setManaged(currentStep == 4);
        }
    }

    private void updateCircle(Label circle, boolean reached, boolean active) {
        if (circle == null) return;
        circle.getStyleClass().removeAll("step-circle-active", "step-circle-done");
        if (active) {
            circle.getStyleClass().add("step-circle-active");
        } else if (reached) {
            circle.getStyleClass().add("step-circle-done");
        }
    }
}
