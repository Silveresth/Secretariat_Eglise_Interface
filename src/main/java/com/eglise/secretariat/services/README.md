# ⚙️ Package `services`

> Couche de services métier encapsulant les appels à l'API REST backend et la logique applicative associée.

---

## 📁 Structure

```
services/
├── AuthService.java            # Authentification & profil
├── DashboardService.java       # Statistiques du tableau de bord
├── DocumentService.java        # Génération & gestion des PDF
├── FideleService.java          # CRUD complet des fidèles
├── MouvementService.java       # OCR, mouvements & conformité
└── api/
    ├── ApiClient.java          # Client HTTP générique (singleton)
    └── ApiException.java       # Exception métier API
```

---

## 📄 Services Métier

### `AuthService`

Gestion de l'authentification et du profil utilisateur.

| Méthode | Endpoint | Description |
|---|---|---|
| `login(username, password)` | `POST /api/auth/login` | Authentifie l'utilisateur, stocke le token JWT dans `SessionManager` |
| `updateProfile(currentPwd, newUsername, newPwd)` | `PUT /api/auth/profile` | Met à jour le profil et synchronise le `SessionManager` |
| `logout()` | — | Efface la session locale |

---

### `DashboardService`

| Méthode | Endpoint | Description |
|---|---|---|
| `getStats()` | `GET /api/dashboard/stats` | Récupère les statistiques globales (`DashboardStatsDto`) |

---

### `FideleService`

CRUD complet des fidèles avec recherche paginée.

| Méthode | Endpoint | Description |
|---|---|---|
| `searchFideles(query, quartier, baptise, actif, page, size, sort)` | `GET /api/fideles` | Recherche paginée multi-critères |
| `getFideleById(id)` | `GET /api/fideles/{id}` | Détail d'un fidèle |
| `createFidele(dto)` | `POST /api/fideles` | Création d'un fidèle |
| `updateFidele(id, dto)` | `PUT /api/fideles/{id}` | Mise à jour d'un fidèle |
| `deleteFidele(id)` | `DELETE /api/fideles/{id}` | Suppression d'un fidèle |

> La méthode `searchFideles` construit dynamiquement les query params en ignorant les valeurs nulles ou vides.

---

### `DocumentService`

Génération de documents PDF et gestion des fichiers.

| Méthode | Endpoint / Action | Description |
|---|---|---|
| `getFidelePdf(id)` | `GET /api/documents/fidele/{id}/pdf` | Télécharge la fiche d'inscription PDF (bytes) |
| `generateLettreRecommandationPdf(request)` | `POST /api/documents/lettre-recommandation/pdf` | Génère une lettre de recommandation PDF |
| `savePdfToDownloads(pdfBytes, prefix)` | Fichier local | Sauvegarde dans `~/Downloads` avec nom nettoyé |
| `buildDocumentFilename(fidele, suffix)` | — | Construit un nom de fichier propre à partir du fidèle |
| `openPdf(file)` | `Desktop.open()` | Ouvre le PDF dans le lecteur par défaut du système |

---

### `MouvementService`

Gestion des mouvements de fidèles et de la conformité.

| Méthode | Endpoint | Description |
|---|---|---|
| `scanLetterOcr(file)` | `POST /api/mouvements/ocr-scan` | Upload multipart d'un document pour extraction OCR |
| `saveFideleEntrant(dto)` | `POST /api/mouvements/fidele-entrant` | Enregistre un fidèle entrant avec sa lettre |
| `updateCarteMembreStatus(fideleId, valide)` | `PATCH /api/mouvements/carte-membre/{id}` | Met à jour le statut de la carte de membre |
| `updateCarnetDimeStatus(fideleId, valide)` | `PATCH /api/mouvements/carnet-dime/{id}` | Met à jour le statut du carnet de dîme |

---

## 🌐 Sous-package `api/`

### `ApiClient` *(Singleton)*

Client HTTP générique basé sur `java.net.http.HttpClient` (HTTP/2). Point central pour **toutes** les communications réseau.

#### Caractéristiques

- **Singleton thread-safe** avec `HttpClient` et `ObjectMapper` (Jackson) partagés
- **Authentification automatique** : injection du header `Authorization: Bearer <token>` depuis `SessionManager`
- **Timeouts configurables** via `AppConfig`
- **Toutes les requêtes sont asynchrones** (`CompletableFuture`)

#### Méthodes HTTP

| Méthode | Signature | Usage |
|---|---|---|
| `getAsync()` | `<T> CompletableFuture<T>` | GET avec query params, désérialisation JSON |
| `postAsync()` | `<T> CompletableFuture<T>` | POST JSON body |
| `putAsync()` | `<T> CompletableFuture<T>` | PUT JSON body |
| `patchAsync()` | `<T> CompletableFuture<T>` | PATCH avec query params |
| `deleteAsync()` | `CompletableFuture<Void>` | DELETE |
| `getBytesAsync()` | `CompletableFuture<byte[]>` | GET binaire (téléchargement PDF) |
| `postForBytesAsync()` | `CompletableFuture<byte[]>` | POST JSON → réponse binaire (génération PDF) |
| `postMultipartAsync()` | `<T> CompletableFuture<T>` | Upload multipart (OCR) |

#### Gestion des erreurs

- Validation automatique du code HTTP (2xx = succès)
- Extraction intelligente du message d'erreur depuis le body JSON (`errors`, `message`)
- Messages user-friendly par code HTTP (401, 403, 404, 500+)

---

### `ApiException`

Exception personnalisée pour les erreurs API.

| Champ | Type | Description |
|---|---|---|
| `statusCode` | `int` | Code HTTP de la réponse |
| `errorBody` | `String` | Corps brut de la réponse d'erreur |

| Méthode utilitaire | Description |
|---|---|
| `isAuthError()` | `true` si 401 ou 403 |
| `isNotFound()` | `true` si 404 |
| `isServerError()` | `true` si ≥ 500 |

---

## 🔗 Dépendances

| Package | Usage |
|---|---|
| `config` | `AppConfig` (URL, timeouts), `SessionManager` (token JWT) |
| `dto` | Tous les DTO pour sérialisation/désérialisation |
| Jackson | `ObjectMapper`, `TypeReference`, `JavaTimeModule` |
| `java.net.http` | `HttpClient`, `HttpRequest`, `HttpResponse` |

---

← [Retour au README principal](../../../../../../../../README.md)
