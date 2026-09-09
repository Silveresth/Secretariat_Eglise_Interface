# 📦 Package `dto`

> Data Transfer Objects (DTO) utilisés pour la sérialisation/désérialisation JSON entre le client JavaFX et l'API REST backend.

---

## 📁 Structure

```
dto/
├── AuthResponseDto.java
├── ConformiteStatusDto.java
├── DashboardStatsDto.java
├── EngagementDto.java
├── FideleDto.java
├── LettreRecommandationRequestDto.java
├── LoginRequestDto.java
├── OcrResultDto.java
├── PageResponseDto.java
└── UpdateProfileDto.java
```

---

## 📄 Classes

### 🔐 Authentification

#### `LoginRequestDto`

DTO de requête pour l'authentification.

| Champ | Type | Description |
|---|---|---|
| `username` | `String` | Nom d'utilisateur ou email |
| `password` | `String` | Mot de passe |

#### `AuthResponseDto`

DTO de réponse après authentification réussie.

| Champ | Type | Description |
|---|---|---|
| `token` | `String` | Token JWT |
| `tokenType` | `String` | Type de token (défaut : `Bearer`) |
| `expiresIn` | `long` | Durée de validité en secondes |
| `role` | `String` | Rôle de l'utilisateur (ex : `ADMIN`) |

#### `UpdateProfileDto`

DTO de requête pour la mise à jour du profil.

| Champ | Type | Description |
|---|---|---|
| `currentPassword` | `String` | Mot de passe actuel (vérification) |
| `newUsername` | `String` | Nouveau nom d'utilisateur (optionnel) |
| `newPassword` | `String` | Nouveau mot de passe (optionnel) |

---

### 👤 Fidèle

#### `FideleDto`

DTO principal représentant un fidèle de l'église. **Classe la plus riche** du projet avec ~40 champs organisés en sections :

| Section | Champs principaux |
|---|---|
| **Identité** | `id`, `nom`, `prenoms`, `sexe` (`Sexe`), `dateNaissance`, `lieuNaissance`, `ethnie`, `profession`, `niveauEtude` |
| **Contact & Adresse** | `telephone`, `contactMoov`, `email`, `quartier`, `adresse`, `prefectureRegion` |
| **Situation Matrimoniale** | `statutMatrimonial` (`Statut`), `dateMariage`, `egliseMariage`, `pasteurMariage`, `nomConjoint`, `confessionFoiConjoint`, `nombreGarcons`, `nombreFilles` |
| **Filiation** | `nomPere`, `prenomPere`, `nomMere`, `prenomMere` |
| **Parcours Spirituel** | `dateConversion`, `egliseConversion`, `baptise`, `dateBapteme`, `lieuBapteme`, `pasteurBapteme`, `dateBaptemeEsprit`, `lieuBaptemeEsprit`, `ancienneDenomination`, `nouvelleDenomination` |
| **Lettre & Intégration** | `lettreRecommandationPresentee`, `dateLettreRecommandation`, `pasteurLettreRecommandation`, `egliseLettreRecommandation`, `dateIntegrationAdidogome` |
| **Vie de Membre** | `carteMembreValide`, `regulierReunions`, `carnetDimeValide`, `payeDimes`, `frequenceDime` (`FrequenceDime`) |
| **État** | `actif`, `engagements` (`List<EngagementDto>`) |

---

### 🤝 Engagement

#### `EngagementDto`

Représente un engagement/ministère d'un fidèle au sein de l'église.

| Champ | Type | Description |
|---|---|---|
| `id` | `Long` | Identifiant |
| `type` | `String` | Type : `DEPARTEMENT`, `MINISTERE`, `GROUPE_VIE` |
| `nom` | `String` | Nom du département/ministère |
| `role` | `String` | Rôle : `RESPONSABLE`, `ADJOINT`, `MEMBRE_ACTIF` |
| `dateDebut` | `LocalDate` | Date de début |
| `dateFin` | `LocalDate` | Date de fin (nullable si toujours actif) |
| `actif` | `boolean` | Engagement en cours |

---

### 📊 Tableau de Bord

#### `DashboardStatsDto`

Statistiques globales affichées sur le tableau de bord.

| Champ | Type | Description |
|---|---|---|
| `totalInscrits` | `long` | Nombre total de fidèles inscrits |
| `repartitionParQuartier` | `Map<String, Long>` | Nombre de fidèles par quartier |
| `tauxMembresAJourCotisationDime` | `double` | Pourcentage de conformité dîme |
| `fluxMensuels` | `Map<String, Long>` | Entrées/sorties par mois |

---

### 📄 Documents

#### `LettreRecommandationRequestDto`

DTO de requête pour la génération d'une lettre de recommandation PDF.

| Champ | Type | Description |
|---|---|---|
| `fideleId` | `Long` | ID du fidèle concerné |
| `motif` | `String` | Motif du transfert |
| `egliseDestination` | `String` | Église de destination |
| `pasteurSignataire` | `String` | Pasteur qui signe la lettre |
| `dateDepart` | `LocalDate` | Date de départ (défaut : aujourd'hui) |

---

### 🔍 OCR

#### `OcrResultDto`

Résultat de l'extraction OCR d'une lettre de recommandation scannée.

| Champ | Type | Description |
|---|---|---|
| `pasteurSignataire` | `String` | Nom du pasteur signataire extrait |
| `egliseOrigine` | `String` | Église d'origine extraite |
| `dateLettre` | `LocalDate` | Date de la lettre extraite |
| `rawText` | `String` | Texte brut complet extrait par OCR |

---

### ✅ Conformité

#### `ConformiteStatusDto`

Réponse de mise à jour du statut de conformité d'un fidèle.

| Champ | Type | Description |
|---|---|---|
| `fideleId` | `Long` | ID du fidèle |
| `carteMembreValide` | `Boolean` | Statut de la carte de membre |
| `carnetDimeValide` | `Boolean` | Statut du carnet de dîme |
| `message` | `String` | Message de confirmation |

---

### 📃 Pagination

#### `PageResponseDto<T>`

DTO générique encapsulant une réponse paginée Spring Boot (`Page<T>`).

| Champ | Type | Description |
|---|---|---|
| `content` | `List<T>` | Éléments de la page courante |
| `totalPages` | `int` | Nombre total de pages |
| `totalElements` | `long` | Nombre total d'éléments |
| `size` | `int` | Taille de la page |
| `number` | `int` | Numéro de la page courante (0-indexed) |
| `first` / `last` | `boolean` | Première/dernière page |
| `empty` | `boolean` | Page vide |

> Annotée `@JsonIgnoreProperties(ignoreUnknown = true)` pour une compatibilité souple avec le backend.

---

## 🔗 Dépendances

| Dépendance | Usage |
|---|---|
| `models.enums` | `Sexe`, `Statut`, `FrequenceDime` dans `FideleDto` |
| Jackson | Sérialisation/désérialisation JSON (`@JsonCreator`, `@JsonIgnoreProperties`) |
| `java.time` | `LocalDate` pour toutes les dates |

---

← [Retour au README principal](../../../../../../../../README.md)
