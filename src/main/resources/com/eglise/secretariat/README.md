# 🎨 Package `resources`

> Ressources de l'application : vues FXML, feuilles de style CSS, images officielles et templates HTML pour la génération de documents PDF.

---

## 📁 Structure

```
resources/com/eglise/secretariat/
├── css/
│   └── main.css                         # Feuille de style globale (31 Ko)
├── fxml/
│   ├── auth/
│   │   └── LoginView.fxml               # Écran de connexion
│   ├── dashboard/
│   │   └── DashboardView.fxml           # Tableau de bord
│   ├── documents/
│   │   └── DocumentsView.fxml           # Interface de génération PDF
│   ├── fideles/
│   │   ├── FidelesListView.fxml         # Liste paginée des fidèles
│   │   ├── FideleDetailsView.fxml       # Fiche détaillée
│   │   ├── FideleWizardView.fxml        # Wizard d'inscription (4 étapes)
│   │   └── FideleEditDialog.fxml        # Formulaire d'édition
│   ├── layout/
│   │   └── MainLayoutView.fxml          # Layout principal (sidebar + content)
│   ├── mouvements/
│   │   └── MouvementsOcrView.fxml       # Interface OCR & mouvements
│   └── profile/
│       └── ProfileSettingsView.fxml     # Paramètres du profil
├── images/
│   ├── logo.png                         # Logo de l'application (416 Ko)
│   └── logo_ad.png                      # Logo officiel Assemblées de Dieu (744 Ko)
└── templates/
    ├── fiche_inscription.html           # Template HTML → PDF fiche individuelle
    └── lettre_recommandation.html       # Template HTML → PDF lettre de recommandation
```

---

## 📂 Détails par dossier

### 🎨 `css/main.css`

Feuille de style **unique et centralisée** (~31 Ko) définissant l'intégralité du thème visuel de l'application.

#### Palette de couleurs

| Couleur | Hex | Usage |
|---|---|---|
| **Bleu Roi AD** | `#00236f` | Couleur primaire, sidebar, boutons principaux |
| **Or Ambre** | *(doré)* | Accents, badges, éléments de mise en valeur |
| **Blanc** | `#ffffff` | Arrière-plan des zones de contenu |
| **Gris clair** | `#f8f9fa` | Arrière-plans secondaires |

#### Couverture

- Composants JavaFX standard : `Button`, `TextField`, `ComboBox`, `TableView`, `Label`, `ScrollPane`, etc.
- Composants personnalisés : sidebar, cards, badges de statut, wizard steps, drag & drop zone
- Dialogues de confirmation (standard et suppression)
- États interactifs : hover, focused, pressed, disabled

---

### 🖼️ `fxml/`

**10 fichiers FXML** organisés en 7 sous-dossiers alignés avec la structure des contrôleurs.

| Dossier | Fichier | Contrôleur associé |
|---|---|---|
| `auth/` | `LoginView.fxml` | `LoginController` |
| `dashboard/` | `DashboardView.fxml` | `DashboardController` |
| `documents/` | `DocumentsView.fxml` | `DocumentsController` |
| `fideles/` | `FidelesListView.fxml` | `FidelesListController` |
| `fideles/` | `FideleDetailsView.fxml` | `FideleDetailsController` |
| `fideles/` | `FideleWizardView.fxml` | `FideleWizardController` |
| `fideles/` | `FideleEditDialog.fxml` | `FideleEditController` |
| `layout/` | `MainLayoutView.fxml` | `MainLayoutController` |
| `mouvements/` | `MouvementsOcrView.fxml` | `MouvementsOcrController` |
| `profile/` | `ProfileSettingsView.fxml` | `ProfileSettingsController` |

> Chaque FXML référence son contrôleur via `fx:controller` et utilise les classes CSS de `main.css`.

---

### 🖼️ `images/`

| Fichier | Taille | Usage |
|---|---|---|
| `logo.png` | 416 Ko | Logo affiché dans l'application (écran de login, sidebar) |
| `logo_ad.png` | 744 Ko | Logo officiel des Assemblées de Dieu (documents PDF) |

---

### 📝 `templates/`

Templates **HTML** utilisés côté backend pour la génération de documents PDF via Apache PDFBox. Ces fichiers sont référencés ici pour cohérence, mais le rendu est effectué par l'API Spring Boot.

| Fichier | Description |
|---|---|
| `fiche_inscription.html` | Modèle de la fiche d'inscription individuelle d'un fidèle |
| `lettre_recommandation.html` | Modèle de la lettre de recommandation officielle |

---

## 🔗 Chargement des ressources

Les ressources sont chargées via le classpath Java :

```java
// FXML
FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eglise/secretariat/fxml/..."));

// CSS
scene.getStylesheets().add(getClass().getResource("/com/eglise/secretariat/css/main.css").toExternalForm());

// Images
new Image(getClass().getResourceAsStream("/com/eglise/secretariat/images/logo.png"));
```

---

← [Retour au README principal](../../../../../../README.md)
