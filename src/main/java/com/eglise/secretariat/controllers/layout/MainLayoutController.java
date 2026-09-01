package com.eglise.secretariat.controllers.layout;

import com.eglise.secretariat.config.SessionManager;
import com.eglise.secretariat.controllers.BaseController;
import com.eglise.secretariat.utils.NavigationService;
import com.eglise.secretariat.utils.NotificationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainLayoutController extends BaseController {

    @FXML private StackPane contentArea;
    @FXML private Button navDashboardBtn;
    @FXML private Button navFidelesBtn;
    @FXML private Button navDocumentsBtn;
    @FXML private Button navMouvementsBtn;
    @FXML private Button navDimesBtn;
    @FXML private Button navSettingsBtn;
    
    @FXML private Label userFullNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private TextField globalSearchField;
    @FXML private MenuButton userMenuButton;

    private NavigationService.View currentView;
    private final Map<NavigationService.View, Button> navButtons = new HashMap<>();

    @FXML
    public void initialize() {
        navigationService.setMainLayoutController(this);

        // Map views to buttons
        navButtons.put(NavigationService.View.DASHBOARD, navDashboardBtn);
        navButtons.put(NavigationService.View.FIDELES_LIST, navFidelesBtn);
        navButtons.put(NavigationService.View.DOCUMENTS_PDF, navDocumentsBtn);
        navButtons.put(NavigationService.View.MOUVEMENTS_OCR, navMouvementsBtn);
        navButtons.put(NavigationService.View.PROFILE_SETTINGS, navSettingsBtn);

        // Update User info
        updateUserInfo();

        SessionManager.getInstance().addListener(isAuthenticated -> {
            Platform.runLater(this::updateUserInfo);
        });

        // Global search enter key handler
        if (globalSearchField != null) {
            globalSearchField.setOnAction(e -> {
                String q = globalSearchField.getText();
                if (q != null && !q.trim().isEmpty()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("searchQuery", q.trim());
                    navigationService.navigateToContent(NavigationService.View.FIDELES_LIST, params);
                }
            });
        }
    }

    private void updateUserInfo() {
        SessionManager sm = SessionManager.getInstance();
        if (userFullNameLabel != null) {
            userFullNameLabel.setText(sm.getUsername() != null ? sm.getUsername() : "Secrétaire Général");
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText(sm.getRole() != null ? sm.getRole() : "Admin");
        }
    }

    public void loadView(NavigationService.View view) {
        try {
            this.currentView = view;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFxmlPath()));
            Parent viewRoot = loader.load();

            contentArea.getChildren().setAll(viewRoot);
            updateNavHighlights(view);
        } catch (IOException e) {
            e.printStackTrace();
            NotificationUtil.showError("Erreur de chargement", "Impossible de charger la vue: " + e.getMessage());
        }
    }

    private void updateNavHighlights(NavigationService.View activeView) {
        navButtons.forEach((view, btn) -> {
            if (btn != null) {
                btn.getStyleClass().remove("nav-item-active");
                if (view == activeView || 
                   (activeView == NavigationService.View.FIDELE_DETAILS && view == NavigationService.View.FIDELES_LIST) ||
                   (activeView == NavigationService.View.FIDELE_WIZARD && view == NavigationService.View.FIDELES_LIST) ||
                   (activeView == NavigationService.View.FIDELE_EDIT && view == NavigationService.View.FIDELES_LIST)) {
                    btn.getStyleClass().add("nav-item-active");
                }
            }
        });
    }

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        loadView(NavigationService.View.DASHBOARD);
    }

    @FXML
    private void handleNavFideles(ActionEvent event) {
        loadView(NavigationService.View.FIDELES_LIST);
    }

    @FXML
    private void handleNavDocuments(ActionEvent event) {
        loadView(NavigationService.View.DOCUMENTS_PDF);
    }

    @FXML
    private void handleNavMouvements(ActionEvent event) {
        loadView(NavigationService.View.MOUVEMENTS_OCR);
    }

    @FXML
    private void handleNavDimes(ActionEvent event) {
        // Can open fideles filtered or dedicated view
        Map<String, Object> params = new HashMap<>();
        params.put("filterDimes", true);
        navigationService.navigateToContent(NavigationService.View.FIDELES_LIST, params);
    }

    @FXML
    private void handleNavSettings(ActionEvent event) {
        loadView(NavigationService.View.PROFILE_SETTINGS);
    }

    @FXML
    private void handleQuickRegisterFidele(ActionEvent event) {
        loadView(NavigationService.View.FIDELE_WIZARD);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        NotificationUtil.showInfo("Déconnexion", "Vous avez été déconnecté.");
        navigationService.navigateToLogin();
    }
}
