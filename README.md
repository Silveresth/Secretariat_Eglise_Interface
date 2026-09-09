<p align="center">
  <img src="src/main/resources/com/eglise/secretariat/images/logo.png" alt="Logo Ecclesia Admin" width="100" />
</p>

<h1 align="center">Ecclesia Admin Desktop</h1>

<p align="center">
  <strong>Application de bureau JavaFX pour la gestion administrative du secrétariat ecclésiastique</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%20LTS-007396?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/JavaFX-21.0.2-4B8BBE?style=flat-square" alt="JavaFX 21" />
  <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat-square&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/Licence-Propriétaire-333?style=flat-square" alt="Licence" />
  <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=flat-square" alt="Version" />
</p>

---

## 📋 Table des Matières

- [Présentation](#-présentation)
- [Fonctionnalités](#-fonctionnalités)
- [Stack Technique](#-stack-technique)
- [Architecture du Projet](#-architecture-du-projet)
- [Prérequis](#-prérequis)
- [Installation & Lancement](#-installation--lancement)
- [Endpoints API Consommés](#-endpoints-api-consommés)
- [Documentation des Packages](#-documentation-des-packages)
- [Captures d'Écran](#-captures-décran)
- [Contribuer](#-contribuer)
- [Licence](#-licence)

---

## 🏛️ Présentation

**Ecclesia Admin Desktop** est une application de bureau moderne développée en **JavaFX 21** et **Java 17** pour la gestion administrative intégrale du secrétariat de l'**Église des Assemblées de Dieu du Togo — Temple « DIEU NE CHANGE PAS »** (Adidogomé, Lomé).

L'application communique via des **API REST sécurisées** (authentification JWT Bearer) avec le backend Spring Boot [`secretariat_api`](https://github.com/Silveresth/secretariat_api).

> **Architecture Client-Serveur** — Cette application est le **client desktop** (Frontend). Le traitement des données, la persistence et l'OCR sont délégués au backend Spring Boot.

---

## ✨ Fonctionnalités

### 📊 Tableau de Bord Analytique
- Statistiques en temps réel : total des membres, répartition par quartier, taux de conformité dîme
- Flux mensuels d'entrées/sorties de fidèles
- Actions rapides vers l'enregistrement, l'OCR, la génération de documents et la recherche

### 👥 Gestion Complète des Fidèles
- **Recherche avancée multi-critères** : nom, prénom, quartier, baptême, statut d'activité
- **Wizard d'inscription en 4 étapes** :
  1. Identité & Profession
  2. Coordonnées & Filiation
  3. Famille & Situation Matrimoniale
  4. Parcours Spirituel & Conformité
- **Fiche détaillée** avec badges de statut et **édition en place**
- **Suppression sécurisée** avec dialogue de confirmation personnalisé

### 🔍 Mouvements & Scanner OCR
- Glisser-déposer de lettres de recommandation scannées (PDF, JPG, PNG)
- Extraction optique côté backend avec pré-remplissage automatique des champs
- Enregistrement direct du fidèle entrant avec lettre validée
- Gestion de la conformité : carte de membre et carnet de dîme

### 📄 Génération de Documents PDF
- **Lettre de Recommandation Officielle** avec choix du motif, aperçu en direct et téléchargement
- **Fiche d'Inscription Individuelle** conforme au modèle de l'église
- Sauvegarde automatique dans le dossier Téléchargements

### 👤 Profil & Paramètres
- Modification du nom d'utilisateur et du mot de passe
- Affichage du rôle et de l'email de session

### 🔔 Notifications & Dialogues
- Système de toasts (succès, info, warning, erreur) avec logging console
- Dialogues de confirmation stylisés (standard et suppression)

---

## 🛠️ Stack Technique

| Composant | Technologie | Version |
|-----------|------------|---------|
| **Langage** | Java (LTS) | 17 |
| **Framework UI** | JavaFX | 21.0.2 |
| **Composants UI avancés** | ControlsFX | 11.2.1 |
| **Icônes vectorielles** | Ikonli (Material Design 2) | 12.3.1 |
| **Sérialisation JSON** | Jackson Databind + JSR310 | 2.17.1 |
| **Rendu / Parsing PDF** | Apache PDFBox | 3.0.2 |
| **Client HTTP** | `java.net.http.HttpClient` natif (HTTP/2) | — |
| **Build & Packaging** | Maven + `javafx-maven-plugin` | 3.9+ |
| **Tests** | JUnit Jupiter | 5.10.2 |
| **Style** | CSS3 — Palette *Bleu Roi AD / Or Ambre* | — |

---

## 🏗️ Architecture du Projet

```
Secretariat_Eglise_Interface/
├── pom.xml                          # Configuration Maven & dépendances
├── README.md                        # ← Vous êtes ici
│
├── src/main/java/com/eglise/secretariat/
│   ├── App.java                     # Point d'entrée JavaFX (Application.start)
│   ├── config/                      # 🔧 Configuration & Session     → README.md
│   ├── controllers/                 # 🎮 Contrôleurs MVC JavaFX      → README.md
│   │   ├── BaseController.java      #    Classe abstraite commune
│   │   ├── auth/                    #    Authentification
│   │   ├── dashboard/               #    Tableau de bord
│   │   ├── documents/               #    Génération PDF
│   │   ├── fideles/                 #    CRUD Fidèles (4 contrôleurs)
│   │   ├── layout/                  #    Layout principal (sidebar)
│   │   ├── mouvements/              #    OCR & Mouvements
│   │   └── profile/                 #    Profil utilisateur
│   ├── dto/                         # 📦 Data Transfer Objects        → README.md
│   ├── models/                      # 🗂️ Modèles & Énumérations      → README.md
│   │   └── enums/
│   ├── services/                    # ⚙️ Services métier & API        → README.md
│   │   └── api/                     #    Client HTTP générique
│   └── utils/                       # 🔨 Utilitaires transversaux     → README.md
│
└── src/main/resources/com/eglise/secretariat/
    ├── css/                         # 🎨 Feuilles de style            → README.md
    │   └── main.css
    ├── fxml/                        # 🖼️ Vues FXML
    │   ├── auth/
    │   ├── dashboard/
    │   ├── documents/
    │   ├── fideles/
    │   ├── layout/
    │   ├── mouvements/
    │   └── profile/
    ├── images/                      # 🖼️ Logos officiels AD
    └── templates/                   # 📝 Templates HTML (génération PDF)
```

---

## 📋 Prérequis

| Outil | Version minimum | Vérification |
|-------|----------------|--------------|
| **JDK** | 17+ | `java -version` |
| **Maven** | 3.9+ (ou wrapper inclus) | `mvn -version` |
| **Backend API** | `secretariat_api` démarré | `http://localhost:8081` |

---

## 🚀 Installation & Lancement

```powershell
# 1. Cloner le dépôt
git clone https://github.com/Silveresth/Secretariat_Eglise_Interface.git
cd Secretariat_Eglise_Interface

# 2. Lancer l'application (le wrapper Maven est inclus)
.\mvnw.cmd javafx:run
```

### Identifiants par défaut

| Champ | Valeur |
|-------|--------|
| **Utilisateur** | `secretaire` |
| **Mot de passe** | `secretaire123` |

> ⚠️ **Important** — Le backend `secretariat_api` doit être démarré sur `http://localhost:8081` avant le lancement du client.

---

## 🌐 Endpoints API Consommés

| Méthode | Endpoint | Description | Service |
|---------|----------|-------------|---------|
| `POST` | `/api/auth/login` | Authentification JWT | `AuthService` |
| `PUT` | `/api/auth/profile` | Mise à jour du profil | `AuthService` |
| `GET` | `/api/dashboard/stats` | Statistiques du tableau de bord | `DashboardService` |
| `GET` | `/api/fideles` | Recherche paginée des fidèles | `FideleService` |
| `GET` | `/api/fideles/{id}` | Détail d'un fidèle | `FideleService` |
| `POST` | `/api/fideles` | Création d'un fidèle | `FideleService` |
| `PUT` | `/api/fideles/{id}` | Modification d'un fidèle | `FideleService` |
| `DELETE` | `/api/fideles/{id}` | Suppression d'un fidèle | `FideleService` |
| `GET` | `/api/documents/fidele/{id}/pdf` | Téléchargement fiche PDF | `DocumentService` |
| `POST` | `/api/documents/lettre-recommandation/pdf` | Génération lettre de recommandation | `DocumentService` |
| `POST` | `/api/mouvements/ocr-scan` | Scan OCR (multipart) | `MouvementService` |
| `POST` | `/api/mouvements/fidele-entrant` | Enregistrement fidèle entrant | `MouvementService` |
| `PATCH` | `/api/mouvements/carte-membre/{id}` | Mise à jour carte de membre | `MouvementService` |
| `PATCH` | `/api/mouvements/carnet-dime/{id}` | Mise à jour carnet de dîme | `MouvementService` |

---

## 📚 Documentation des Packages

Chaque package dispose de son propre `README.md` avec la description détaillée des classes :

| Package | Description | Lien |
|---------|-------------|------|
| `config` | Configuration globale & gestion de session | [README](src/main/java/com/eglise/secretariat/config/README.md) |
| `controllers` | Contrôleurs MVC JavaFX (7 sous-packages) | [README](src/main/java/com/eglise/secretariat/controllers/README.md) |
| `dto` | Data Transfer Objects (10 classes) | [README](src/main/java/com/eglise/secretariat/dto/README.md) |
| `models` | Modèles de domaine & énumérations | [README](src/main/java/com/eglise/secretariat/models/README.md) |
| `services` | Services métier & client API HTTP | [README](src/main/java/com/eglise/secretariat/services/README.md) |
| `utils` | Utilitaires transversaux | [README](src/main/java/com/eglise/secretariat/utils/README.md) |
| `resources` | Ressources FXML, CSS, images, templates | [README](src/main/resources/com/eglise/secretariat/README.md) |

---

## 🖼️ Captures d'Écran

> _Section à compléter avec les captures d'écran de l'application._

---

## 🤝 Contribuer

1. **Forker** le dépôt
2. Créer une branche feature : `git checkout -b feature/ma-fonctionnalite`
3. Committer les modifications : `git commit -m "feat: description"`
4. Pousser la branche : `git push origin feature/ma-fonctionnalite`
5. Ouvrir une **Pull Request**

### Conventions
- **Architecture MVC** : les vues FXML sont dans `resources/fxml/`, les contrôleurs dans `controllers/`
- **Nommage** : suffixes `Controller`, `Service`, `Dto`, `Util`
- **Asynchrone** : toutes les requêtes API utilisent `CompletableFuture` + `Platform.runLater`
- **Style** : CSS centralisé dans `main.css`, palette Bleu Roi `#00236f` / Or Ambre

---

## 📜 Licence

Projet développé pour l'**Église des Assemblées de Dieu du Togo — Temple « DIEU NE CHANGE PAS »**.  
Adidogomé, Lomé — Tous droits réservés.

---

<p align="center">
  <sub>Développé avec ☕ Java & 💙 JavaFX</sub>
</p>
