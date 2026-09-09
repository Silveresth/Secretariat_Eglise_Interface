# 🔨 Package `utils`

> Utilitaires transversaux utilisés par l'ensemble des contrôleurs pour la navigation, les dialogues, les notifications, les dates et l'exécution asynchrone.

---

## 📁 Structure

```
utils/
├── AsyncHelper.java            # Exécution asynchrone sécurisée (FX Thread)
├── DateUtil.java               # Formatage de dates en français
├── DialogUtil.java             # Dialogues de confirmation stylisés
├── NavigationService.java      # Service de navigation centralisé (singleton)
└── NotificationUtil.java       # Système de toasts / notifications
```

---

## 📄 Classes

### `AsyncHelper`

Utilitaire statique pour exécuter des `CompletableFuture` avec gestion automatique du **JavaFX Application Thread**.

```java
public static <T> void execute(
    CompletableFuture<T> future,
    Node loadingIndicator,      // affiché pendant l'exécution (nullable)
    Consumer<T> onSuccess,       // exécuté sur le FX Thread
    Consumer<Throwable> onError  // exécuté sur le FX Thread
);
```

#### Flux d'exécution

```
1. Platform.runLater → loadingIndicator.setVisible(true)
2. future.whenComplete →
   3. Platform.runLater →
      ├── loadingIndicator.setVisible(false)
      ├── Si succès → onSuccess.accept(result)
      └── Si erreur → onError.accept(cause) ou NotificationUtil.showError()
```

> Tous les callbacks s'exécutent sur le **FX Thread** via `Platform.runLater()`, garantissant la sécurité thread pour la mise à jour de l'UI.

---

### `DateUtil`

Formatage de dates en **français** avec calcul d'âge.

| Méthode | Format / Résultat | Exemple |
|---|---|---|
| `formatFrench(date)` | `dd MMMM yyyy` | `15 mars 2024` |
| `formatShort(date)` | `dd/MM/yyyy` | `15/03/2024` |
| `formatWithAge(birthDate)` | `dd MMMM yyyy (X ans)` | `10 janvier 1990 (34 ans)` |
| `calculateAge(birthDate)` | `int` | `34` |

> Utilise `Locale.FRENCH` pour les noms de mois en français. Retourne `"-"` pour les dates nulles.

---

### `DialogUtil`

Dialogues de confirmation personnalisés avec style CSS de l'application.

#### Méthodes publiques

| Méthode | Description |
|---|---|
| `showConfirmation(title, header, content)` | Affiche un dialogue standard ou de suppression (détecté automatiquement via le mot « suppr/delete » dans le titre/header) |
| `showDeleteConfirmation(title, header, content)` | Dialogue de suppression avec icône 🗑️, badge « Action Irréversible », bouton rouge |
| `showStandardConfirmation(title, header, content)` | Dialogue standard avec icône ℹ️, boutons Bleu Roi |

#### Design

- **Delete** : Palette rouge (`#991b1b`), icône poubelle, badge d'avertissement
- **Standard** : Palette Bleu Roi (`#00236f`), icône info
- Boutons personnalisés (pas les boutons JavaFX par défaut)
- CSS de l'application appliqué automatiquement

---

### `NavigationService` *(Singleton)*

Service centralisé de navigation entre les vues de l'application.

#### Enum `View`

| Valeur | FXML associé |
|---|---|
| `LOGIN` | `auth/LoginView.fxml` |
| `MAIN_LAYOUT` | `layout/MainLayoutView.fxml` |
| `DASHBOARD` | `dashboard/DashboardView.fxml` |
| `FIDELES_LIST` | `fideles/FidelesListView.fxml` |
| `FIDELE_DETAILS` | `fideles/FideleDetailsView.fxml` |
| `FIDELE_WIZARD` | `fideles/FideleWizardView.fxml` |
| `FIDELE_EDIT` | `fideles/FideleEditDialog.fxml` |
| `MOUVEMENTS_OCR` | `mouvements/MouvementsOcrView.fxml` |
| `DOCUMENTS_PDF` | `documents/DocumentsView.fxml` |
| `PROFILE_SETTINGS` | `profile/ProfileSettingsView.fxml` |

#### Méthodes principales

| Méthode | Description |
|---|---|
| `navigateToLogin()` | Charge la vue de connexion (crée ou remplace la scène) |
| `navigateToMain()` | Charge le layout principal + dashboard par défaut |
| `navigateToContent(view, params)` | Charge une vue dans le content area du `MainLayout` |
| `getParameter(key)` / `setParameter(key, value)` | Passage de paramètres entre vues (ex : ID du fidèle sélectionné) |

#### Configuration

- **Résolution fixe** : `1280 × 768` pixels, fenêtre non redimensionnable
- **CSS centralisé** : `main.css` chargé une seule fois dans la `Scene`
- **Centrage automatique** de la fenêtre à l'écran

---

### `NotificationUtil`

Système de notifications par type avec logging console.

#### Types de toast

| Type | Couleur | Icône | Durée par défaut |
|---|---|---|---|
| `SUCCESS` | `#10b981` (vert) | ✓ | 3 500 ms |
| `INFO` | `#38bdf8` (bleu clair) | ℹ | 3 000 ms |
| `WARNING` | `#fbbf24` (jaune) | ⚠ | 4 500 ms |
| `ERROR` | `#f87171` (rouge) | ✕ | 5 500 ms |

#### Méthodes

```java
NotificationUtil.showSuccess("Titre", "Message");
NotificationUtil.showInfo("Titre", "Message");
NotificationUtil.showWarning("Titre", "Message");
NotificationUtil.showError("Titre", "Message");
```

> Actuellement, les toasts sont loggés en console (`System.out.println`). L'affichage visuel flottant est désactivé.

---

## 🔗 Dépendances

| Package | Usage |
|---|---|
| `config` | `AppConfig` (titre de l'application) |
| `controllers.layout` | `MainLayoutController` (chargement des vues enfant) |
| JavaFX | `Platform`, `Stage`, `Scene`, `FXMLLoader`, `Node`, `Dialog` |

---

← [Retour au README principal](../../../../../../../../README.md)
