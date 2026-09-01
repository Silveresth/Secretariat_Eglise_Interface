package com.eglise.secretariat.controllers.documents;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.dto.LettreRecommandationRequestDto;
import com.eglise.secretariat.services.DocumentService;
import com.eglise.secretariat.services.FideleService;
import com.eglise.secretariat.utils.DateUtil;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class DocumentsController extends BaseController {

    @FXML private TabPane docTabPane;

    // Fiche d'inscription Form
    @FXML private ComboBox<FideleDto> comboFideleFiche;
    @FXML private Button btnPreviewFiche;

    // Lettre de recommandation Form
    @FXML private ComboBox<FideleDto> comboFideleLettre;
    @FXML private RadioButton radioTransfert;
    @FXML private RadioButton radioVoyage;
    @FXML private RadioButton radioTravail;
    @FXML private ToggleGroup groupMotif;
    @FXML private TextField txtEgliseDestination;
    @FXML private ComboBox<String> comboPasteurSignataire;
    @FXML private Button btnGenerateLettre;

    // Preview Lettre Labels
    @FXML private Label docObjetLabel;
    @FXML private Label docBodyLabel;
    @FXML private Label docNaissanceLabel;
    @FXML private Label docBaptemeLabel;
    @FXML private Label docEspritLabel;
    @FXML private Label docProfessionLabel;
    @FXML private Label docMatrimonialLabel;
    @FXML private Label docSexeLabel;
    @FXML private Label docMotifLabel;
    @FXML private Label docDateLabel;
    @FXML private Label docSignataireLabel;

    // Preview Fiche Labels
    @FXML private Label ficheNomLabel;
    @FXML private Label ficheNaissanceLabel;
    @FXML private Label ficheDomicileLabel;
    @FXML private Label fichePereLabel;
    @FXML private Label ficheMereLabel;
    @FXML private Label ficheMatrimonialLabel;
    @FXML private Label ficheMariageLabel;
    @FXML private Label ficheBaptemeLabel;
    @FXML private Label ficheEspritLabel;
    @FXML private Label ficheProfessionLabel;
    @FXML private Label ficheContactLabel;
    @FXML private Label ficheDateLabel;

    @FXML private ProgressIndicator loadingIndicator;

    private final FideleService fideleService = new FideleService();
    private final DocumentService documentService = new DocumentService();
    private byte[] currentPdfBytes;

    @FXML
    public void initialize() {
        if (groupMotif == null) {
            groupMotif = new ToggleGroup();
            if (radioTransfert != null) radioTransfert.setToggleGroup(groupMotif);
            if (radioVoyage != null) radioVoyage.setToggleGroup(groupMotif);
            if (radioTravail != null) radioTravail.setToggleGroup(groupMotif);
        }

        if (radioTransfert != null) radioTransfert.setOnAction(e -> updatePreviewFromForm());
        if (radioVoyage != null) radioVoyage.setOnAction(e -> updatePreviewFromForm());
        if (radioTravail != null) radioTravail.setOnAction(e -> updatePreviewFromForm());

        if (comboPasteurSignataire != null) {
            comboPasteurSignataire.setItems(FXCollections.observableArrayList(
                    "Pasteur AD",
                    "Rév. Pasteur Principal",
                    "Pasteur Adjoint",
                    "Secrétaire Général"
            ));
            comboPasteurSignataire.setValue("Pasteur AD");
            comboPasteurSignataire.setOnAction(e -> updatePreviewFromForm());
        }

        StringConverter<FideleDto> converter = new StringConverter<>() {
            @Override
            public String toString(FideleDto f) {
                if (f == null) return "";
                if (f.getTelephone() != null && !f.getTelephone().isBlank()) {
                    return f.getNomComplet() + " - " + f.getTelephone();
                }
                return f.getNomComplet();
            }

            @Override
            public FideleDto fromString(String string) {
                return null;
            }
        };

        if (comboFideleFiche != null) {
            comboFideleFiche.setConverter(converter);
            comboFideleFiche.setOnAction(e -> updateFichePreviewFromSelection());
        }
        if (comboFideleLettre != null) {
            comboFideleLettre.setConverter(converter);
            comboFideleLettre.setOnAction(e -> updatePreviewFromForm());
        }

        loadFidelesList();
        updatePreviewFromForm();
    }

    private void loadFidelesList() {
        fideleService.searchFideles(null, null, null, true, 0, 100, "nom,asc")
                .whenComplete((page, throwable) -> {
                    Platform.runLater(() -> {
                        if (throwable == null && page != null && page.getContent() != null) {
                            List<FideleDto> list = page.getContent();
                            if (comboFideleFiche != null) {
                                comboFideleFiche.setItems(FXCollections.observableArrayList(list));
                                if (!list.isEmpty()) {
                                    comboFideleFiche.setValue(list.get(0));
                                    updateFichePreviewFromSelection();
                                }
                            }
                            if (comboFideleLettre != null) {
                                comboFideleLettre.setItems(FXCollections.observableArrayList(list));
                                if (!list.isEmpty()) {
                                    comboFideleLettre.setValue(list.get(0));
                                    updatePreviewFromForm();
                                }
                            }
                        }
                    });
                });
    }

    private void updatePreviewFromForm() {
        boolean isTransfert = radioTransfert != null && radioTransfert.isSelected();
        boolean isVoyage = radioVoyage != null && radioVoyage.isSelected();
        boolean isEmploi = radioTravail != null && radioTravail.isSelected();

        if (docObjetLabel != null) {
            docObjetLabel.setText(String.format(
                    "Transfert [ %s ]   Voyage [ %s ]   Emploi [ %s ]",
                    isTransfert ? "X" : " ",
                    isVoyage ? "X" : " ",
                    isEmploi ? "X" : " "
            ));
        }

        FideleDto f = comboFideleLettre != null ? comboFideleLettre.getValue() : null;
        String nomFidele = f != null ? f.getNomComplet() : "KOFFI Jean-Baptiste";
        String pasteur = comboPasteurSignataire != null ? comboPasteurSignataire.getValue() : "Pasteur AD";

        if (docBodyLabel != null) {
            docBodyLabel.setText(
                    "Je soussigné : " + pasteur +
                    " Pasteur de l'Église des Assemblées de Dieu Temple : « DIEU NE CHANGE PAS », atteste que le ou la nommé(e) : " +
                    nomFidele + " est membre actif de notre assemblée depuis : " +
                    (f != null && f.getDateIntegrationAdidogome() != null ? DateUtil.formatShort(f.getDateIntegrationAdidogome()) : "15/01/2018") + "."
            );
        }

        if (docNaissanceLabel != null) {
            if (f != null && f.getDateNaissance() != null) {
                docNaissanceLabel.setText(DateUtil.formatShort(f.getDateNaissance()) + " à " + (f.getLieuNaissance() != null ? f.getLieuNaissance() : "Lomé"));
            } else {
                docNaissanceLabel.setText("12/05/1995 à Lomé");
            }
        }

        if (docBaptemeLabel != null) {
            if (f != null && f.getDateBapteme() != null) {
                docBaptemeLabel.setText(DateUtil.formatShort(f.getDateBapteme()));
            } else {
                docBaptemeLabel.setText("10/04/2016");
            }
        }

        if (docEspritLabel != null) {
            if (f != null && f.getDateBaptemeEsprit() != null) {
                docEspritLabel.setText(DateUtil.formatShort(f.getDateBaptemeEsprit()));
            } else {
                docEspritLabel.setText("04/06/2017");
            }
        }

        if (docProfessionLabel != null) {
            docProfessionLabel.setText(f != null && f.getProfession() != null ? f.getProfession() : "Informaticien");
        }

        if (docMatrimonialLabel != null) {
            if (f != null && f.getStatutMatrimonial() != null) {
                int enfants = (f.getNombreGarcons() != null ? f.getNombreGarcons() : 0) + (f.getNombreFilles() != null ? f.getNombreFilles() : 0);
                docMatrimonialLabel.setText("Célibataire [ " + (f.getStatutMatrimonial().name().equals("CELIBATAIRE") ? "X" : " ") + " ]   " +
                        "Marié(e) [ " + (f.getStatutMatrimonial().name().equals("MARIE") ? "X" : " ") + " ]   " +
                        "Nombre d'enfant : " + enfants);
            } else {
                docMatrimonialLabel.setText("Célibataire [ ]   Fiancé(e) [ ]   Marié(e) [ X ]   Nombre d'enfant : 3");
            }
        }

        if (docSexeLabel != null) {
            boolean isM = f == null || f.getSexe() == null || f.getSexe().name().startsWith("M");
            docSexeLabel.setText("Masculin [ " + (isM ? "X" : " ") + " ]   Féminin [ " + (!isM ? "X" : " ") + " ]");
        }

        if (docMotifLabel != null) {
            String dest = txtEgliseDestination != null && !txtEgliseDestination.getText().trim().isEmpty() ? txtEgliseDestination.getText().trim() : "Temple de Kpalimé";
            String motifText = isTransfert ? "Transfert pour raison professionnelle" : (isVoyage ? "Voyage" : "Emploi");
            docMotifLabel.setText("Motif / Église de destination : " + motifText + " - Église : " + dest);
        }

        if (docDateLabel != null) {
            docDateLabel.setText("Fait à Lomé, le " + DateUtil.formatShort(LocalDate.now()));
        }

        if (docSignataireLabel != null) {
            docSignataireLabel.setText(pasteur);
        }
    }

    private void updateFichePreviewFromSelection() {
        FideleDto f = comboFideleFiche != null ? comboFideleFiche.getValue() : null;
        if (f == null) return;

        if (ficheNomLabel != null) ficheNomLabel.setText(f.getNomComplet());
        if (ficheNaissanceLabel != null) {
            ficheNaissanceLabel.setText((f.getDateNaissance() != null ? DateUtil.formatShort(f.getDateNaissance()) : "-") +
                    " à " + (f.getLieuNaissance() != null ? f.getLieuNaissance() : "-") +
                    "  .Ethnie " + (f.getEthnie() != null ? f.getEthnie() : "-"));
        }
        if (ficheDomicileLabel != null) {
            ficheDomicileLabel.setText((f.getQuartier() != null ? f.getQuartier() : "-") +
                    "  Maison " + (f.getAdresse() != null ? f.getAdresse() : "-"));
        }
        if (fichePereLabel != null) fichePereLabel.setText((f.getNomPere() != null ? f.getNomPere() + " " + (f.getPrenomPere() != null ? f.getPrenomPere() : "") : "-"));
        if (ficheMereLabel != null) ficheMereLabel.setText((f.getNomMere() != null ? f.getNomMere() + " " + (f.getPrenomMere() != null ? f.getPrenomMere() : "") : "-"));
        if (ficheMatrimonialLabel != null) {
            int enfants = (f.getNombreGarcons() != null ? f.getNombreGarcons() : 0) + (f.getNombreFilles() != null ? f.getNombreFilles() : 0);
            ficheMatrimonialLabel.setText((f.getSexe() != null ? f.getSexe().name() : "MASCULIN") + "   " +
                    (f.getStatutMatrimonial() != null ? f.getStatutMatrimonial().getLabel() : "Célibataire") +
                    "   Enfants : " + enfants);
        }
        if (ficheMariageLabel != null) {
            ficheMariageLabel.setText(f.getDateMariage() != null ? DateUtil.formatShort(f.getDateMariage()) + " à l'Église " + (f.getEgliseMariage() != null ? f.getEgliseMariage() : "-") : "Néant");
        }
        if (ficheBaptemeLabel != null) {
            ficheBaptemeLabel.setText(f.getDateBapteme() != null ? DateUtil.formatShort(f.getDateBapteme()) + " à " + (f.getLieuBapteme() != null ? f.getLieuBapteme() : "Église AD") : "Non baptisé");
        }
        if (ficheEspritLabel != null) {
            ficheEspritLabel.setText(f.getDateBaptemeEsprit() != null ? DateUtil.formatShort(f.getDateBaptemeEsprit()) : "-");
        }
        if (ficheProfessionLabel != null) ficheProfessionLabel.setText(f.getProfession() != null ? f.getProfession() : "-");
        if (ficheContactLabel != null) {
            ficheContactLabel.setText("Togocel: " + (f.getTelephone() != null ? f.getTelephone() : "-") +
                    "   Moov: " + (f.getContactMoov() != null ? f.getContactMoov() : "-") +
                    "   Email: " + (f.getEmail() != null ? f.getEmail() : "-"));
        }
        if (ficheDateLabel != null) {
            ficheDateLabel.setText("Fait à Lomé, le " + DateUtil.formatShort(LocalDate.now()));
        }
    }

    @FXML
    private void handlePreviewFiche(ActionEvent event) {
        FideleDto f = comboFideleFiche != null ? comboFideleFiche.getValue() : null;
        if (f == null || f.getId() == null) {
            NotificationUtil.showWarning("Sélection requise", "Veuillez sélectionner un fidèle pour exporter sa fiche.");
            return;
        }

        if (docTabPane != null) docTabPane.getSelectionModel().select(1);

        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        documentService.getFidelePdf(f.getId())
                .whenComplete((bytes, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        if (throwable != null) {
                            NotificationUtil.showError("Erreur PDF", throwable.getMessage());
                        } else if (bytes != null) {
                            this.currentPdfBytes = bytes;
                            try {
                                File file = documentService.savePdfToTemp(bytes, "fiche_inscription_" + f.getId());
                                documentService.openPdf(file);
                                NotificationUtil.showSuccess("PDF Téléchargé", "Fiche officielle d'inscription générée et ouverte.");
                            } catch (Exception e) {
                                NotificationUtil.showError("Erreur", e.getMessage());
                            }
                        }
                    });
                });
    }

    @FXML
    private void handleGenerateLettre(ActionEvent event) {
        FideleDto f = comboFideleLettre != null ? comboFideleLettre.getValue() : null;
        if (f == null || f.getId() == null) {
            NotificationUtil.showWarning("Sélection requise", "Veuillez sélectionner un fidèle pour la lettre de recommandation.");
            return;
        }

        if (docTabPane != null) docTabPane.getSelectionModel().select(0);
        updatePreviewFromForm();

        String motif = "transfert";
        if (radioVoyage != null && radioVoyage.isSelected()) motif = "voyage";
        if (radioTravail != null && radioTravail.isSelected()) motif = "emploi";

        LettreRecommandationRequestDto request = new LettreRecommandationRequestDto(
                f.getId(),
                motif,
                txtEgliseDestination != null && !txtEgliseDestination.getText().trim().isEmpty() ? txtEgliseDestination.getText().trim() : "Temple de Kpalimé",
                comboPasteurSignataire != null ? comboPasteurSignataire.getValue() : "Pasteur AD"
        );

        if (loadingIndicator != null) loadingIndicator.setVisible(true);
        if (btnGenerateLettre != null) btnGenerateLettre.setDisable(true);

        documentService.generateLettreRecommandationPdf(request)
                .whenComplete((bytes, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);
                        if (btnGenerateLettre != null) btnGenerateLettre.setDisable(false);

                        if (throwable != null) {
                            NotificationUtil.showError("Erreur génération", throwable.getMessage());
                        } else if (bytes != null) {
                            this.currentPdfBytes = bytes;
                            try {
                                File file = documentService.savePdfToTemp(bytes, "lettre_recommandation_" + f.getId());
                                documentService.openPdf(file);
                                NotificationUtil.showSuccess("Lettre Prête", "Lettre officielle de recommandation générée et ouverte.");
                            } catch (Exception e) {
                                NotificationUtil.showError("Erreur", e.getMessage());
                            }
                        }
                    });
                });
    }

    @FXML
    private void handleDownloadPdf(ActionEvent event) {
        if (currentPdfBytes != null) {
            try {
                File file = documentService.savePdfToTemp(currentPdfBytes, "document_officiel_eglise");
                documentService.openPdf(file);
                NotificationUtil.showSuccess("Document Ouvert", "Fichier PDF ouvert avec succès dans votre lecteur.");
            } catch (Exception e) {
                NotificationUtil.showError("Erreur", e.getMessage());
            }
        } else {
            handleGenerateLettre(event);
        }
    }
}
