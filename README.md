# 🏛️ Secretariat Eglise Interface — Ecclesia Admin Desktop

Application de bureau moderne développée en **JavaFX 21** et **Java 17** pour la gestion administrative intégrale du secrétariat de l'**Église des Assemblées de Dieu du Togo — Temple « DIEU NE CHANGE PAS »** (Adidogomé, Lomé).

L'application communique via des API REST sécurisées avec le backend Spring Boot (`secretariat_api`).

---

## ✨ Fonctionnalités Principales

### 1. 📊 Tableau de Bord Analytique
- **Statistiques en temps réel** : total des membres, taux de fidèles baptisés d'eau, proportion de membres actifs et statut de conformité administrative.
- **Dernières intégrations** : tableau récapitulatif des fidèles récemment inscrits avec accès direct aux fiches.
- **Actions rapides** : raccourcis vers l'enregistrement, l'OCR, la génération de documents et la recherche.

### 2. 👥 Répertoire & Gestion des Fidèles
- **Recherche avancée multi-critères** : filtrage par nom, prénom, quartier, statut de baptême, régularité et activité.
- **Wizard d'Inscription en 4 Étapes** :
  1. *Identité & Profession* : nom, prénoms, sexe, date et lieu de naissance, ethnie, profession, niveau d'étude.
  2. *Coordonnées & Filiation* : contacts (Togocel, Moov, Email), quartier, adresse, noms des parents.
  3. *Famille & Situation Matrimoniale* : statut matrimonial, date/lieu/pasteur de mariage, conjoint, nombre d'enfants.
  4. *Parcours Spirituel & Conformité* : dates et lieux de baptêmes (eau & Saint-Esprit), ancienne/nouvelle dénomination, carte de membre, carnet de dîme.
- **Fiche Détaillée & Édition** : consultation complète avec badges de statut et modification immédiate.

### 3. 🔍 Mouvements & Scanner Laser OCR
- **Aperçu et Reconnaissance Haute Fidélité** : glisser-déposer de lettres de recommandation scannées (PDF, JPG, PNG).
- **Animation Scanner Laser** : faisceau laser interactif centré balayant le document pendant le traitement optique.
- **Extraction Optique & Pré-remplissage Automatique** :
  - Nom et Prénoms (séparés et formatés).
  - Date et Lieu de naissance.
  - Dates de baptêmes (avec correction automatique des artefacts OCR).
  - Profession, Sexe, Situation matrimoniale et nombre d'enfants.
  - Pasteur signataire, Église de provenance et motif de transfert.
- **Intégration en 1 clic** : enregistrement direct du fidèle arrivant avec lettre validée.

### 4. 📄 Documents Officiels & Édition PDF
- **Lettre de Recommandation Officielle (Sortante)** :
  - Sélection du fidèle, choix du motif (*Transfert*, *Voyage*, *Emploi*), saisie de l'église de destination et du pasteur signataire.
  - Aperçu en direct et génération du PDF officiel conforme aux normes AD.
- **Fiche d'Inscription Individuelle Préliminaire** :
  - Génération et téléchargement de la fiche administrative complète conforme au modèle de l'église.

### 5. 🔔 Notifications Natives Épurées
- Système de notifications popups/toasts élégantes et non intrusives ancrées à l'interface, avec animations fluides d'entrée/sortie.

---

## 🛠️ Stack Technique

- **Langage** : Java 17 (LTS)
- **Framework UI** : JavaFX 21 (`javafx-controls`, `javafx-fxml`, `javafx-swing`)
- **Rendu PDF** : Apache PDFBox 3.0.2
- **Client HTTP** : Java 11+ `HttpClient` natif asynchrone (HTTP/2) + Jackson (`jackson-datatype-jsr310`)
- **Style & Design** : CSS3 moderne, palette officielle *Bleu Roi AD / Or Ambre*, composants réactifs.
- **Build & Packaging** : Maven 3.9+ (`javafx-maven-plugin`)

---

## 🚀 Démarrage Rapide

### Prérequis
- **JDK 17** ou supérieur installé
- Le backend **`secretariat_api`** démarré sur `http://localhost:8081`

### Lancement de l'Application

```powershell
# Cloner le dépôt
git clone https://github.com/Silveresth/Secretariat_Eglise_Interface.git
cd Secretariat_Eglise_Interface

# Lancer l'application
.\mvnw.cmd javafx:run
```

### Identifiants de Connexion par Défaut
- **Nom d'utilisateur** : `secretaire`
- **Mot de passe** : `secretaire123`

---

## 📁 Architecture du Projet

```
C:\Secretariat_Eglise_Interface\
├── src/main/java/com/eglise/secretariat/
│   ├── config/              # Configuration globale & Session utilisateur
│   ├── controllers/         # Contrôleurs MVC (Auth, Dashboard, Fidèles, Mouvements OCR, Documents)
│   ├── dto/                 # Objets de transfert de données (DTOs)
│   ├── models/enums/        # Énumérations (Sexe, Statut, FrequenceDime, etc.)
│   ├── services/            # Services métier & Client API HTTP (ApiClient)
│   └── utils/               # Navigation, Notifications toasts, DateUtil
└── src/main/resources/com/eglise/secretariat/
    ├── css/                 # Feuilles de style modernes (main.css)
    ├── fxml/                # Vues FXML hiérarchisées par module
    └── images/              # Logos officiels Assemblées de Dieu
```

---

## 📜 Licence
Projet développé pour l'Église des Assemblées de Dieu du Togo — Temple « DIEU NE CHANGE PAS ». Tous droits réservés.
