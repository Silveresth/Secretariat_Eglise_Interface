package com.eglise.secretariat.controllers.fideles;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.dto.FideleDto;
import com.eglise.secretariat.dto.PageResponseDto;
import com.eglise.secretariat.models.enums.Statut;
import com.eglise.secretariat.services.DocumentService;
import com.eglise.secretariat.services.FideleService;
import com.eglise.secretariat.utils.DialogUtil;
import com.eglise.secretariat.utils.NavigationService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FidelesListController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> quartierCombo;
    @FXML private ComboBox<String> baptiseCombo;
    @FXML private ComboBox<String> dimeCombo;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label paginationLabel;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;

    @FXML private TableView<FideleDto> fidelesTable;
    @FXML private TableColumn<FideleDto, Void> colPhoto;
    @FXML private TableColumn<FideleDto, String> colNomPrenom;
    @FXML private TableColumn<FideleDto, String> colTelephone;
    @FXML private TableColumn<FideleDto, String> colQuartier;
    @FXML private TableColumn<FideleDto, String> colMatrimonial;
    @FXML private TableColumn<FideleDto, Void> colCarte;
    @FXML private TableColumn<FideleDto, Void> colDime;
    @FXML private TableColumn<FideleDto, Void> colActions;

    private final FideleService fideleService = new FideleService();
    private final DocumentService documentService = new DocumentService();
    private final ObservableList<FideleDto> fidelesList = FXCollections.observableArrayList();

    private int currentPage = 0;
    private final int pageSize = 15;
    private int totalPages = 1;
    private long totalElements = 0;

    @FXML
    public void initialize() {
        setupFilterCombos();
        setupTableColumns();

        // Check if navigated with search parameter
        Object searchParam = navigationService.getParameter("searchQuery");
        if (searchParam != null) {
            searchField.setText(String.valueOf(searchParam));
            navigationService.clearParameters();
        }

        loadFideles();
    }

    private void setupFilterCombos() {
        if (quartierCombo != null) {
            quartierCombo.setItems(FXCollections.observableArrayList("Tous les quartiers", "Adidogomé", "Bè", "Agoè", "Tokoin", "Hedzranawoé"));
            quartierCombo.setValue("Tous les quartiers");
            quartierCombo.setOnAction(e -> applyFilter());
        }
        if (baptiseCombo != null) {
            baptiseCombo.setItems(FXCollections.observableArrayList("Tous", "Baptisé(e)", "Non Baptisé(e)"));
            baptiseCombo.setValue("Tous");
            baptiseCombo.setOnAction(e -> applyFilter());
        }
        if (dimeCombo != null) {
            dimeCombo.setItems(FXCollections.observableArrayList("Tous", "À jour", "En retard"));
            dimeCombo.setValue("Tous");
            dimeCombo.setOnAction(e -> applyFilter());
        }
        if (searchField != null) {
            searchField.setOnAction(e -> applyFilter());
        }
    }

    private void setupTableColumns() {
        if (colPhoto != null) {
            colPhoto.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        FideleDto f = getTableRow().getItem();
                        String initials = getInitials(f);
                        Label avatar = new Label(initials);
                        avatar.setStyle("-fx-background-color: #dce1ff; -fx-text-fill: #00236f; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-alignment: center;");
                        HBox box = new HBox(avatar);
                        box.setAlignment(Pos.CENTER);
                        setGraphic(box);
                    }
                }
            });
        }

        if (colNomPrenom != null) {
            colNomPrenom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNomComplet()));
        }

        if (colTelephone != null) {
            colTelephone.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getTelephone() != null ? cell.getValue().getTelephone() : "-"
            ));
        }

        if (colQuartier != null) {
            colQuartier.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getQuartier() != null ? cell.getValue().getQuartier() : "-"
            ));
        }

        if (colMatrimonial != null) {
            colMatrimonial.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getStatutMatrimonial() != null ? cell.getValue().getStatutMatrimonial().getLabel() : "-"
            ));
        }

        if (colCarte != null) {
            colCarte.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        FideleDto f = getTableRow().getItem();
                        Label badge = new Label();
                        if (Boolean.TRUE.equals(f.getCarteMembreValide())) {
                            badge.setText("VALIDE");
                            badge.getStyleClass().addAll("badge-success");
                        } else {
                            badge.setText("EXPIRÉ");
                            badge.getStyleClass().addAll("badge-error");
                        }
                        HBox box = new HBox(badge);
                        box.setAlignment(Pos.CENTER);
                        setGraphic(box);
                    }
                }
            });
        }

        if (colDime != null) {
            colDime.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        FideleDto f = getTableRow().getItem();
                        Label badge = new Label();
                        if (Boolean.TRUE.equals(f.getPayeDimes()) || Boolean.TRUE.equals(f.getCarnetDimeValide())) {
                            badge.setText("À JOUR");
                            badge.getStyleClass().addAll("badge-success");
                        } else {
                            badge.setText("EN RETARD");
                            badge.getStyleClass().addAll("badge-warning");
                        }
                        HBox box = new HBox(badge);
                        box.setAlignment(Pos.CENTER);
                        setGraphic(box);
                    }
                }
            });
        }

        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button viewBtn = new Button("Voir");
                private final Button editBtn = new Button("Modifier");
                private final Button pdfBtn = new Button("PDF");
                private final Button delBtn = new Button("Supprimer");
                private final HBox container = new HBox(6, viewBtn, editBtn, pdfBtn, delBtn);

                {
                    container.setAlignment(Pos.CENTER_RIGHT);
                    viewBtn.getStyleClass().addAll("btn-outline");
                    viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");
                    editBtn.getStyleClass().addAll("btn-outline");
                    editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");
                    pdfBtn.getStyleClass().addAll("btn-outline");
                    pdfBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");
                    delBtn.getStyleClass().addAll("btn-danger");
                    delBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");

                    viewBtn.setOnAction(e -> {
                        FideleDto f = getTableRow().getItem();
                        if (f != null && f.getId() != null) {
                            Map<String, Object> params = new HashMap<>();
                            params.put("fideleId", f.getId());
                            navigationService.navigateToContent(NavigationService.View.FIDELE_DETAILS, params);
                        }
                    });

                    editBtn.setOnAction(e -> {
                        FideleDto f = getTableRow().getItem();
                        if (f != null && f.getId() != null) {
                            Map<String, Object> params = new HashMap<>();
                            params.put("fideleId", f.getId());
                            navigationService.navigateToContent(NavigationService.View.FIDELE_EDIT, params);
                        }
                    });

                    pdfBtn.setOnAction(e -> {
                        FideleDto f = getTableRow().getItem();
                        if (f != null && f.getId() != null) {
                            exportFidelePdf(f);
                        }
                    });

                    delBtn.setOnAction(e -> {
                        FideleDto f = getTableRow().getItem();
                        if (f != null && f.getId() != null) {
                            confirmDeleteFidele(f);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(container);
                    }
                }
            });
        }

        if (fidelesTable != null) {
            fidelesTable.setItems(fidelesList);
        }
    }

    private String getInitials(FideleDto f) {
        String n = f.getNom() != null && !f.getNom().isEmpty() ? f.getNom().substring(0, 1).toUpperCase() : "";
        String p = f.getPrenoms() != null && !f.getPrenoms().isEmpty() ? f.getPrenoms().substring(0, 1).toUpperCase() : "";
        return (n + p).isEmpty() ? "F" : (n + p);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        currentPage = 0;
        loadFideles();
    }

    @FXML
    private void handleNewFidele(ActionEvent event) {
        navigationService.navigateToContent(NavigationService.View.FIDELE_WIZARD);
    }

    @FXML
    private void handlePrevPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            loadFideles();
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadFideles();
        }
    }

    private void applyFilter() {
        currentPage = 0;
        loadFideles();
    }

    public void loadFideles() {
        if (loadingIndicator != null) loadingIndicator.setVisible(true);

        String query = searchField != null ? searchField.getText() : null;
        String qQuartier = quartierCombo != null && !"Tous les quartiers".equals(quartierCombo.getValue()) ? quartierCombo.getValue() : null;
        
        Boolean qBaptise = null;
        if (baptiseCombo != null && "Baptisé(e)".equals(baptiseCombo.getValue())) qBaptise = true;
        if (baptiseCombo != null && "Non Baptisé(e)".equals(baptiseCombo.getValue())) qBaptise = false;

        fideleService.searchFideles(query, qQuartier, qBaptise, true, currentPage, pageSize, "nom,asc")
                .whenComplete((page, throwable) -> {
                    Platform.runLater(() -> {
                        if (loadingIndicator != null) loadingIndicator.setVisible(false);

                        if (throwable == null && page != null) {
                            fidelesList.setAll(page.getContent());
                            totalPages = Math.max(1, page.getTotalPages());
                            totalElements = page.getTotalElements();
                            updatePaginationUI();
                        } else {
                            // Offline fallback sample data
                            applySampleFideles();
                        }
                    });
                });
    }

    private void updatePaginationUI() {
        int start = totalElements == 0 ? 0 : (currentPage * pageSize + 1);
        int end = (int) Math.min((long) (currentPage + 1) * pageSize, totalElements);
        if (paginationLabel != null) {
            paginationLabel.setText(String.format("Affichage de %d à %d sur %d fidèles", start, end, totalElements));
        }
        if (prevPageBtn != null) prevPageBtn.setDisable(currentPage <= 0);
        if (nextPageBtn != null) nextPageBtn.setDisable(currentPage >= totalPages - 1);
    }

    private void exportFidelePdf(FideleDto f) {
        NotificationUtil.showInfo("Export PDF", "Génération de la fiche PDF en cours...");
        documentService.getFidelePdf(f.getId())
                .whenComplete((bytes, throwable) -> {
                    Platform.runLater(() -> {
                        if (throwable != null) {
                            NotificationUtil.showError("Erreur PDF", throwable.getMessage());
                        } else if (bytes != null) {
                            try {
                                File file = documentService.savePdfToTemp(bytes, "fiche_" + f.getId());
                                documentService.openPdf(file);
                                NotificationUtil.showSuccess("PDF prêt", "Fiche du fidèle générée avec succès.");
                            } catch (Exception e) {
                                NotificationUtil.showError("Erreur ouverture PDF", e.getMessage());
                            }
                        }
                    });
                });
    }

    private void confirmDeleteFidele(FideleDto f) {
        boolean confirmed = DialogUtil.showConfirmation(
                "Confirmation de suppression",
                "Supprimer le fidèle " + f.getNomComplet() + " ?",
                "Cette action retirera le fidèle de l'annuaire actif."
        );

        if (confirmed) {
            fideleService.deleteFidele(f.getId())
                    .whenComplete((res, throwable) -> {
                        Platform.runLater(() -> {
                            if (throwable != null) {
                                NotificationUtil.showError("Erreur suppression", throwable.getMessage());
                            } else {
                                NotificationUtil.showSuccess("Suppression", "Le fidèle a été supprimé.");
                                loadFideles();
                            }
                        });
                    });
        }
    }

    private void applySampleFideles() {
        FideleDto f1 = new FideleDto();
        f1.setId(1L);
        f1.setNom("DOE");
        f1.setPrenoms("Jane Marie");
        f1.setTelephone("+228 90 12 34 56");
        f1.setQuartier("Adidogomé");
        f1.setStatutMatrimonial(Statut.CELIBATAIRE);
        f1.setCarteMembreValide(true);
        f1.setPayeDimes(true);

        FideleDto f2 = new FideleDto();
        f2.setId(2L);
        f2.setNom("MENSAH");
        f2.setPrenoms("Koffi Paul");
        f2.setTelephone("+228 99 88 77 66");
        f2.setQuartier("Bè");
        f2.setStatutMatrimonial(Statut.MARIE);
        f2.setCarteMembreValide(false);
        f2.setPayeDimes(true);

        FideleDto f3 = new FideleDto();
        f3.setId(3L);
        f3.setNom("LAWSON");
        f3.setPrenoms("Afi Sarah");
        f3.setTelephone("+228 92 33 44 55");
        f3.setQuartier("Agoè");
        f3.setStatutMatrimonial(Statut.VEUF);
        f3.setCarteMembreValide(true);
        f3.setPayeDimes(false);

        fidelesList.setAll(f1, f2, f3);
        totalElements = 3;
        totalPages = 1;
        updatePaginationUI();
    }
}
