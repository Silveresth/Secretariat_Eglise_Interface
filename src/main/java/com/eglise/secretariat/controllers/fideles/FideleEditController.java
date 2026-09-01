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

import java.util.HashMap;
import java.util.Map;

public class FideleEditController extends BaseController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenoms;
    @FXML private ComboBox<Sexe> comboSexe;
    @FXML private DatePicker dpDateNaissance;
    @FXML private TextField txtLieuNaissance;
    @FXML private TextField txtProfession;
    @FXML private TextField txtNiveauEtude;

    @FXML private TextField txtTelephone;
    @FXML private TextField txtContactMoov;
    @FXML private TextField txtEmail;
    @FXML private TextField txtQuartier;
    @FXML private TextField txtAdresse;

    @FXML private ComboBox<Statut> comboStatutMatrimonial;
    @FXML private TextField txtNomConjoint;
    @FXML private Spinner<Integer> spinGarcons;
    @FXML private Spinner<Integer> spinFilles;

    @FXML private CheckBox chkBaptise;
    @FXML private DatePicker dpDateBapteme;
    @FXML private TextField txtLieuBapteme;
    @FXML private CheckBox chkCarteMembre;
    @FXML private CheckBox chkCarnetDime;
    @FXML private CheckBox chkPayeDimes;
    @FXML private ComboBox<FrequenceDime> comboFrequenceDime;

    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button saveBtn;

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

        Object param = navigationService.getParameter("fideleId");
        if (param != null) {
            this.fideleId = Long.valueOf(String.valueOf(param));
            navigationService.clearParameters();
            loadExistingFidele(this.fideleId);
        }
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
        if (txtProfession != null) txtProfession.setText(f.getProfession());
        if (txtNiveauEtude != null) txtNiveauEtude.setText(f.getNiveauEtude());

        if (txtTelephone != null) txtTelephone.setText(f.getTelephone());
        if (txtContactMoov != null) txtContactMoov.setText(f.getContactMoov());
        if (txtEmail != null) txtEmail.setText(f.getEmail());
        if (txtQuartier != null) txtQuartier.setText(f.getQuartier());
        if (txtAdresse != null) txtAdresse.setText(f.getAdresse());

        if (comboStatutMatrimonial != null) comboStatutMatrimonial.setValue(f.getStatutMatrimonial());
        if (txtNomConjoint != null) txtNomConjoint.setText(f.getNomConjoint());
        if (spinGarcons != null && f.getNombreGarcons() != null) spinGarcons.getValueFactory().setValue(f.getNombreGarcons());
        if (spinFilles != null && f.getNombreFilles() != null) spinFilles.getValueFactory().setValue(f.getNombreFilles());

        if (chkBaptise != null) chkBaptise.setSelected(f.isBaptise());
        if (dpDateBapteme != null) dpDateBapteme.setValue(f.getDateBapteme());
        if (txtLieuBapteme != null) txtLieuBapteme.setText(f.getLieuBapteme());

        if (chkCarteMembre != null) chkCarteMembre.setSelected(Boolean.TRUE.equals(f.getCarteMembreValide()));
        if (chkCarnetDime != null) chkCarnetDime.setSelected(Boolean.TRUE.equals(f.getCarnetDimeValide()));
        if (chkPayeDimes != null) chkPayeDimes.setSelected(Boolean.TRUE.equals(f.getPayeDimes()));
        if (comboFrequenceDime != null) comboFrequenceDime.setValue(f.getFrequenceDime());
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
        if (txtNom == null || txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            NotificationUtil.showWarning("Validation", "Le nom de famille est obligatoire.");
            if (txtNom != null) txtNom.requestFocus();
            return;
        }
        if (txtPrenoms == null || txtPrenoms.getText() == null || txtPrenoms.getText().trim().isEmpty()) {
            NotificationUtil.showWarning("Validation", "Le prénom est obligatoire.");
            if (txtPrenoms != null) txtPrenoms.requestFocus();
            return;
        }

        if (existingFidele == null) existingFidele = new FideleDto();

        existingFidele.setNom(txtNom.getText().trim().toUpperCase());
        existingFidele.setPrenoms(txtPrenoms.getText() != null ? txtPrenoms.getText().trim() : "");
        existingFidele.setSexe(comboSexe.getValue());
        existingFidele.setDateNaissance(dpDateNaissance.getValue());
        existingFidele.setLieuNaissance(txtLieuNaissance.getText());
        existingFidele.setProfession(txtProfession.getText());
        existingFidele.setNiveauEtude(txtNiveauEtude.getText());

        existingFidele.setTelephone(txtTelephone.getText());
        existingFidele.setContactMoov(txtContactMoov.getText());
        existingFidele.setEmail(txtEmail.getText());
        existingFidele.setQuartier(txtQuartier.getText());
        existingFidele.setAdresse(txtAdresse.getText());

        existingFidele.setStatutMatrimonial(comboStatutMatrimonial.getValue());
        existingFidele.setNomConjoint(txtNomConjoint.getText());
        if (spinGarcons != null) existingFidele.setNombreGarcons(spinGarcons.getValue());
        if (spinFilles != null) existingFidele.setNombreFilles(spinFilles.getValue());

        existingFidele.setBaptise(chkBaptise.isSelected());
        existingFidele.setDateBapteme(dpDateBapteme.getValue());
        existingFidele.setLieuBapteme(txtLieuBapteme.getText());

        existingFidele.setCarteMembreValide(chkCarteMembre.isSelected());
        existingFidele.setCarnetDimeValide(chkCarnetDime.isSelected());
        existingFidele.setPayeDimes(chkPayeDimes.isSelected());
        existingFidele.setFrequenceDime(comboFrequenceDime.getValue());

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
                            NotificationUtil.showSuccess("Modification enregistrée", "La fiche du fidèle a été mise à jour.");
                            Map<String, Object> params = new HashMap<>();
                            params.put("fideleId", fideleId);
                            navigationService.navigateToContent(NavigationService.View.FIDELE_DETAILS, params);
                        }
                    });
                });
    }
}
