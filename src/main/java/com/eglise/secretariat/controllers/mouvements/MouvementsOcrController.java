package com.eglise.secretariat.controllers.mouvements;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.dto.OcrResultDto;
import com.eglise.secretariat.models.enums.FrequenceDime;
import com.eglise.secretariat.models.enums.Sexe;
import com.eglise.secretariat.models.enums.Statut;
import com.eglise.secretariat.services.MouvementService;
import com.eglise.secretariat.utils.NavigationService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MouvementsOcrController extends BaseController {

    @FXML private VBox dropzoneBox;
    @FXML private StackPane previewContainer;
    @FXML private ImageView documentPreviewImage;
    @FXML private Label previewPlaceholderLabel;
    @FXML private StackPane scanOverlayPane;
    @FXML private Rectangle scanLaserLine;
    @FXML private ProgressIndicator scanningIndicator;

    // Section 1 : Identité Civile
    @FXML private TextField txtNomArrivant;
    @FXML private TextField txtPrenomsArrivant;
    @FXML private ComboBox<Sexe> comboSexe;
    @FXML private DatePicker dpDateNaissance;
    @FXML private TextField txtLieuNaissance;
    @FXML private TextField txtProfession;

    // Section 2 : Parcours Spirituel
    @FXML private DatePicker dpDateBapteme;
    @FXML private DatePicker dpDateBaptemeEsprit;
    @FXML private TextField txtPasteurSignataire;
    @FXML private TextField txtEgliseOrigine;
    @FXML private DatePicker dpDatePresentation;
    @FXML private TextField txtMotifDepart;

    // Section 3 : Famille & Contacts
    @FXML private ComboBox<Statut> comboStatutMatrimonial;
    @FXML private Spinner<Integer> spinEnfants;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtQuartier;
    @FXML private TextArea txtRawOcr;

    // Administrative Verifications
    @FXML private CheckBox chkCarteMembre;
    @FXML private CheckBox chkCarnetDime;

    @FXML private Button saveMovementBtn;
    @FXML private ProgressIndicator saveIndicator;

    private final MouvementService mouvementService = new MouvementService();
    private TranslateTransition scanAnimation;
    private File selectedFile;

    @FXML
    public void initialize() {
        if (comboSexe != null) {
            comboSexe.setItems(FXCollections.observableArrayList(Sexe.values()));
            comboSexe.setValue(Sexe.MASCULIN);
        }
        if (comboStatutMatrimonial != null) {
            comboStatutMatrimonial.setItems(FXCollections.observableArrayList(Statut.values()));
            comboStatutMatrimonial.setValue(Statut.MARIE);
        }
        if (spinEnfants != null) {
            spinEnfants.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        }

        if (dpDatePresentation != null) dpDatePresentation.setValue(LocalDate.now());
        if (chkCarteMembre != null) chkCarteMembre.setSelected(true);
        if (chkCarnetDime != null) chkCarnetDime.setSelected(true);

        // Dynamically bind centered laser scan line width to preview container
        if (scanLaserLine != null && previewContainer != null) {
            scanLaserLine.widthProperty().bind(previewContainer.widthProperty().subtract(30));
        }

        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        if (dropzoneBox != null) {
            dropzoneBox.setOnDragOver(event -> {
                if (event.getGestureSource() != dropzoneBox && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            });

            dropzoneBox.setOnDragEntered(event -> {
                if (event.getGestureSource() != dropzoneBox && event.getDragboard().hasFiles()) {
                    dropzoneBox.getStyleClass().add("dropzone-hover");
                }
                event.consume();
            });

            dropzoneBox.setOnDragExited(event -> {
                dropzoneBox.getStyleClass().remove("dropzone-hover");
                event.consume();
            });

            dropzoneBox.setOnDragDropped((DragEvent event) -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && !db.getFiles().isEmpty()) {
                    File file = db.getFiles().get(0);
                    processFile(file);
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }
    }

    @FXML
    private void handleBrowseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une lettre de recommandation");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents & Images (*.pdf, *.png, *.jpg, *.jpeg)", "*.pdf", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(navigationService.getPrimaryStage());
        if (file != null) {
            processFile(file);
        }
    }

    private void startLaserScanAnimation() {
        if (scanOverlayPane != null) {
            scanOverlayPane.setVisible(true);
        }
        if (scanLaserLine != null && previewContainer != null) {
            if (scanAnimation != null) {
                scanAnimation.stop();
            }
            double targetHeight = previewContainer.getHeight() > 100 ? previewContainer.getHeight() - 40 : 360;
            scanAnimation = new TranslateTransition(Duration.millis(1400), scanLaserLine);
            scanAnimation.setFromY(20);
            scanAnimation.setToY(targetHeight);
            scanAnimation.setCycleCount(Animation.INDEFINITE);
            scanAnimation.setAutoReverse(true);
            scanAnimation.play();
        }
    }

    private void stopLaserScanAnimation() {
        if (scanAnimation != null) {
            scanAnimation.stop();
        }
        if (scanOverlayPane != null) {
            scanOverlayPane.setVisible(false);
        }
    }

    private void processFile(File file) {
        this.selectedFile = file;

        // 1. Render Document Preview (Images or PDF)
        renderFilePreview(file);

        // 2. Start Animated Laser Scanner
        startLaserScanAnimation();
        if (scanningIndicator != null) scanningIndicator.setVisible(true);
        NotificationUtil.showInfo("Analyse OCR", "Numérisation laser et extraction des champs...");

        // 3. Extract text from PDF if applicable
        String localPdfText = extractTextIfPdf(file);

        // 4. Send to backend OCR
        mouvementService.scanLetterOcr(file)
                .whenComplete((ocrResult, throwable) -> {
                    Platform.runLater(() -> {
                        stopLaserScanAnimation();
                        if (scanningIndicator != null) scanningIndicator.setVisible(false);

                        String textToParse = null;
                        if (ocrResult != null && ocrResult.getRawText() != null && !ocrResult.getRawText().isBlank()) {
                            textToParse = ocrResult.getRawText();
                        } else if (localPdfText != null && !localPdfText.isBlank()) {
                            textToParse = localPdfText;
                        }

                        if (textToParse != null && !textToParse.isBlank()) {
                            NotificationUtil.showSuccess("OCR Terminé", "Informations extraites de la lettre !");
                            if (txtRawOcr != null) txtRawOcr.setText(textToParse);
                            parseFullLetterText(textToParse);
                        } else {
                            NotificationUtil.showWarning("OCR", "Le document n'a pas pu être lu automatiquement.");
                        }
                    });
                });
    }

    private void renderFilePreview(File file) {
        String lower = file.getName().toLowerCase();
        try {
            if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".bmp")) {
                Image img = new Image(new FileInputStream(file));
                documentPreviewImage.setImage(img);
                documentPreviewImage.setVisible(true);
                if (previewPlaceholderLabel != null) previewPlaceholderLabel.setVisible(false);
            } else if (lower.endsWith(".pdf")) {
                try (PDDocument document = Loader.loadPDF(file)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(bim, "png", os);
                    Image fxImage = new Image(new ByteArrayInputStream(os.toByteArray()));
                    documentPreviewImage.setImage(fxImage);
                    documentPreviewImage.setVisible(true);
                    if (previewPlaceholderLabel != null) previewPlaceholderLabel.setVisible(false);
                }
            }
        } catch (Exception e) {
            System.err.println("Note prévisualisation: " + e.getMessage());
        }
    }

    private String extractTextIfPdf(File file) {
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void parseFullLetterText(String text) {
        if (text == null || text.isBlank()) return;

        String clean = text.replace("\r", "");

        // 1. Nom & Prénoms : Smart Extraction
        extractNomPrenomMultiStrategy(clean);

        // 2. Date et lieu de naissance : "12/05/1995 à Lomé"
        Pattern birthPat = Pattern.compile("(?i)(?:date\\s*et\\s*lieu\\s*de\\s*naissance|n[ée]\\s*\\(e\\)\\s*le|naissance)\\s*:?\\s*(\\d{1,2}[\\/\\-\\.]\\d{1,2}[\\/\\-\\.]\\d{4})(?:\\s*[àa]\\s*([^\\r\\n]+))?");
        Matcher birthMat = birthPat.matcher(clean);
        if (birthMat.find()) {
            LocalDate birthDate = parseDateString(birthMat.group(1));
            if (birthDate != null && dpDateNaissance != null) dpDateNaissance.setValue(birthDate);
            if (birthMat.groupCount() >= 2 && birthMat.group(2) != null && txtLieuNaissance != null) {
                String lieu = birthMat.group(2).trim();
                if (lieu.contains("\n")) lieu = lieu.split("\n")[0].trim();
                lieu = lieu.replaceAll("^[.:_\\s]+|[.:_\\s]+$", "");
                txtLieuNaissance.setText(lieu);
            }
        }

        // 3. Baptême d'eau : "Baptisé d'eau le : 10/04/2016" (avec correction OCR 2076 -> 2016)
        Pattern waterPat = Pattern.compile("(?i)(?:baptis[ée]\\s*d['’]eau\\s*le|bapt[êe]me\\s*d['’]eau)\\s*:?\\s*(\\d{1,2}[\\/\\-\\.]\\d{1,2}[\\/\\-\\.]\\d{4})");
        Matcher waterMat = waterPat.matcher(clean);
        if (waterMat.find() && dpDateBapteme != null) {
            LocalDate d = parseDateString(waterMat.group(1));
            if (d != null) dpDateBapteme.setValue(d);
        }

        // 4. Baptême Saint-Esprit : "Baptisé du Saint-Esprit le : 04/06/2017" (avec correction OCR 2077 -> 2017)
        Pattern spiritPat = Pattern.compile("(?i)(?:baptis[ée]\\s*du\\s*saint[\\-\\s]*esprit\\s*le|saint[\\-\\s]*esprit)\\s*:?\\s*(\\d{1,2}[\\/\\-\\.]\\d{1,2}[\\/\\-\\.]\\d{4})");
        Matcher spiritMat = spiritPat.matcher(clean);
        if (spiritMat.find() && dpDateBaptemeEsprit != null) {
            LocalDate d = parseDateString(spiritMat.group(1));
            if (d != null) dpDateBaptemeEsprit.setValue(d);
        }

        // 5. Profession : "Profession : Informaticien"
        Pattern profPat = Pattern.compile("(?i)(?:profession|m[ée]tier)\\s*:?\\s*([^\\r\\n]+)");
        Matcher profMat = profPat.matcher(clean);
        if (profMat.find() && txtProfession != null) {
            String prof = profMat.group(1).trim().replaceAll("^[.:_\\s]+|[.:_\\s]+$", "");
            if (prof.contains("\n")) prof = prof.split("\n")[0].trim();
            txtProfession.setText(prof);
        }

        // 6. Sexe : "Sexe : Masculin X Féminin"
        if (comboSexe != null) {
            if (clean.contains("Sexe : Féminin X") || clean.contains("Féminin [X]") || clean.contains("Féminin X") || clean.contains("Sexe: Féminin")) {
                comboSexe.setValue(Sexe.FEMININ);
            } else if (clean.contains("Sexe : Masculin X") || clean.contains("Masculin [X]") || clean.contains("Masculin X") || clean.contains("Sexe: Masculin")) {
                comboSexe.setValue(Sexe.MASCULIN);
            }
        }

        // 7. Situation Matrimoniale & Enfants
        if (comboStatutMatrimonial != null) {
            if (clean.contains("Marié(e) X") || clean.contains("Marié(e) [X]") || clean.contains("Marié [X]") || clean.contains("Marié(e) : X")) {
                comboStatutMatrimonial.setValue(Statut.MARIE);
            } else if (clean.contains("Célibataire X") || clean.contains("Célibataire [X]")) {
                comboStatutMatrimonial.setValue(Statut.CELIBATAIRE);
            } else if (clean.contains("Divorcé(e) X") || clean.contains("Divorcé(e) [X]")) {
                comboStatutMatrimonial.setValue(Statut.DIVORCE);
            }
        }

        Pattern enfPat = Pattern.compile("(?i)(?:nombre\\s*d['’]enfants?|enfants?)\\s*:?\\s*(\\d+)");
        Matcher enfMat = enfPat.matcher(clean);
        if (enfMat.find() && spinEnfants != null) {
            try {
                spinEnfants.getValueFactory().setValue(Integer.parseInt(enfMat.group(1)));
            } catch (Exception ignored) {}
        }

        // 8. Pasteur Signataire : "Je soussigné : Pasteur AD Pasteur de l'Église..."
        Pattern pastPat = Pattern.compile("(?i)(?:je\\s*soussign[ée]\\s*:?)\\s*([A-Za-zÀ-ÿ\\-\\s.]+?)(?=\\s*(?:pasteur\\s*de|pasteur|atteste|de\\s*l'|\\r|\\n|$))");
        Matcher pastMat = pastPat.matcher(clean);
        if (pastMat.find() && txtPasteurSignataire != null) {
            String p = pastMat.group(1).trim().replaceAll("^[.:_\\s]+|[.:_\\s]+$", "");
            if (!p.isBlank() && !p.equalsIgnoreCase("le")) {
                txtPasteurSignataire.setText(p);
            }
        }

        // 9. Église de Provenance / Destination : "Église : Temple de Kpalimé" ou "Temple : « DIEU NE CHANGE PAS »"
        Pattern eglPat1 = Pattern.compile("(?i)[ÉE]glise\\s*:\\s*([^\\r\\n\\-]+)");
        Matcher eglMat1 = eglPat1.matcher(clean);
        if (eglMat1.find() && txtEgliseOrigine != null) {
            String egl = eglMat1.group(1).trim().replaceAll("^[.:_\\s«»\"]+|[.:_\\s«»\"]+$", "");
            if (!egl.isBlank()) txtEgliseOrigine.setText(egl);
        } else {
            Pattern eglPat2 = Pattern.compile("(?i)Temple\\s*:\\s*[«\"]?([^»\"\\r\\n]+(?:\\s+[^»\"\\r\\n]+)*)[»\"]?");
            Matcher eglMat2 = eglPat2.matcher(clean);
            if (eglMat2.find() && txtEgliseOrigine != null) {
                String egl = eglMat2.group(1).trim().replaceAll("^[.:_\\s«»\"]+|[.:_\\s«»\"]+$", "");
                if (!egl.isBlank()) txtEgliseOrigine.setText(egl);
            }
        }

        // 10. Date d'émission de la Lettre : "Fait à Lomé, le 23/08/2026"
        Pattern datePat = Pattern.compile("(?i)(?:fait\\s*[àa]\\s*[A-Za-zÀ-ÿ\\-\\s]+,\\s*le|le)\\s*(\\d{1,2}[\\/\\-\\.]\\d{1,2}[\\/\\-\\.]\\d{4})");
        Matcher dateMat = datePat.matcher(clean);
        if (dateMat.find() && dpDatePresentation != null) {
            LocalDate d = parseDateString(dateMat.group(1));
            if (d != null) dpDatePresentation.setValue(d);
        }

        // 11. Motif du départ : "Transfert pour raison professionnelle"
        Pattern motifPat = Pattern.compile("(?i)Motif(?:\\s*\\/\\s*[^:]+)?\\s*:\\s*([^\\r\\n\\-]+)");
        Matcher motifMat = motifPat.matcher(clean);
        if (motifMat.find() && txtMotifDepart != null) {
            String motif = motifMat.group(1).trim().replaceAll("^[.:_\\/\\s]+|[.:_\\/\\s]+$", "");
            txtMotifDepart.setText(motif);
        } else {
            if (clean.contains("Transfert X") || clean.contains("Transfert [X]")) {
                if (txtMotifDepart != null) txtMotifDepart.setText("Transfert");
            } else if (clean.contains("Voyage X") || clean.contains("Voyage [X]")) {
                if (txtMotifDepart != null) txtMotifDepart.setText("Voyage");
            } else if (clean.contains("Emploi X") || clean.contains("Emploi [X]")) {
                if (txtMotifDepart != null) txtMotifDepart.setText("Emploi");
            }
        }
    }

    private void extractNomPrenomMultiStrategy(String text) {
        if (text == null || text.isBlank()) return;

        String[] lines = text.split("[\\r\\n]+");
        String fullName = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            String lower = line.toLowerCase();

            // Case A : Line contains trigger keywords
            if (lower.contains("nomm") || lower.contains("atteste que") || lower.contains("porteur") || lower.contains("nom et") || lower.contains("nom :")) {
                int colonIdx = line.lastIndexOf(':');
                if (colonIdx != -1) {
                    String afterColon = line.substring(colonIdx + 1).trim();
                    afterColon = afterColon.replaceAll("(?i)\\s+est\\b.*$", "");
                    afterColon = afterColon.replaceAll("(?i)\\s+(?:de\\s+notre|depuis|date\\s*de).*$", "");
                    afterColon = afterColon.replaceAll("^[.:_\\s«»\"'…\\-]+|[.:_\\s«»\"'…\\-]+$", "").trim();

                    if (isValidCandidateName(afterColon)) {
                        fullName = afterColon;
                        break;
                    }

                    // If after colon is empty, check the NEXT line
                    if (afterColon.isBlank() && i + 1 < lines.length) {
                        String nextLine = lines[i + 1].trim();
                        nextLine = nextLine.replaceAll("(?i)\\s+est\\b.*$", "");
                        nextLine = nextLine.replaceAll("(?i)\\s+(?:de\\s+notre|depuis|date\\s*de).*$", "");
                        nextLine = nextLine.replaceAll("^[.:_\\s«»\"'…\\-]+|[.:_\\s«»\"'…\\-]+$", "").trim();
                        if (isValidCandidateName(nextLine)) {
                            fullName = nextLine;
                            break;
                        }
                    }
                } else {
                    String stripped = line.replaceAll("(?i)^.*?(?:nomm[a-z()\\/\\s]*|atteste\\s+que(?:\\s+le\\s+ou\\s+la)?|porteur\\s*du\\s*pr[ée]sent|porteur)\\s*", "");
                    stripped = stripped.replaceAll("(?i)\\s+est\\b.*$", "");
                    stripped = stripped.replaceAll("(?i)\\s+(?:de\\s+notre|depuis|date\\s*de).*$", "");
                    stripped = stripped.replaceAll("^[.:_\\s«»\"'…\\-]+|[.:_\\s«»\"'…\\-]+$", "").trim();
                    if (isValidCandidateName(stripped)) {
                        fullName = stripped;
                        break;
                    }
                }
            }
        }

        // Case B : Regex search for UPPERCASE word followed by Capitalized words
        if (fullName == null) {
            Pattern pUpper = Pattern.compile("([A-ZÀ-ÿ\\-]{2,}\\s+[A-ZÀ-ÿ][a-zà-ÿ\\-]+(?:\\s+[A-ZÀ-ÿ][a-zà-ÿ\\-]+)*)");
            Matcher mUpper = pUpper.matcher(text);
            while (mUpper.find()) {
                String cand = mUpper.group(1).trim();
                if (isValidCandidateName(cand) && !cand.contains("ASSEMBLEES") && !cand.contains("DIEU") && !cand.contains("LETTRE") && !cand.contains("RECOMMANDATION")) {
                    fullName = cand;
                    break;
                }
            }
        }

        // Apply detected name into text fields
        if (fullName != null) {
            String[] parts = fullName.split("\\s+");
            if (parts.length >= 2) {
                if (txtNomArrivant != null) txtNomArrivant.setText(parts[0].toUpperCase());
                StringBuilder p = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    if (i > 1) p.append(" ");
                    p.append(parts[i]);
                }
                if (txtPrenomsArrivant != null) txtPrenomsArrivant.setText(p.toString());
            } else if (parts.length == 1) {
                if (txtNomArrivant != null) txtNomArrivant.setText(parts[0].toUpperCase());
            }
        }
    }

    private boolean isValidCandidateName(String str) {
        if (str == null || str.isBlank()) return false;
        if (str.length() < 3 || str.length() > 60) return false;
        String lower = str.toLowerCase();
        if (lower.equals("le pasteur") || lower.contains("eglise") || lower.contains("temple") || lower.contains("recommandation") || lower.contains("assemblees") || lower.contains("dieu ne change pas")) {
            return false;
        }
        return str.matches(".*[A-Za-zÀ-ÿ]{2,}.*");
    }

    private LocalDate parseDateString(String str) {
        if (str == null) return null;
        try {
            String[] parts = str.trim().split("[\\/\\-\\.]");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0].trim());
                int month = Integer.parseInt(parts[1].trim());
                int year = Integer.parseInt(parts[2].trim());
                
                // OCR Year sanity correction
                if (year >= 2070 && year <= 2079) {
                    year = year - 60; // 2076 -> 2016, 2077 -> 2017
                }
                if (year < 100) {
                    year = (year > 40) ? (1900 + year) : (2000 + year);
                }
                if (year > LocalDate.now().getYear() + 1) {
                    if (year >= 2070 && year <= 2099) {
                        year = year - 60;
                    }
                }
                if (day >= 1 && day <= 31 && month >= 1 && month <= 12 && year >= 1900 && year <= 2099) {
                    return LocalDate.of(year, month, day);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    @FXML
    private void handleReset(ActionEvent event) {
        if (txtNomArrivant != null) txtNomArrivant.clear();
        if (txtPrenomsArrivant != null) txtPrenomsArrivant.clear();
        if (txtLieuNaissance != null) txtLieuNaissance.clear();
        if (txtProfession != null) txtProfession.clear();
        if (txtPasteurSignataire != null) txtPasteurSignataire.clear();
        if (txtEgliseOrigine != null) txtEgliseOrigine.clear();
        if (txtMotifDepart != null) txtMotifDepart.clear();
        if (txtTelephone != null) txtTelephone.clear();
        if (txtQuartier != null) txtQuartier.clear();
        if (txtRawOcr != null) txtRawOcr.clear();
        if (documentPreviewImage != null) {
            documentPreviewImage.setImage(null);
            documentPreviewImage.setVisible(false);
        }
        if (previewPlaceholderLabel != null) previewPlaceholderLabel.setVisible(true);
        stopLaserScanAnimation();
    }

    @FXML
    private void handleSaveMovement(ActionEvent event) {
        String nom = txtNomArrivant != null && txtNomArrivant.getText() != null ? txtNomArrivant.getText().trim().toUpperCase() : "";
        String prenoms = txtPrenomsArrivant != null && txtPrenomsArrivant.getText() != null ? txtPrenomsArrivant.getText().trim() : "";

        if (nom.isEmpty()) {
            NotificationUtil.showWarning("Champ requis", "Veuillez renseigner le nom du fidèle arrivant.");
            if (txtNomArrivant != null) txtNomArrivant.requestFocus();
            return;
        }

        if (prenoms.isEmpty()) {
            NotificationUtil.showWarning("Champ requis", "Veuillez renseigner le prénom du fidèle arrivant.");
            if (txtPrenomsArrivant != null) txtPrenomsArrivant.requestFocus();
            return;
        }

        FideleDto entrant = new FideleDto();
        entrant.setNom(nom);
        entrant.setPrenoms(prenoms);
        entrant.setSexe(comboSexe != null && comboSexe.getValue() != null ? comboSexe.getValue() : Sexe.MASCULIN);
        entrant.setDateNaissance(dpDateNaissance != null ? dpDateNaissance.getValue() : null);
        entrant.setLieuNaissance(txtLieuNaissance != null && txtLieuNaissance.getText() != null ? txtLieuNaissance.getText().trim() : "");
        entrant.setProfession(txtProfession != null && txtProfession.getText() != null ? txtProfession.getText().trim() : "");

        entrant.setStatutMatrimonial(comboStatutMatrimonial != null && comboStatutMatrimonial.getValue() != null ? comboStatutMatrimonial.getValue() : Statut.CELIBATAIRE);
        if (spinEnfants != null && spinEnfants.getValue() != null) {
            entrant.setNombreGarcons(spinEnfants.getValue());
        } else {
            entrant.setNombreGarcons(0);
        }
        entrant.setNombreFilles(0);

        entrant.setDateBapteme(dpDateBapteme != null ? dpDateBapteme.getValue() : null);
        entrant.setBaptise(dpDateBapteme != null && dpDateBapteme.getValue() != null);
        entrant.setDateBaptemeEsprit(dpDateBaptemeEsprit != null ? dpDateBaptemeEsprit.getValue() : null);

        entrant.setTelephone(txtTelephone != null && txtTelephone.getText() != null ? txtTelephone.getText().trim() : "");
        entrant.setQuartier(txtQuartier != null && !txtQuartier.getText().isBlank() ? txtQuartier.getText().trim() : "Adidogomé");
        entrant.setAncienneDenomination(txtEgliseOrigine != null ? txtEgliseOrigine.getText().trim() : "");
        entrant.setEgliseLettreRecommandation(txtEgliseOrigine != null ? txtEgliseOrigine.getText().trim() : "");
        entrant.setPasteurLettreRecommandation(txtPasteurSignataire != null ? txtPasteurSignataire.getText().trim() : "");
        entrant.setDateLettreRecommandation(dpDatePresentation != null && dpDatePresentation.getValue() != null ? dpDatePresentation.getValue() : LocalDate.now());
        entrant.setLettreRecommandationPresentee(true);
        entrant.setDateIntegrationAdidogome(LocalDate.now());

        entrant.setCarteMembreValide(chkCarteMembre != null && chkCarteMembre.isSelected());
        entrant.setCarnetDimeValide(chkCarnetDime != null && chkCarnetDime.isSelected());
        entrant.setPayeDimes(true);
        entrant.setFrequenceDime(FrequenceDime.REGULIEREMENT);
        entrant.setActif(true);

        if (saveIndicator != null) saveIndicator.setVisible(true);
        if (saveMovementBtn != null) saveMovementBtn.setDisable(true);

        mouvementService.saveFideleEntrant(entrant)
                .whenComplete((saved, throwable) -> {
                    Platform.runLater(() -> {
                        if (saveIndicator != null) saveIndicator.setVisible(false);
                        if (saveMovementBtn != null) saveMovementBtn.setDisable(false);

                        if (throwable != null) {
                            NotificationUtil.showError("Erreur enregistrement", throwable.getMessage());
                        } else {
                            NotificationUtil.showSuccess("Fidèle Intégré", "Le nouveau fidèle arrivant " + entrant.getNomComplet() + " a été enregistré avec succès.");
                            Map<String, Object> params = new HashMap<>();
                            params.put("fideleId", saved.getId());
                            navigationService.navigateToContent(NavigationService.View.FIDELE_DETAILS, params);
                        }
                    });
                });
    }
}
