package com.eglise.secretariat.controllers.fideles;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.dto.EngagementDto;
import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.services.DocumentService;
import com.eglise.secretariat.services.FideleService;
import com.eglise.secretariat.utils.DateUtil;
import com.eglise.secretariat.utils.NavigationService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FideleDetailsController extends BaseController {

    @FXML private Label headerNomCompletLabel;
    @FXML private Label headerAvatarLabel;
    @FXML private Label headerMatrimonialBadge;
    @FXML private Label headerCarteBadge;
    @FXML private Label headerDimeBadge;

    // Tab 1 : Identité & Contact
    @FXML private Label lblNom;
    @FXML private Label lblPrenoms;
    @FXML private Label lblSexe;
    @FXML private Label lblNaissance;
    @FXML private Label lblLieuNaissance;
    @FXML private Label lblEthnie;
    @FXML private Label lblProfession;
    @FXML private Label lblNiveauEtude;
    @FXML private Label lblTelephone;
    @FXML private Label lblContactMoov;
    @FXML private Label lblEmail;
    @FXML private Label lblQuartier;
    @FXML private Label lblAdresse;

    // Tab 2 : Famille & Filiation
    @FXML private Label lblStatutMatrimonial;
    @FXML private Label lblDateMariage;
    @FXML private Label lblEgliseMariage;
    @FXML private Label lblPasteurMariage;
    @FXML private Label lblConjoint;
    @FXML private Label lblEnfants;
    @FXML private Label lblPere;
    @FXML private Label lblMere;

    // Tab 3 : Spirituel & Intégration
    @FXML private Label lblConversion;
    @FXML private Label lblBaptemeEau;
    @FXML private Label lblBaptemeEsprit;
    @FXML private Label lblDenominations;
    @FXML private Label lblLettreEntrante;
    @FXML private Label lblIntegration;

    // Tab 4 : Vie de membre & Engagements
    @FXML private Label lblCarteStatus;
    @FXML private Label lblCarnetDimeStatus;
    @FXML private Label lblFrequenceDime;
    @FXML private Label lblReunionsStatus;
    @FXML private VBox engagementsListContainer;

    @FXML private ProgressIndicator loadingIndicator;

    private final FideleService fideleService = new FideleService();
    private final DocumentService documentService = new DocumentService();
    private FideleDto currentFidele;
    private Long fideleId;

    @FXML
    public void initialize() {
        Object param = navigationService.getParameter("fideleId");
        if (param != null) {
            this.fideleId = Long.valueOf(String.valueOf(param));
            navigationService.clearParameters();
            loadFideleDetails(this.fideleId);
        } else {
            // Default demo fidele
            applyDemoFidele();
        }
    }

    public void loadFideleDetails(Long id) {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        fideleService.getFideleById(id)
                .whenComplete((fidele, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);

                        if (throwable == null && fidele != null) {
                            this.currentFidele = fidele;
                            populateUI(fidele);
                        } else {
                            NotificationUtil.showError("Erreur", "Impossible de charger la fiche du fidèle.");
                            applyDemoFidele();
                        }
                    });
                });
    }

    private void populateUI(FideleDto f) {
        // Header
        if (headerNomCompletLabel != null) headerNomCompletLabel.setText(f.getNomComplet());
        if (headerAvatarLabel != null) {
            String initials = (f.getNom() != null && !f.getNom().isEmpty() ? f.getNom().substring(0, 1) : "") +
                             (f.getPrenoms() != null && !f.getPrenoms().isEmpty() ? f.getPrenoms().substring(0, 1) : "");
            headerAvatarLabel.setText(initials.isEmpty() ? "F" : initials);
        }

        if (headerMatrimonialBadge != null) {
            headerMatrimonialBadge.setText(f.getStatutMatrimonial() != null ? f.getStatutMatrimonial().getLabel() : "Célibataire");
        }
        if (headerCarteBadge != null) {
            headerCarteBadge.setText(Boolean.TRUE.equals(f.getCarteMembreValide()) ? "CARTE VALIDE" : "CARTE EXPIRÉE");
            headerCarteBadge.getStyleClass().setAll(Boolean.TRUE.equals(f.getCarteMembreValide()) ? "badge-success" : "badge-error");
        }
        if (headerDimeBadge != null) {
            headerDimeBadge.setText(Boolean.TRUE.equals(f.getPayeDimes()) ? "DÎME À JOUR" : "DÎME EN RETARD");
            headerDimeBadge.getStyleClass().setAll(Boolean.TRUE.equals(f.getPayeDimes()) ? "badge-success" : "badge-warning");
        }

        // Tab 1 : Identité & Contact
        if (lblNom != null) lblNom.setText(f.getNom() != null ? f.getNom() : "-");
        if (lblPrenoms != null) lblPrenoms.setText(f.getPrenoms() != null ? f.getPrenoms() : "-");
        if (lblSexe != null) lblSexe.setText(f.getSexe() != null ? f.getSexe().getLabel() : "-");
        if (lblNaissance != null) lblNaissance.setText(DateUtil.formatWithAge(f.getDateNaissance()));
        if (lblLieuNaissance != null) lblLieuNaissance.setText(f.getLieuNaissance() != null ? f.getLieuNaissance() : "-");
        if (lblEthnie != null) lblEthnie.setText(f.getEthnie() != null ? f.getEthnie() : "-");
        if (lblProfession != null) lblProfession.setText(f.getProfession() != null ? f.getProfession() : "-");
        if (lblNiveauEtude != null) lblNiveauEtude.setText(f.getNiveauEtude() != null ? f.getNiveauEtude() : "-");

        if (lblTelephone != null) lblTelephone.setText(f.getTelephone() != null ? f.getTelephone() : "-");
        if (lblContactMoov != null) lblContactMoov.setText(f.getContactMoov() != null ? f.getContactMoov() : "-");
        if (lblEmail != null) lblEmail.setText(f.getEmail() != null ? f.getEmail() : "-");
        if (lblQuartier != null) lblQuartier.setText(f.getQuartier() != null ? f.getQuartier() : "-");
        if (lblAdresse != null) lblAdresse.setText(f.getAdresse() != null ? f.getAdresse() : "-");

        // Tab 2 : Famille & Filiation
        if (lblStatutMatrimonial != null) lblStatutMatrimonial.setText(f.getStatutMatrimonial() != null ? f.getStatutMatrimonial().getLabel() : "-");
        if (lblDateMariage != null) lblDateMariage.setText(DateUtil.formatFrench(f.getDateMariage()));
        if (lblEgliseMariage != null) lblEgliseMariage.setText(f.getEgliseMariage() != null ? f.getEgliseMariage() : "-");
        if (lblPasteurMariage != null) lblPasteurMariage.setText(f.getPasteurMariage() != null ? f.getPasteurMariage() : "-");
        if (lblConjoint != null) lblConjoint.setText(f.getNomConjoint() != null ? f.getNomConjoint() : "Aucun");
        if (lblEnfants != null) {
            int g = f.getNombreGarcons() != null ? f.getNombreGarcons() : 0;
            int fi = f.getNombreFilles() != null ? f.getNombreFilles() : 0;
            lblEnfants.setText(g + " Garçon(s), " + fi + " Fille(s)");
        }
        if (lblPere != null) {
            String p = (f.getNomPere() != null ? f.getNomPere() : "") + " " + (f.getPrenomPere() != null ? f.getPrenomPere() : "");
            lblPere.setText(p.trim().isEmpty() ? "-" : p.trim());
        }
        if (lblMere != null) {
            String m = (f.getNomMere() != null ? f.getNomMere() : "") + " " + (f.getPrenomMere() != null ? f.getPrenomMere() : "");
            lblMere.setText(m.trim().isEmpty() ? "-" : m.trim());
        }

        // Tab 3 : Spirituel & Intégration
        if (lblConversion != null) {
            String conv = DateUtil.formatFrench(f.getDateConversion()) + (f.getEgliseConversion() != null ? " (" + f.getEgliseConversion() + ")" : "");
            lblConversion.setText(conv.trim().equals("-") ? "-" : conv);
        }
        if (lblBaptemeEau != null) {
            if (f.isBaptise()) {
                lblBaptemeEau.setText("Baptisé(e) le " + DateUtil.formatFrench(f.getDateBapteme()) + " à " + (f.getLieuBapteme() != null ? f.getLieuBapteme() : "-"));
            } else {
                lblBaptemeEau.setText("Non baptisé(e)");
            }
        }
        if (lblBaptemeEsprit != null) {
            lblBaptemeEsprit.setText(f.getDateBaptemeEsprit() != null ? "Baptisé(e) le " + DateUtil.formatFrench(f.getDateBaptemeEsprit()) : "Non renseigné");
        }
        if (lblDenominations != null) {
            lblDenominations.setText("Précédente: " + (f.getAncienneDenomination() != null ? f.getAncienneDenomination() : "Aucune"));
        }
        if (lblLettreEntrante != null) {
            lblLettreEntrante.setText(Boolean.TRUE.equals(f.getLettreRecommandationPresentee()) ? "Présentée (Pasteur " + (f.getPasteurLettreRecommandation() != null ? f.getPasteurLettreRecommandation() : "-") + ")" : "Non présentée");
        }
        if (lblIntegration != null) {
            lblIntegration.setText(DateUtil.formatFrench(f.getDateIntegrationAdidogome()));
        }

        // Tab 4 : Vie de membre & Engagements
        if (lblCarteStatus != null) lblCarteStatus.setText(Boolean.TRUE.equals(f.getCarteMembreValide()) ? "Active / Valide" : "Expirée / À renouveler");
        if (lblCarnetDimeStatus != null) lblCarnetDimeStatus.setText(Boolean.TRUE.equals(f.getCarnetDimeValide()) ? "Valide" : "Non valide");
        if (lblFrequenceDime != null) lblFrequenceDime.setText(f.getFrequenceDime() != null ? f.getFrequenceDime().getLabel() : "Régulièrement");
        if (lblReunionsStatus != null) lblReunionsStatus.setText(Boolean.TRUE.equals(f.getRegulierReunions()) ? "Régulier" : "Irrégulier");

        if (engagementsListContainer != null) {
            engagementsListContainer.getChildren().clear();
            if (f.getEngagements() != null && !f.getEngagements().isEmpty()) {
                for (EngagementDto eng : f.getEngagements()) {
                    Label item = new Label("• " + eng.getNom() + " (" + eng.getRole() + ")");
                    item.getStyleClass().add("font-body-md");
                    engagementsListContainer.getChildren().add(item);
                }
            } else {
                Label none = new Label("Aucun engagement / ministère actif enregistré.");
                none.getStyleClass().add("font-body-muted");
                engagementsListContainer.getChildren().add(none);
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigationService.navigateToContent(NavigationService.View.FIDELES_LIST);
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (currentFidele != null && currentFidele.getId() != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("fideleId", currentFidele.getId());
            navigationService.navigateToContent(NavigationService.View.FIDELE_EDIT, params);
        }
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        if (currentFidele == null || currentFidele.getId() == null) return;

        NotificationUtil.showInfo("Génération PDF", "Préparation de la fiche...");
        documentService.getFidelePdf(currentFidele.getId())
                .whenComplete((bytes, throwable) -> {
                    Platform.runLater(() -> {
                        if (throwable != null) {
                            NotificationUtil.showError("Erreur PDF", throwable.getMessage());
                        } else if (bytes != null) {
                            try {
                                File file = documentService.savePdfToTemp(bytes, "fiche_" + currentFidele.getId());
                                documentService.openPdf(file);
                                NotificationUtil.showSuccess("PDF prêt", "Fiche ouverte avec succès.");
                            } catch (Exception e) {
                                NotificationUtil.showError("Erreur PDF", e.getMessage());
                            }
                        }
                    });
                });
    }

    private void applyDemoFidele() {
        FideleDto demo = new FideleDto();
        demo.setId(1L);
        demo.setNom("DUPONT");
        demo.setPrenoms("Jean Emmanuel");
        demo.setTelephone("+228 90 12 34 56");
        demo.setEmail("jean.dupont@email.com");
        demo.setQuartier("Adidogomé");
        demo.setAdresse("15 Rue de l'Union, Lomé");
        demo.setProfession("Ingénieur Réseaux");
        demo.setStatutMatrimonial(com.eglise.secretariat.models.enums.Statut.MARIE);
        demo.setNomConjoint("Marie Dupont");
        demo.setNombreGarcons(1);
        demo.setNombreFilles(1);
        demo.setCarteMembreValide(true);
        demo.setPayeDimes(true);
        demo.setBaptise(true);
        this.currentFidele = demo;
        populateUI(demo);
    }
}
