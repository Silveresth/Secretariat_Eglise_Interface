package com.eglise.secretariat.controllers.documents;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.dto.LettreRecommandationRequestDto;
import com.eglise.secretariat.services.DocumentService;
import com.eglise.secretariat.services.FideleService;
import com.eglise.secretariat.utils.DateUtil;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

public class DocumentsController extends BaseController {

    // Left Search & Selection Controls
    @FXML private TextField searchFideleInput;
    @FXML private Label lblTableSubtext;
    @FXML private TableView<FideleDto> tableSearchResult;
    @FXML private TableColumn<FideleDto, String> colNomComplet;
    @FXML private TableColumn<FideleDto, Void> colActions;

    // Lettre Options Card
    @FXML private VBox cardLettreOptions;
    @FXML private RadioButton radioTransfert;
    @FXML private RadioButton radioVoyage;
    @FXML private RadioButton radioTravail;
    private ToggleGroup groupMotif;
    @FXML private TextField txtEgliseDestination;
    @FXML private ComboBox<String> comboPasteurSignataire;

    // Right WebView & PDF Export
    @FXML private Label lblDocTypeTitle;
    @FXML private Label lblDocTargetName;
    @FXML private WebView webViewDocument;
    @FXML private Button btnExportPdf;
    @FXML private ProgressIndicator loadingIndicator;

    private final FideleService fideleService = new FideleService();
    private final DocumentService documentService = new DocumentService();

    private FideleDto selectedFidele;
    private enum DocumentType { FICHE, LETTRE }
    private DocumentType currentDocType = DocumentType.FICHE;
    private static String cachedLogoBase64 = null;

