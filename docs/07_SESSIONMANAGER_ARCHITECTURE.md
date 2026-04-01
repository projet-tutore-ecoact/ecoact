# 🏗️ Architecture SessionManager - Explication et amélioration

## 🎯 Votre question

**"Pourquoi utiliser des constantes statiques alors qu'on a une table User?"**

C'est une excellente question architecturale!

---

## ❌ Le problème avec l'approche actuelle

### Actuel:
```java
public static final String KEY_USER_ID = "user_id";      // ← Clé de stockage
public static final String KEY_USER_EMAIL = "user_email"; // ← Clé de stockage
public static final String KEY_USER_NAME = "user_name";   // ← Clé de stockage

// Ces constantes représentent juste les clés SharedPreferences
// Pas les valeurs réelles!
```

### Le vrai problème:
```
❌ On duplique les données:
   - Table User en BD (source de vérité)
   - SharedPreferences (copie locale)

❌ Risque de désynchronisation:
   - L'utilisateur change ses infos en BD
   - SharedPreferences garde les anciennes infos

❌ Pas évolutif:
   - Si on ajoute des champs à User, il faut modifier SessionManager
```

---

## ✅ Meilleure approche: Stocker que l'USER_ID

### Concept:
```
Stocker SEULEMENT l'ID de l'utilisateur connecté en SharedPreferences.
Récupérer le reste des infos de la BD quand nécessaire.
```

### Avantages:
- ✅ Une seule source de vérité (la BD)
- ✅ Synchronisé automatiquement
- ✅ Plus simple et plus maintenable
- ✅ Plus sûr (pas de duplication)

---

## 🔧 SessionManager Amélioré

### Code:
```java
public class SessionManager {
    private static final String PREF_NAME = "ecoact_session";
    private static final String KEY_USER_ID = "user_id";  // ← SEULE clé!
    
    private SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Sauvegarder l'ID de l'utilisateur connecté
     */
    public void saveUserSession(Long userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Récupérer l'ID de l'utilisateur connecté
     */
    public Long getCurrentUserId() {
        long id = sharedPreferences.getLong(KEY_USER_ID, -1);
        return id == -1 ? null : id;
    }

    /**
     * Vérifier si un utilisateur est connecté
     */
    public boolean isUserLoggedIn() {
        return sharedPreferences.getLong(KEY_USER_ID, -1) != -1;
    }

    /**
     * Déconnexion
     */
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
```

---

## 🔄 Comment l'utiliser

### Dans LoginFragment:
```java
if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
    // ✅ Sauvegarder SEULEMENT l'ID
    sessionManager.saveUserSession(user.getId());
    navigate(HomeFragment);
}
```

### Dans ProfileFragment:
```java
// Récupérer l'ID
Long userId = sessionManager.getCurrentUserId();

// Charger l'utilisateur DEPUIS LA BD
User user = userRepository.getUserById(userId);

// Afficher les infos fraiches de la BD
tvUserName.setText(user.getFirstName() + " " + user.getLastName());
tvUserEmail.setText(user.getEmail());
```

### Dans MainActivity:
```java
if (sessionManager.isUserLoggedIn()) {
    // L'utilisateur est connecté
    navController.navigate(R.id.homeFragment);
} else {
    // Pas de session
    navController.navigate(R.id.loginFragment);
}
```

---

## 📊 Comparaison: Avant vs Après

### ❌ AVANT (Approche actuelle)
```
SharedPreferences stocke:
├─ user_id: 1
├─ user_email: "jean@test.com"
└─ user_name: "Jean Dupont"

Problème:
❌ Duplication (même info en BD et SharedPreferences)
❌ Peut se désynchroniser
❌ Plus lourd
```

### ✅ APRÈS (Approche améliorée)
```
SharedPreferences stocke:
└─ user_id: 1

À chaque besoin:
1. Récupérer user_id de SharedPreferences
2. Charger User depuis la BD
3. Utiliser les infos fraîches

Avantage:
✅ Une source de vérité (la BD)
✅ Toujours synchronisé
✅ Plus léger
✅ Plus maintenable
```

---

## 🔐 Sécurité

### SharedPreferences contient:
```
✅ user_id (entier)
❌ PAS email
❌ PAS password
❌ PAS infos personnelles
```

### Raison:
- On n'a besoin QUE de l'ID pour identifier la session
- Tout le reste vient de la BD
- Plus sûr et plus simple

---

## 🧵 Scénario réel

### Exemple:
```
1. Jean se connecte
   sessionManager.saveUserSession(1)  // Sauvegarde ID 1
   
2. Jean ferme l'app
   
3. Jean rouvre l'app
   sessionManager.getCurrentUserId()  // Récupère ID 1
   
4. On récupère User(1) depuis la BD
   User user = userRepository.getUserById(1)
   
5. Les infos sont fraîches (si Jean a changé son email, on la voit)
```

---

## 🎯 Résumé

### Ce qu'il FAUT comprendre:

```
SharedPreferences = Simple carnet local avec JUSTE l'ID
Table User = Source de vérité en BD avec TOUTES les infos

Workflow:
1. Utilisateur se connecte
2. SessionManager.saveUserSession(userId)  ← Juste l'ID!
3. Quand besoin des infos: UserRepository.getUserById(userId)  ← De la BD
```

### Avantages:
- ✅ Pas de duplication
- ✅ Toujours synchronisé
- ✅ Plus simple
- ✅ Plus maintenable
- ✅ Plus sûr

---

**Cette approche est la meilleure pratique en mobile!**

