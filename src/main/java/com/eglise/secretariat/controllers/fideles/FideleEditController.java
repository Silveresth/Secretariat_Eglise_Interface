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
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FideleEditController extends BaseController {

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
    @FXML private Button saveBtn;
    @FXML private ProgressIndicator loadingIndicator;

    // Inline Error Labels
    @FXML private Label lblErrNom;
    @FXML private Label lblErrPrenoms;
    @FXML private Label lblErrTelephone;
    @FXML private Label lblErrQuartier;

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
    @FXML private CheckBox chkLettreRecommandation;
    @FXML private DatePicker dpDateIntegration;
    @FXML private CheckBox chkCarteMembre;
    @FXML private CheckBox chkCarnetDime;
    @FXML private CheckBox chkPayeDimes;
    @FXML private ComboBox<FrequenceDime> comboFrequenceDime;

    private int currentStep = 1;
    private final FideleService fideleService = new FideleService();
    private Long fideleId;
    private FideleDto existingFidele;

    @FXML
    public void initialize() {
        if (comboSexe != null) comboSexe.setItems(FXCollections.observableArrayList(Sexe.values()));
        if (comboStatutMatrimonial != null) comboStatutMatrimonial.setItems(FXCollections.observableArrayList(Statut.values()));
        if (comboFrequenceDime != null) comboFrequenceDime.setItems(FXCollections.observableArrayList(FrequenceDime.values()));

        if (spinGarcons != null) spinGarcons.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        if (spinFilles != null) spinFilles.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        setupRealtimeValidation();

        Object param = navigationService.getParameter("fideleId");
        if (param != null) {
            this.fideleId = Long.valueOf(String.valueOf(param));
            navigationService.clearParameters();
            loadExistingFidele(this.fideleId);
        }

        updateStepView();
    }

    private void setupRealtimeValidation() {
        if (txtNom != null) {
            txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    if (lblErrNom != null) { lblErrNom.setVisible(false); lblErrNom.setManaged(false); }
                    txtNom.getStyleClass().remove("text-input-error");
                }
            });
        }
        if (txtPrenoms != null) {
            txtPrenoms.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    if (lblErrPrenoms != null) { lblErrPrenoms.setVisible(false); lblErrPrenoms.setManaged(false); }
                    txtPrenoms.getStyleClass().remove("text-input-error");
                }
            });
        }
        if (txtTelephone != null) {
            txtTelephone.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    if (lblErrTelephone != null) { lblErrTelephone.setVisible(false); lblErrTelephone.setManaged(false); }
                    txtTelephone.getStyleClass().remove("text-input-error");
                }
            });
        }
        if (txtQuartier != null) {
            txtQuartier.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.trim().isEmpty()) {
                    if (lblErrQuartier != null) { lblErrQuartier.setVisible(false); lblErrQuartier.setManaged(false); }
                    txtQuartier.getStyleClass().remove("text-input-error");
                }
            });
        }
    }

    private void clearInlineErrors() {
        if (lblErrNom != null) { lblErrNom.setVisible(false); lblErrNom.setManaged(false); }
        if (lblErrPrenoms != null) { lblErrPrenoms.setVisible(false); lblErrPrenoms.setManaged(false); }
        if (lblErrTelephone != null) { lblErrTelephone.setVisible(false); lblErrTelephone.setManaged(false); }
        if (lblErrQuartier != null) { lblErrQuartier.setVisible(false); lblErrQuartier.setManaged(false); }

        if (txtNom != null) txtNom.getStyleClass().remove("text-input-error");
        if (txtPrenoms != null) txtPrenoms.getStyleClass().remove("text-input-error");
        if (txtTelephone != null) txtTelephone.getStyleClass().remove("text-input-error");
        if (txtQuartier != null) txtQuartier.getStyleClass().remove("text-input-error");
    }

    private void loadExistingFidele(Long id) {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        fideleService.getFideleById(id)
                .whenComplete((fidele, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);

                        if (throwable == null && fidele != null) {
                            this.existingFidele = fidele;
                            populateFields(fidele);
                        } else {
                            NotificationUtil.showError("Erreur", "Impossible de charger les données du fidèle.");
                        }
                    });
                });
    }

    private void populateFields(FideleDto f) {
        if (txtNom != null) txtNom.setText(f.getNom());
        if (txtPrenoms != null) txtPrenoms.setText(f.getPrenoms());
        if (comboSexe != null) comboSexe.setValue(f.getSexe());
        if (dpDateNaissance != null) dpDateNaissance.setValue(f.getDateNaissance());
        if (txtLieuNaissance != null) txtLieuNaissance.setText(f.getLieuNaissance());
        if (txtEthnie != null) txtEthnie.setText(f.getEthnie());
        if (txtProfession != null) txtProfession.setText(f.getProfession());
        if (txtNiveauEtude != null) txtNiveauEtude.setText(f.getNiveauEtude());

        if (txtTelephone != null) txtTelephone.setText(f.getTelephone());
        if (txtContactMoov != null) txtContactMoov.setText(f.getContactMoov());
        if (txtEmail != null) txtEmail.setText(f.getEmail());
        if (txtQuartier != null) txtQuartier.setText(f.getQuartier());
        if (txtAdresse != null) txtAdresse.setText(f.getAdresse());
        if (txtPrefectureRegion != null) txtPrefectureRegion.setText(f.getPrefectureRegion());
        if (txtNomPere != null) txtNomPere.setText(f.getNomPere());
        if (txtPrenomPere != null) txtPrenomPere.setText(f.getPrenomPere());
        if (txtNomMere != null) txtNomMere.setText(f.getNomMere());
        if (txtPrenomMere != null) txtPrenomMere.setText(f.getPrenomMere());

        if (comboStatutMatrimonial != null) comboStatutMatrimonial.setValue(f.getStatutMatrimonial());
        if (dpDateMariage != null) dpDateMariage.setValue(f.getDateMariage());
        if (txtNomConjoint != null) txtNomConjoint.setText(f.getNomConjoint());
        if (txtConfessionConjoint != null) txtConfessionConjoint.setText(f.getConfessionFoiConjoint());
        if (txtEgliseMariage != null) txtEgliseMariage.setText(f.getEgliseMariage());
        if (txtPasteurMariage != null) txtPasteurMariage.setText(f.getPasteurMariage());
        if (spinGarcons != null && f.getNombreGarcons() != null) spinGarcons.getValueFactory().setValue(f.getNombreGarcons());
        if (spinFilles != null && f.getNombreFilles() != null) spinFilles.getValueFactory().setValue(f.getNombreFilles());

        if (dpDateConversion != null) dpDateConversion.setValue(f.getDateConversion());
        if (txtEgliseConversion != null) txtEgliseConversion.setText(f.getEgliseConversion());
        if (chkBaptise != null) chkBaptise.setSelected(f.isBaptise());
        if (dpDateBapteme != null) dpDateBapteme.setValue(f.getDateBapteme());
        if (txtLieuBapteme != null) txtLieuBapteme.setText(f.getLieuBapteme());
        if (txtPasteurBapteme != null) txtPasteurBapteme.setText(f.getPasteurBapteme());
        if (dpDateBaptemeEsprit != null) dpDateBaptemeEsprit.setValue(f.getDateBaptemeEsprit());
        if (chkLettreRecommandation != null) chkLettreRecommandation.setSelected(Boolean.TRUE.equals(f.getLettreRecommandationPresentee()));
        if (dpDateIntegration != null) dpDateIntegration.setValue(f.getDateIntegrationAdidogome());

        if (chkCarteMembre != null) chkCarteMembre.setSelected(Boolean.TRUE.equals(f.getCarteMembreValide()));
        if (chkCarnetDime != null) chkCarnetDime.setSelected(Boolean.TRUE.equals(f.getCarnetDimeValide()));
        if (chkPayeDimes != null) chkPayeDimes.setSelected(Boolean.TRUE.equals(f.getPayeDimes()));
        if (comboFrequenceDime != null) comboFrequenceDime.setValue(f.getFrequenceDime());
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
        if (fideleId != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("fideleId", fideleId);
            navigationService.navigateToContent(NavigationService.View.FIDELE_DETAILS, params);
        } else {
            navigationService.navigateToContent(NavigationService.View.FIDELES_LIST);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateAllSteps()) return;

        if (existingFidele == null) existingFidele = new FideleDto();

        existingFidele.setNom(txtNom.getText().trim().toUpperCase());
        existingFidele.setPrenoms(txtPrenoms.getText() != null ? txtPrenoms.getText().trim() : "");
        existingFidele.setSexe(comboSexe != null && comboSexe.getValue() != null ? comboSexe.getValue() : Sexe.MASCULIN);
        existingFidele.setDateNaissance(dpDateNaissance != null ? dpDateNaissance.getValue() : null);
        existingFidele.setLieuNaissance(txtLieuNaissance != null ? txtLieuNaissance.getText().trim() : "");
        existingFidele.setEthnie(txtEthnie != null ? txtEthnie.getText().trim() : "");
        existingFidele.setProfession(txtProfession != null ? txtProfession.getText().trim() : "");
        existingFidele.setNiveauEtude(txtNiveauEtude != null ? txtNiveauEtude.getText().trim() : "");

        existingFidele.setTelephone(txtTelephone != null ? txtTelephone.getText().trim() : "");
        existingFidele.setContactMoov(txtContactMoov != null ? txtContactMoov.getText().trim() : "");
        existingFidele.setEmail(txtEmail != null ? txtEmail.getText().trim() : "");
        existingFidele.setQuartier(txtQuartier != null && !txtQuartier.getText().isBlank() ? txtQuartier.getText().trim() : "Adidogomé");
        existingFidele.setAdresse(txtAdresse != null ? txtAdresse.getText().trim() : "");
        existingFidele.setPrefectureRegion(txtPrefectureRegion != null ? txtPrefectureRegion.getText().trim() : "");
        existingFidele.setNomPere(txtNomPere != null ? txtNomPere.getText().trim() : "");
        existingFidele.setPrenomPere(txtPrenomPere != null ? txtPrenomPere.getText().trim() : "");
        existingFidele.setNomMere(txtNomMere != null ? txtNomMere.getText().trim() : "");
        existingFidele.setPrenomMere(txtPrenomMere != null ? txtPrenomMere.getText().trim() : "");

        existingFidele.setStatutMatrimonial(comboStatutMatrimonial != null && comboStatutMatrimonial.getValue() != null ? comboStatutMatrimonial.getValue() : Statut.CELIBATAIRE);
        existingFidele.setDateMariage(dpDateMariage != null ? dpDateMariage.getValue() : null);
        existingFidele.setEgliseMariage(txtEgliseMariage != null ? txtEgliseMariage.getText().trim() : "");
        existingFidele.setPasteurMariage(txtPasteurMariage != null ? txtPasteurMariage.getText().trim() : "");
        existingFidele.setNomConjoint(txtNomConjoint != null ? txtNomConjoint.getText().trim() : "");
        existingFidele.setConfessionFoiConjoint(txtConfessionConjoint != null ? txtConfessionConjoint.getText().trim() : "");
        if (spinGarcons != null) existingFidele.setNombreGarcons(spinGarcons.getValue());
        if (spinFilles != null) existingFidele.setNombreFilles(spinFilles.getValue());

        existingFidele.setDateConversion(dpDateConversion != null ? dpDateConversion.getValue() : null);
        existingFidele.setEgliseConversion(txtEgliseConversion != null ? txtEgliseConversion.getText().trim() : "");
        existingFidele.setBaptise(chkBaptise != null && chkBaptise.isSelected());
        existingFidele.setDateBapteme(dpDateBapteme != null ? dpDateBapteme.getValue() : null);
        existingFidele.setLieuBapteme(txtLieuBapteme != null ? txtLieuBapteme.getText().trim() : "");
        existingFidele.setPasteurBapteme(txtPasteurBapteme != null ? txtPasteurBapteme.getText().trim() : "");
        existingFidele.setDateBaptemeEsprit(dpDateBaptemeEsprit != null ? dpDateBaptemeEsprit.getValue() : null);
        existingFidele.setLettreRecommandationPresentee(chkLettreRecommandation != null && chkLettreRecommandation.isSelected());
        existingFidele.setDateIntegrationAdidogome(dpDateIntegration != null && dpDateIntegration.getValue() != null ? dpDateIntegration.getValue() : LocalDate.now());

        existingFidele.setCarteMembreValide(chkCarteMembre != null ? chkCarteMembre.isSelected() : true);
        existingFidele.setCarnetDimeValide(chkCarnetDime != null ? chkCarnetDime.isSelected() : true);
        existingFidele.setPayeDimes(chkPayeDimes != null ? chkPayeDimes.isSelected() : true);
        existingFidele.setFrequenceDime(comboFrequenceDime != null && comboFrequenceDime.getValue() != null ? comboFrequenceDime.getValue() : FrequenceDime.REGULIEREMENT);

        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (saveBtn != null) saveBtn.setDisable(true);

        fideleService.updateFidele(fideleId, existingFidele)
                .whenComplete((updated, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        if (saveBtn != null) saveBtn.setDisable(false);

                        if (throwable != null) {
                            NotificationUtil.showError("Erreur modification", throwable.getMessage());
                        } else {
                            Map<String, Object> params = new HashMap<>();
                            params.put("fideleId", fideleId);
                            params.put("showSuccessBanner", true);
                            params.put("isModification", true);
                            navigationService.navigateToContent(NavigationService.View.FIDELE_DETAILS, params);
                        }
                    });
                });
    }

    private boolean validateStep(int step) {
        clearInlineErrors();
        boolean isValid = true;

        if (step == 1) {
            if (txtNom == null || txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
                if (lblErrNom != null) { lblErrNom.setVisible(true); lblErrNom.setManaged(true); }
                if (txtNom != null) {
                    if (!txtNom.getStyleClass().contains("text-input-error")) txtNom.getStyleClass().add("text-input-error");
                    txtNom.requestFocus();
                }
                isValid = false;
            }
            if (txtPrenoms == null || txtPrenoms.getText() == null || txtPrenoms.getText().trim().isEmpty()) {
                if (lblErrPrenoms != null) { lblErrPrenoms.setVisible(true); lblErrPrenoms.setManaged(true); }
                if (txtPrenoms != null) {
                    if (!txtPrenoms.getStyleClass().contains("text-input-error")) txtPrenoms.getStyleClass().add("text-input-error");
                    if (isValid) txtPrenoms.requestFocus();
                }
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean validateAllSteps() {
        clearInlineErrors();
        boolean isValid = true;

        if (txtNom == null || txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            if (lblErrNom != null) { lblErrNom.setVisible(true); lblErrNom.setManaged(true); }
            if (txtNom != null && !txtNom.getStyleClass().contains("text-input-error")) txtNom.getStyleClass().add("text-input-error");
            isValid = false;
        }
        if (txtPrenoms == null || txtPrenoms.getText() == null || txtPrenoms.getText().trim().isEmpty()) {
            if (lblErrPrenoms != null) { lblErrPrenoms.setVisible(true); lblErrPrenoms.setManaged(true); }
            if (txtPrenoms != null && !txtPrenoms.getStyleClass().contains("text-input-error")) txtPrenoms.getStyleClass().add("text-input-error");
            isValid = false;
        }

        if (!isValid) {
            currentStep = 1;
            updateStepView();
            if (txtNom != null) txtNom.requestFocus();
        }
        return isValid;
    }

    private void updateStepView() {
        if (step1Panel != null) { step1Panel.setVisible(currentStep == 1); step1Panel.setManaged(currentStep == 1); }
        if (step2Panel != null) { step2Panel.setVisible(currentStep == 2); step2Panel.setManaged(currentStep == 2); }
        if (step3Panel != null) { step3Panel.setVisible(currentStep == 3); step3Panel.setManaged(currentStep == 3); }
        if (step4Panel != null) { step4Panel.setVisible(currentStep == 4); step4Panel.setManaged(currentStep == 4); }

        updateCircle(step1Circle, currentStep >= 1, currentStep == 1);
        updateCircle(step2Circle, currentStep >= 2, currentStep == 2);
        updateCircle(step3Circle, currentStep >= 3, currentStep == 3);
        updateCircle(step4Circle, currentStep >= 4, currentStep == 4);

        if (prevStepBtn != null) prevStepBtn.setDisable(currentStep == 1);
        if (nextStepBtn != null) {
            nextStepBtn.setVisible(currentStep < 4);
            nextStepBtn.setManaged(currentStep < 4);
        }
        if (saveBtn != null) {
            saveBtn.setVisible(currentStep == 4);
            saveBtn.setManaged(currentStep == 4);
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