    @FXML
    public void initialize() {
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (cardLettreOptions != null) {
            cardLettreOptions.setVisible(false);
            cardLettreOptions.setManaged(false);
        }

        // Group Radio buttons
        groupMotif = new ToggleGroup();
        if (radioTransfert != null) radioTransfert.setToggleGroup(groupMotif);
        if (radioVoyage != null) radioVoyage.setToggleGroup(groupMotif);
        if (radioTravail != null) radioTravail.setToggleGroup(groupMotif);

        if (comboPasteurSignataire != null) {
            comboPasteurSignataire.setItems(FXCollections.observableArrayList(
                    "Pasteur AD",
                    "Rév. Pasteur Principal",
                    "Pasteur Adjoint",
                    "Secrétaire Général"
            ));
            comboPasteurSignataire.setValue("Pasteur AD");
        }

        // Configure Table Columns
        if (colNomComplet != null) {
            colNomComplet.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getNomComplet()
            ));
        }

        setupTableActionsColumn();

        // Listen for table selection
        if (tableSearchResult != null) {
            tableSearchResult.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    this.selectedFidele = newSel;
                    renderCurrentDocument();
                }
            });
        }

        // Load 3 latest registered fideles by default
        performSearch(null);
    }

    private void setupTableActionsColumn() {
        if (colActions == null) return;

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnFiche = new Button("Fiche");
            private final Button btnLettre = new Button("Lettre");
            private final HBox container = new HBox(8, btnFiche, btnLettre);

            {
                container.setAlignment(Pos.CENTER);
                btnFiche.setStyle("-fx-background-color: #00236f; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 12px; -fx-cursor: hand; -fx-background-radius: 6px;");
                btnLettre.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5px 12px; -fx-cursor: hand; -fx-background-radius: 6px;");

                btnFiche.setOnAction(event -> {
                    FideleDto fidele = getTableView().getItems().get(getIndex());
                    selectedFidele = fidele;
                    getTableView().getSelectionModel().select(fidele);
                    handleSelectFiche(null);
                });

                btnLettre.setOnAction(event -> {
                    FideleDto fidele = getTableView().getItems().get(getIndex());
                    selectedFidele = fidele;
                    getTableView().getSelectionModel().select(fidele);
                    handleSelectLettre(null);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    private void handleSearchFidele(ActionEvent event) {
        String query = searchFideleInput != null ? searchFideleInput.getText().trim() : null;
        performSearch(query);
    }

    @FXML
    private void handleResetSearch(ActionEvent event) {
        if (searchFideleInput != null) searchFideleInput.clear();
        performSearch(null);
    }

    private void performSearch(String query) {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        boolean isDefault = (query == null || query.isBlank());
        int limit = isDefault ? 3 : 20;

        if (lblTableSubtext != null) {
            lblTableSubtext.setText(isDefault ? "3 derniers inscrits" : "Résultats de recherche");
        }

        fideleService.searchFideles(isDefault ? null : query, null, null, true, 0, limit, "id,desc")
                .whenComplete((page, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);

                        if (throwable == null && page != null && page.getContent() != null) {
                            List<FideleDto> list = page.getContent();
                            tableSearchResult.setItems(FXCollections.observableArrayList(list));
                            if (!list.isEmpty()) {
                                tableSearchResult.getSelectionModel().select(0);
                                selectedFidele = list.get(0);
                                renderCurrentDocument();
                            }
                        } else {
                            tableSearchResult.setItems(FXCollections.observableArrayList());
                        }
                    });
                });
    }

    @FXML
    private void handleSelectFiche(ActionEvent event) {
        currentDocType = DocumentType.FICHE;
        if (cardLettreOptions != null) {
            cardLettreOptions.setVisible(false);
            cardLettreOptions.setManaged(false);
        }
        renderCurrentDocument();
    }

    @FXML
    private void handleSelectLettre(ActionEvent event) {
        currentDocType = DocumentType.LETTRE;
        if (cardLettreOptions != null) {
            cardLettreOptions.setVisible(true);
            cardLettreOptions.setManaged(true);
        }
        renderCurrentDocument();
    }

    @FXML
    private void handleUpdateLettrePreview(ActionEvent event) {
        if (currentDocType == DocumentType.LETTRE) {
            renderCurrentDocument();
        }
    }

    private void renderCurrentDocument() {
        if (selectedFidele == null) {
            if (lblDocTargetName != null) lblDocTargetName.setText("Veuillez sélectionner un fidèle dans la liste.");
            return;
        }

        if (lblDocTargetName != null) {
            lblDocTargetName.setText("Fidèle sélectionné : " + selectedFidele.getNomComplet());
        }

        if (currentDocType == DocumentType.FICHE) {
            if (lblDocTypeTitle != null) lblDocTypeTitle.setText("Aperçu : Fiche d'Inscription Individuelle");
            renderFicheHtml(selectedFidele);
        } else {
            if (lblDocTypeTitle != null) lblDocTypeTitle.setText("Aperçu : Lettre de Recommandation");
            renderLettreHtml(selectedFidele);
        }
    }

    private void renderFicheHtml(FideleDto f) {
        String template = loadHtmlTemplate("fiche_inscription.html");
        if (template == null) return;

        String logoSrc = getLogoBase64();
        String nomComplet = bold(f.getNomComplet());
        String dateLieuNaiss = bold((f.getDateNaissance() != null ? DateUtil.formatShort(f.getDateNaissance()) : "-") + " à " + (f.getLieuNaissance() != null ? f.getLieuNaissance() : "-"));
        String ethnie = bold(f.getEthnie() != null ? f.getEthnie() : "-");
        String domicile = bold(f.getQuartier() != null ? f.getQuartier() : "-");
        String maison = bold(f.getAdresse() != null ? f.getAdresse() : "-");
        String nomPere = bold((f.getNomPere() != null ? f.getNomPere() : "") + " " + (f.getPrenomPere() != null ? f.getPrenomPere() : ""));
        String nomMere = bold((f.getNomMere() != null ? f.getNomMere() : "") + " " + (f.getPrenomMere() != null ? f.getPrenomMere() : ""));
        String sexe = bold(f.getSexe() != null ? f.getSexe().name() : "MASCULIN");
        String celibataire = bold(f.getStatutMatrimonial() != null && f.getStatutMatrimonial().name().contains("CELIB") ? "Oui" : "Non");
        String dateMariage = bold(f.getDateMariage() != null ? DateUtil.formatShort(f.getDateMariage()) : "-");
        String egliseMariage = bold(f.getEgliseMariage() != null ? f.getEgliseMariage() : "-");
        String pasteurMariage = bold(f.getPasteurMariage() != null ? f.getPasteurMariage() : "-");
        String enfants = bold(String.valueOf((f.getNombreGarcons() != null ? f.getNombreGarcons() : 0) + (f.getNombreFilles() != null ? f.getNombreFilles() : 0)));
        String profession = bold(f.getProfession() != null ? f.getProfession() : "-");
        String contacts = bold((f.getTelephone() != null ? f.getTelephone() : "") + (f.getContactMoov() != null ? " / " + f.getContactMoov() : ""));
        String email = bold(f.getEmail() != null ? f.getEmail() : "-");
        String todayDate = bold(DateUtil.formatShort(LocalDate.now()));

        template = template.replace("{{LOGO_AD_SRC}}", logoSrc);
        template = template.replace("{{NOM_COMPLET}}", nomComplet);
        template = template.replace("{{DATE_LIEU_NAISSANCE}}", dateLieuNaiss);
        template = template.replace("{{ETHNIE}}", ethnie);
        template = template.replace("{{DOMICILE}}", domicile);
        template = template.replace("{{MAISON}}", maison);
        template = template.replace("{{NOM_PERE}}", nomPere.isBlank() ? "<b>-</b>" : nomPere);
        template = template.replace("{{NOM_MERE}}", nomMere.isBlank() ? "<b>-</b>" : nomMere);
        template = template.replace("{{SEXE}}", sexe);
        template = template.replace("{{CELIBATAIRE_YES}}", celibataire);
        template = template.replace("{{MARIAGE_DATE}}", dateMariage);
        template = template.replace("{{EGLISE_MARIAGE}}", egliseMariage);
        template = template.replace("{{PASTEUR_MARIAGE}}", pasteurMariage);
        template = template.replace("{{NB_ENFANTS}}", enfants);
        template = template.replace("{{PROFESSION}}", profession);
        template = template.replace("{{CONTACTS}}", contacts.isBlank() ? "<b>-</b>" : contacts);
        template = template.replace("{{EMAIL}}", email);
        template = template.replace("{{DATE_TODAY}}", todayDate);

        // CSS Fit zoom scale
        template = injectFitZoomCss(template, 0.72);

        loadWebViewContent(template);
    }

    private void renderLettreHtml(FideleDto f) {
        String template = loadHtmlTemplate("lettre_recommandation.html");
        if (template == null) return;

        boolean isTransfert = radioTransfert != null && radioTransfert.isSelected();
        boolean isVoyage = radioVoyage != null && radioVoyage.isSelected();
        boolean isEmploi = radioTravail != null && radioTravail.isSelected();

        String logoSrc = getLogoBase64();
        String pasteur = bold(comboPasteurSignataire != null ? comboPasteurSignataire.getValue() : "Pasteur AD");
        String nomComplet = bold(f.getNomComplet());
        String dateIntegration = bold(f.getDateIntegrationAdidogome() != null ? DateUtil.formatShort(f.getDateIntegrationAdidogome()) : "15/01/2018");
        String dateLieuNaiss = bold((f.getDateNaissance() != null ? DateUtil.formatShort(f.getDateNaissance()) : "-") + " à " + (f.getLieuNaissance() != null ? f.getLieuNaissance() : "-"));
        String dateBapteme = bold(f.getDateBapteme() != null ? DateUtil.formatShort(f.getDateBapteme()) : "-");
        String dateEsprit = bold(f.getDateBaptemeEsprit() != null ? DateUtil.formatShort(f.getDateBaptemeEsprit()) : "-");
        String profession = bold(f.getProfession() != null ? f.getProfession() : "-");
        String enfants = bold(String.valueOf((f.getNombreGarcons() != null ? f.getNombreGarcons() : 0) + (f.getNombreFilles() != null ? f.getNombreFilles() : 0)));

        String isCelib = bold(f.getStatutMatrimonial() != null && f.getStatutMatrimonial().name().contains("CELIB") ? "X" : "");
        String isFiance = bold(f.getStatutMatrimonial() != null && f.getStatutMatrimonial().name().contains("FIANCE") ? "X" : "");
        String isMarie = bold(f.getStatutMatrimonial() != null && f.getStatutMatrimonial().name().contains("MARI") ? "X" : "");
        String isDivorce = bold(f.getStatutMatrimonial() != null && f.getStatutMatrimonial().name().contains("DIVORC") ? "X" : "");

        boolean isM = f.getSexe() == null || f.getSexe().name().startsWith("M");
        String isMasculin = bold(isM ? "X" : "");
        String isFeminin = bold(!isM ? "X" : "");

        String motifText = isTransfert ? "Transfert d'Église" : (isVoyage ? "Voyage" : "Emploi / Travail");
        String egliseDest = txtEgliseDestination != null && !txtEgliseDestination.getText().trim().isEmpty() ? txtEgliseDestination.getText().trim() : "Temple de Kpalimé";
        String motifEtEglise = bold(motifText + " - Église : " + egliseDest);

        String todayDate = bold(DateUtil.formatShort(LocalDate.now()));

        template = template.replace("{{LOGO_AD_SRC}}", logoSrc);
        template = template.replace("{{MOTIF_TRANSFERT_X}}", isTransfert ? bold("X") : "");
        template = template.replace("{{MOTIF_VOYAGE_X}}", isVoyage ? bold("X") : "");
        template = template.replace("{{MOTIF_EMPLOI_X}}", isEmploi ? bold("X") : "");
        template = template.replace("{{PASTEUR_SIGNATAIRE}}", pasteur);
        template = template.replace("{{NOM_COMPLET}}", nomComplet);
        template = template.replace("{{DATE_INTEGRATION}}", dateIntegration);
        template = template.replace("{{DATE_LIEU_NAISSANCE}}", dateLieuNaiss);
        template = template.replace("{{DATE_BAPTEME_EAU}}", dateBapteme);
        template = template.replace("{{DATE_BAPTEME_ESPRIT}}", dateEsprit);
        template = template.replace("{{PROFESSION}}", profession);
        template = template.replace("{{CELIBATAIRE_X}}", isCelib);
        template = template.replace("{{FIANCE_X}}", isFiance);
        template = template.replace("{{MARIE_X}}", isMarie);
        template = template.replace("{{DIVORCE_X}}", isDivorce);
        template = template.replace("{{NB_ENFANTS}}", enfants);
        template = template.replace("{{SEXE_MASCULIN_X}}", isMasculin);
        template = template.replace("{{SEXE_FEMININ_X}}", isFeminin);
        template = template.replace("{{MOTIF_ET_EGLISE_DESTINATION}}", motifEtEglise);
        template = template.replace("{{DATE_TODAY}}", todayDate);

        // CSS Fit zoom scale
        template = injectFitZoomCss(template, 0.72);

        loadWebViewContent(template);
    }

    private void loadWebViewContent(String htmlContent) {
        if (webViewDocument != null) {
            webViewDocument.getEngine().loadContent(htmlContent, "text/html");
            webViewDocument.setZoom(0.78);
        }
    }

    private String injectFitZoomCss(String html, double zoomScale) {
        String fitCss = "<style>" +
                "body { zoom: " + zoomScale + "; -webkit-transform-origin: top center; margin: 0 auto; overflow: hidden; padding: 10px; }" +
                "@page { margin: 0; }" +
                "</style></head>";
        return html.replace("</head>", fitCss);
    }

    private String bold(String text) {
        if (text == null || text.isBlank()) return "";
        return "<b>" + text + "</b>";
    }

    private String getLogoBase64() {
        if (cachedLogoBase64 != null) return cachedLogoBase64;
        try {
            byte[] bytes = null;
            File file = new File("logo_ad.png");
            if (file.exists()) {
                bytes = java.nio.file.Files.readAllBytes(file.toPath());
            } else {
                InputStream is = getClass().getResourceAsStream("/com/eglise/secretariat/images/logo_ad.png");
                if (is != null) {
                    bytes = is.readAllBytes();
                }
            }
            if (bytes != null && bytes.length > 0) {
                cachedLogoBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
                return cachedLogoBase64;
            }
        } catch (Exception e) {
            System.err.println("Erreur encodage Logo Base64: " + e.getMessage());
        }
        return "images/logo_ad.png";
    }

    private String loadHtmlTemplate(String filename) {
        // 1. Try loading from resources
        try (InputStream is = getClass().getResourceAsStream("/com/eglise/secretariat/templates/" + filename)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}

        // 2. Fallback to root directory file
        try {
            File f = new File(filename);
            if (f.exists()) {
                return java.nio.file.Files.readString(f.toPath(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.err.println("Erreur lecture HTML template: " + e.getMessage());
        }
        return null;
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        if (selectedFidele == null || selectedFidele.getId() == null) {
            NotificationUtil.showWarning("Sélection requise", "Veuillez sélectionner un fidèle pour exporter le document.");
            return;
        }

        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        if (currentDocType == DocumentType.FICHE) {
            String filename = documentService.buildDocumentFilename(selectedFidele, "Fiche");
            documentService.getFidelePdf(selectedFidele.getId())
                    .whenComplete((bytes, throwable) -> handlePdfDownloadResponse(bytes, throwable, filename));
        } else {
            String motif = "transfert";
            if (radioVoyage != null && radioVoyage.isSelected()) motif = "voyage";
            if (radioTravail != null && radioTravail.isSelected()) motif = "emploi";

            LettreRecommandationRequestDto req = new LettreRecommandationRequestDto(
                    selectedFidele.getId(),
                    motif,
                    txtEgliseDestination != null && !txtEgliseDestination.getText().trim().isEmpty() ? txtEgliseDestination.getText().trim() : "Temple de Kpalimé",
                    comboPasteurSignataire != null ? comboPasteurSignataire.getValue() : "Pasteur AD"
            );

            String filename = documentService.buildDocumentFilename(selectedFidele, "Lettre");
            documentService.generateLettreRecommandationPdf(req)
                    .whenComplete((bytes, throwable) -> handlePdfDownloadResponse(bytes, throwable, filename));
        }
    }

    private void handlePdfDownloadResponse(byte[] bytes, Throwable throwable, String prefix) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(false);

            if (throwable != null) {
                NotificationUtil.showError("Erreur d'exportation", throwable.getMessage());
            } else if (bytes != null) {
                try {
                    File file = documentService.savePdfToDownloads(bytes, prefix);
                    documentService.openPdf(file);
                    NotificationUtil.showSuccess("PDF Généré", "Le document PDF a été enregistré dans Téléchargements (" + file.getName() + ") et ouvert.");
                } catch (Exception e) {
                    NotificationUtil.showError("Erreur", e.getMessage());
                }
            }
        });
    }
}
