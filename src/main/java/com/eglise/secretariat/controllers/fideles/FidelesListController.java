package com.eglise.secretariat.controllers.fideles;

import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.dto.FideleDto;
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
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FidelesListController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> quartierCombo;
    @FXML private ComboBox<String> baptiseCombo;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label paginationLabel;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;

    @FXML private TableView<FideleDto> fidelesTable;
    @FXML private TableColumn<FideleDto, Void> colPhoto;
    @FXML private TableColumn<FideleDto, String> colNomPrenom;
    @FXML private TableColumn<FideleDto, String> colTelephone;
    @FXML private TableColumn<FideleDto, String> colQuartier;
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
            baptiseCombo.setItems(FXCollections.observableArrayList("Tous les baptêmes", "Baptisé(e)", "Non Baptisé(e)"));
            baptiseCombo.setValue("Tous les baptêmes");
            baptiseCombo.setOnAction(e -> applyFilter());
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
                        setAlignment(Pos.CENTER);
                        setGraphic(box);
                    }
                }
            });
        }

        if (colNomPrenom != null) {
            colNomPrenom.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNomComplet()));
            colNomPrenom.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setAlignment(Pos.CENTER);
                        setTextAlignment(TextAlignment.CENTER);
                    }
                }
            });
        }

        if (colTelephone != null) {
            colTelephone.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getTelephone() != null ? cell.getValue().getTelephone() : "-"
            ));
            colTelephone.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setAlignment(Pos.CENTER);
                        setTextAlignment(TextAlignment.CENTER);
                    }
                }
            });
        }

        if (colQuartier != null) {
            colQuartier.setCellValueFactory(cell -> new SimpleStringProperty(
                    cell.getValue().getQuartier() != null ? cell.getValue().getQuartier() : "-"
            ));
            colQuartier.setCellFactory(param -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setAlignment(Pos.CENTER);
                        setTextAlignment(TextAlignment.CENTER);
                    }
                }
            });
        }

        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button viewBtn = new Button("👁");
                private final Button editBtn = new Button("✎");
                private final Button pdfBtn = new Button("📄");
                private final Button delBtn = new Button("🗑");
                private final HBox container = new HBox(6, viewBtn, editBtn, pdfBtn, delBtn);

                {
                    container.setAlignment(Pos.CENTER);
                    viewBtn.getStyleClass().addAll("btn-action-icon", "btn-action-view");
                    viewBtn.setTooltip(new Tooltip("Voir la fiche du fidèle"));

                    editBtn.getStyleClass().addAll("btn-action-icon", "btn-action-edit");
                    editBtn.setTooltip(new Tooltip("Modifier les informations"));

                    pdfBtn.getStyleClass().addAll("btn-action-icon", "btn-action-pdf");
                    pdfBtn.setTooltip(new Tooltip("Exporter la fiche individuelle PDF"));

                    delBtn.getStyleClass().addAll("btn-action-icon", "btn-action-delete");
                    delBtn.setTooltip(new Tooltip("Supprimer le fidèle"));

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
                        setAlignment(Pos.CENTER);
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
    private void handleResetFilters(ActionEvent event) {
        if (searchField != null) searchField.clear();
        if (quartierCombo != null) quartierCombo.setValue("Tous les quartiers");
        if (baptiseCombo != null) baptiseCombo.setValue("Tous les baptêmes");
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

                        if (fidelesTable != null) {
                            fidelesTable.refresh();
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
        f1.setNom("KOUASSI");
        f1.setPrenoms("Jean-Paul");
        f1.setTelephone("+228 90 12 34 56");
        f1.setQuartier("Adidogomé");

        FideleDto f2 = new FideleDto();
        f2.setId(2L);
        f2.setNom("MENSAH");
        f2.setPrenoms("Koffi Paul");
        f2.setTelephone("+228 99 88 77 66");
        f2.setQuartier("Bè");

        FideleDto f3 = new FideleDto();
        f3.setId(3L);
        f3.setNom("LAWSON");
        f3.setPrenoms("Afi Sarah");
        f3.setTelephone("+228 92 33 44 55");
        f3.setQuartier("Agoè");

        fidelesList.setAll(f1, f2, f3);
        totalElements = 3;
        totalPages = 1;
        updatePaginationUI();
    }
}
