# 🔧 Package `config`

> Configuration globale de l'application et gestion de la session utilisateur.

---

## 📁 Structure

```
config/
├── AppConfig.java          # Constantes et paramètres globaux
└── SessionManager.java     # Singleton de gestion de session JWT
```

---

## 📄 Classes

### `AppConfig`

Classe de configuration statique centralisant toutes les constantes de l'application.

| Constante / Champ | Type | Description |
|---|---|---|
| `APP_TITLE` | `String` | Titre de l'application affiché dans les fenêtres |
| `APP_VERSION` | `String` | Version courante (`1.0.0`) |
| `apiBaseUrl` | `String` | URL de base de l'API backend (défaut : `http://localhost:8081`) |
| `CONNECT_TIMEOUT_SECONDS` | `int` | Timeout de connexion HTTP (10s) |
| `REQUEST_TIMEOUT_SECONDS` | `int` | Timeout des requêtes HTTP (30s) |
| `churchName` | `String` | Nom officiel de l'église |
| `templeName` | `String` | Nom du temple |
| `churchAddress` | `String` | Adresse postale |
| `churchPhone` | `String` | Numéro de téléphone |
| `churchEmail` | `String` | Email de contact |
| `churchRegNumber` | `String` | Numéro d'enregistrement |

> Tous les champs « église » sont modifiables via setters statiques pour permettre une configuration dynamique.

---

### `SessionManager`

**Singleton thread-safe** gérant l'état d'authentification de l'utilisateur connecté.

#### Responsabilités

- **Stockage du token JWT** obtenu après authentification
- **Informations utilisateur** : username, rôle, email
- **Pattern Observer** : notification des `AuthStateListener` lors des changements d'état (connexion/déconnexion)

#### Interface interne

```java
public interface AuthStateListener {
    void onAuthStateChanged(boolean isAuthenticated);
}
```

#### Méthodes principales

| Méthode | Description |
|---|---|
| `getInstance()` | Accès au singleton (synchronisé) |
| `setSession(token, username, role, email)` | Enregistre les données de session et notifie les listeners |
| `clearSession()` | Efface la session (déconnexion) et notifie les listeners |
| `isAuthenticated()` | Vérifie si un token valide est présent |
| `addListener(listener)` / `removeListener(listener)` | Gestion des observateurs d'état |

#### Flux d'authentification

```
Login → AuthService.login() → SessionManager.setSession() → notifyListeners(true)
                                                                    ↓
Logout → AuthService.logout() → SessionManager.clearSession() → notifyListeners(false)
```

---

## 🔗 Dépendances

- Aucune dépendance externe — ce package est consommé par `services`, `controllers` et `utils`.

---

← [Retour au README principal](../../../../../../../../README.md)
