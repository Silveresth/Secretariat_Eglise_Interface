# 🎮 Package `controllers`

> Contrôleurs MVC JavaFX gérant la logique d'interaction utilisateur pour chaque vue FXML.

---

## 📁 Structure

```
controllers/
├── BaseController.java                  # Classe abstraite parente
├── auth/
│   └── LoginController.java             # Écran de connexion
├── dashboard/
│   └── DashboardController.java         # Tableau de bord analytique
├── documents/
│   └── DocumentsController.java         # Génération de documents PDF
├── fideles/
│   ├── FidelesListController.java       # Liste paginée & recherche
│   ├── FideleDetailsController.java     # Fiche détaillée d'un fidèle
│   ├── FideleWizardController.java      # Wizard d'inscription (4 étapes)
│   └── FideleEditController.java        # Édition en place d'un fidèle
├── layout/
│   └── MainLayoutController.java        # Layout principal (sidebar + content area)
├── mouvements/
│   └── MouvementsOcrController.java     # Scanner OCR & mouvements de fidèles
└── profile/
    └── ProfileSettingsController.java   # Paramètres du profil utilisateur
```

---

## 📄 Classes

### `BaseController` *(abstract)*

Classe abstraite dont héritent tous les contrôleurs. Fournit :

- Accès direct au `NavigationService` (singleton)
- Méthode utilitaire `executeAsync()` pour lancer un `CompletableFuture` avec gestion automatique :
  - Affichage/masquage d'un indicateur de chargement (`Node`)
  - Callback `onSuccess` exécuté sur le **JavaFX Application Thread**
  - Callback `onError` avec notification d'erreur par défaut

```java
protected <T> void executeAsync(
    CompletableFuture<T> future,
    Node loadingIndicator,
    Consumer<T> onSuccess
);
```

---

### 🔐 `auth/LoginController`

| Responsabilité | Détail |
|---|---|
| Authentification | Appel `AuthService.login()` avec validation des champs |
| Navigation | Redirection vers `MainLayout` après succès |
| UX | Toggle visibilité mot de passe, option « Se souvenir de moi » |

---

### 📊 `dashboard/DashboardController`

| Responsabilité | Détail |
|---|---|
| Statistiques | Affichage : total inscrits, répartition par quartier, taux conformité dîme |
| Flux mensuels | Graphiques d'entrées/sorties de fidèles |
| Actions rapides | Boutons vers inscription, OCR, documents, recherche |

---

### 📄 `documents/DocumentsController`

| Responsabilité | Détail |
|---|---|
| Lettre de recommandation | Choix du fidèle, motif, église destination, pasteur signataire |
| Fiche d'inscription | Génération PDF de la fiche individuelle |
| Aperçu PDF | Rendu via Apache PDFBox dans un `ImageView` |
| Téléchargement | Sauvegarde automatique dans `~/Downloads` |

---

### 👥 `fideles/` — 4 contrôleurs

#### `FidelesListController`
- Recherche paginée multi-critères (nom, quartier, baptême, statut actif)
- Tableau `TableView` avec colonnes dynamiques et badges de statut
- Navigation vers détails, édition, suppression

#### `FideleDetailsController`
- Affichage complet de la fiche d'un fidèle (identité, contact, famille, parcours spirituel)
- Badges visuels : baptisé, actif, carte de membre, carnet de dîme
- Actions : modifier, supprimer, imprimer la fiche

#### `FideleWizardController`
- Wizard en **4 étapes** avec validation par étape :
  1. **Identité & Profession** — nom, prénom, sexe, date de naissance, profession
  2. **Coordonnées & Filiation** — téléphone, adresse, parents
  3. **Famille & Situation Matrimoniale** — statut, conjoint, enfants
  4. **Parcours Spirituel & Conformité** — conversion, baptême, dîme, engagements
- Barre de progression visuelle
- Sauvegarde via `FideleService.createFidele()`

#### `FideleEditController`
- Formulaire d'édition pré-rempli avec les données existantes
- Mêmes sections que le wizard mais en mode édition
- Sauvegarde via `FideleService.updateFidele()`

---

### 🏠 `layout/MainLayoutController`

| Responsabilité | Détail |
|---|---|
| Sidebar | Menu de navigation avec icônes (Ikonli Material Design) |
| Content Area | Zone de chargement dynamique des vues FXML enfant |
| Header | Affichage nom utilisateur, rôle, bouton déconnexion |
| `loadView(View)` | Chargement d'une vue dans le content area central |

---

### 🔍 `mouvements/MouvementsOcrController`

| Responsabilité | Détail |
|---|---|
| Drag & Drop | Zone de dépôt de fichiers (PDF, JPG, PNG) |
| Scan OCR | Upload multipart vers le backend, extraction des données |
| Pré-remplissage | Remplissage automatique des champs à partir du résultat OCR |
| Fidèle entrant | Enregistrement du fidèle avec lettre de recommandation validée |
| Conformité | Mise à jour carte de membre et carnet de dîme (`PATCH`) |

---

### 👤 `profile/ProfileSettingsController`

| Responsabilité | Détail |
|---|---|
| Affichage | Nom d'utilisateur, email, rôle de la session courante |
| Modification | Changement de nom d'utilisateur et/ou mot de passe |
| Validation | Vérification mot de passe actuel obligatoire |

---

## ⚙️ Pattern commun

Tous les contrôleurs suivent le même pattern asynchrone :

```
Interaction utilisateur → Service.methodAsync()
                              ↓
                    BaseController.executeAsync()
                              ↓
              ┌── loadingIndicator.setVisible(true)
              ├── CompletableFuture exécuté en background
              └── Platform.runLater {
                    loadingIndicator.setVisible(false)
                    onSuccess(result) || onError(throwable)
                  }
```

---

## 🔗 Dépendances

| Package | Usage |
|---|---|
| `services` | Appels API métier (AuthService, FideleService, etc.) |
| `dto` | Objets de transfert de données |
| `utils` | NavigationService, DialogUtil, NotificationUtil, AsyncHelper, DateUtil |
| `config` | SessionManager (état d'authentification) |

---

← [Retour au README principal](../../../../../../../../README.md)
