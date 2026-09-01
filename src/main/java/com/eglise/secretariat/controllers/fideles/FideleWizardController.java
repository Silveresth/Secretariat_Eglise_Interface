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
        if (!validateStep(currentStep)) return;

        FideleDto dto = buildDtoFromForm();

        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (submitBtn != null) submitBtn.setDisable(true);

        fideleService.createFidele(dto)
                .whenComplete((created, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        if (submitBtn != null) submitBtn.setDisable(false);

                        if (throwable != null) {
                            NotificationUtil.showError("Erreur d'inscription", throwable.getMessage());
                        } else {
                            NotificationUtil.showSuccess("Inscription réussie", "Le fidèle " + dto.getNomComplet() + " a été enregistré avec succès.");
                            Map<String, Object> params = new HashMap<>();
                            params.put("fideleId", created.getId());
                            navigationService.navigateToContent(NavigationService.View.FIDELE_DETAILS, params);
                        }
                    });
                });
    }

    private boolean validateStep(int step) {
        if (step == 1) {
            if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
                NotificationUtil.showWarning("Champ requis", "Le nom de famille est obligatoire.");
                txtNom.requestFocus();
                return false;
            }
            if (txtPrenoms.getText() == null || txtPrenoms.getText().trim().isEmpty()) {
                NotificationUtil.showWarning("Champ requis", "Le(s) prénom(s) sont obligatoires.");
                txtPrenoms.requestFocus();
                return false;
            }
        }
        return true;
    }

    private FideleDto buildDtoFromForm() {
        FideleDto dto = new FideleDto();
        dto.setNom(txtNom.getText() != null ? txtNom.getText().trim().toUpperCase() : "");
        dto.setPrenoms(txtPrenoms.getText() != null ? txtPrenoms.getText().trim() : "");
        dto.setSexe(comboSexe.getValue());
        dto.setDateNaissance(dpDateNaissance.getValue());
        dto.setLieuNaissance(txtLieuNaissance.getText());
        dto.setEthnie(txtEthnie.getText());
        dto.setProfession(txtProfession.getText());
        dto.setNiveauEtude(txtNiveauEtude.getText());

        dto.setTelephone(txtTelephone.getText());
        dto.setContactMoov(txtContactMoov.getText());
        dto.setEmail(txtEmail.getText());
        dto.setQuartier(txtQuartier.getText());
        dto.setAdresse(txtAdresse.getText());
        dto.setPrefectureRegion(txtPrefectureRegion.getText());
        dto.setNomPere(txtNomPere.getText());
        dto.setPrenomPere(txtPrenomPere.getText());
        dto.setNomMere(txtNomMere.getText());
        dto.setPrenomMere(txtPrenomMere.getText());

        dto.setStatutMatrimonial(comboStatutMatrimonial.getValue());
        dto.setDateMariage(dpDateMariage.getValue());
        dto.setEgliseMariage(txtEgliseMariage.getText());
        dto.setPasteurMariage(txtPasteurMariage.getText());
        dto.setNomConjoint(txtNomConjoint.getText());
        dto.setConfessionFoiConjoint(txtConfessionConjoint.getText());
        if (spinGarcons != null && spinGarcons.getValue() != null) dto.setNombreGarcons(spinGarcons.getValue());
        if (spinFilles != null && spinFilles.getValue() != null) dto.setNombreFilles(spinFilles.getValue());

        dto.setDateConversion(dpDateConversion.getValue());
        dto.setEgliseConversion(txtEgliseConversion.getText());
        dto.setBaptise(chkBaptise.isSelected());
        dto.setDateBapteme(dpDateBapteme.getValue());
        dto.setLieuBapteme(txtLieuBapteme.getText());
        dto.setPasteurBapteme(txtPasteurBapteme.getText());
        dto.setDateBaptemeEsprit(dpDateBaptemeEsprit.getValue());
        dto.setAncienneDenomination(txtAncienneDenomination.getText());
        dto.setLettreRecommandationPresentee(chkLettreRecommandation.isSelected());
        dto.setDateIntegrationAdidogome(dpDateIntegration.getValue());

        dto.setCarteMembreValide(chkCarteMembre.isSelected());
        dto.setCarnetDimeValide(chkCarnetDime.isSelected());
        dto.setPayeDimes(chkPayeDimes.isSelected());
        dto.setFrequenceDime(comboFrequenceDime.getValue());
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
