# 🗂️ Package `models`

> Modèles de domaine et énumérations métier utilisés par les DTO et les contrôleurs.

---

## 📁 Structure

```
models/
└── enums/
    ├── FrequenceDime.java     # Fréquence de paiement de la dîme
    ├── Sexe.java              # Genre du fidèle
    └── Statut.java            # Situation matrimoniale
```

---

## 📄 Énumérations

### `Sexe`

Genre du fidèle, avec désérialisation intelligente via `@JsonCreator`.

| Valeur | Label affiché |
|---|---|
| `MASCULIN` | Masculin |
| `FEMININ` | Féminin |

**Désérialisation flexible** — La méthode `fromString()` accepte :
- `"M"` ou `"MASC..."` → `MASCULIN`
- `"F"` ou `"FEM..."` → `FEMININ`
- Nom exact de l'enum en fallback

---

### `Statut`

Situation matrimoniale du fidèle.

| Valeur | Label affiché |
|---|---|
| `CELIBATAIRE` | Célibataire |
| `MARIE` | Marié(e) |
| `VEUF` | Veuf/Veuve |
| `DIVORCE` | Divorcé(e) |

---

### `FrequenceDime`

Fréquence à laquelle un fidèle s'acquitte de sa dîme.

| Valeur | Label affiché |
|---|---|
| `REGULIEREMENT` | Régulièrement |
| `OCCASIONNELLEMENT` | Occasionnellement |
| `RAREMENT` | Rarement |
| `JAMAIS` | Jamais |

---

## 🧩 Pattern commun

Chaque enum suit le même pattern :
- Un champ `label` avec le texte francisé à afficher dans l'interface
- Un constructeur prenant le label
- `getLabel()` pour un accès programmatique
- `toString()` retournant le label (utilisé automatiquement par les `ComboBox` JavaFX)

```java
public enum MonEnum {
    VALEUR("Label affiché");

    private final String label;
    MonEnum(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
```

---

## 🔗 Utilisations

| Enum | Utilisé dans |
|---|---|
| `Sexe` | `FideleDto.sexe`, Wizard étape 1, Fiche détaillée |
| `Statut` | `FideleDto.statutMatrimonial`, Wizard étape 3 |
| `FrequenceDime` | `FideleDto.frequenceDime`, Wizard étape 4, Dashboard stats |

---

← [Retour au README principal](../../../../../../../../README.md)
