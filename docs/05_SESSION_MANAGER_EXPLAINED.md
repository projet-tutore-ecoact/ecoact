# 📱 SessionManager - À quoi ça sert?

## TL;DR (Réponse courte)

**SessionManager** gère la session utilisateur en **mémoire persistent** (SharedPreferences).

Elle permet de:
- ✅ Sauvegarder qui est connecté
- ✅ Vérifier si l'utilisateur est connecté
- ✅ Récupérer les infos de l'utilisateur
- ✅ Gérer la déconnexion

---

## 🎯 Rôle de SessionManager

### Qu'est-ce que c'est une "session"?

Une **session** = "L'utilisateur est connecté"

Sans session: À chaque ouverture de l'app, elle demande login  
Avec session: L'utilisateur reste connecté même après fermer l'app

### Comment ça marche?

```
1. Utilisateur se connecte
   ↓
2. SessionManager sauvegarde les infos (userId, email, nom)
   dans SharedPreferences (stockage local de l'app)
   ↓
3. À chaque ouverture de l'app:
   - SessionManager vérifie si isUserLoggedIn()
   - Si oui → aller à Home
   - Si non → aller à Login
```

---

## 💾 SharedPreferences - Stockage persistant

### Qu'est-ce que c'est?

SharedPreferences = petite base de données locale très simple

```
Stocke:
- Petites données (strings, longs, booleans)
- De manière persistante (survit à la fermeture de l'app)
- Pas de password (données non chiffrées - donc ne pas stocker le password!)

Exemple d'utilisation:
- Paramètres utilisateur
- Infos de session
- Préférences d'app
```

### Format de stockage

```
clé: "user_id"          → valeur: 123
clé: "user_email"       → valeur: "jean@test.com"
clé: "user_name"        → valeur: "Jean Dupont"
```

---

## 📋 Méthodes de SessionManager

### 1. `saveUserSession(Long id, String email, String firstName, String lastName)`

**Utilité:** Sauvegarder après login/registration

```java
// Après login réussi
sessionManager.saveUserSession(
    user.getId(),               // 1, 2, 3, etc.
    user.getEmail(),            // "jean@test.com"
    user.getFirstName(),        // "Jean"
    user.getLastName()          // "Dupont"
);
```

**Stocke:**
```
user_id → 1
user_email → "jean@test.com"
user_name → "Jean Dupont"
```

### 2. `isUserLoggedIn()`

**Utilité:** Vérifier si l'utilisateur est connecté

```java
if (sessionManager.isUserLoggedIn()) {
    // Utilisateur connecté → aller à Home
    navigateToHome();
} else {
    // Utilisateur non connecté → rester sur Login
    showLoginScreen();
}
```

### 3. `getCurrentUserId()`

**Utilité:** Récupérer l'ID de l'utilisateur

```java
Long userId = sessionManager.getCurrentUserId();
// Utiliser pour requêtes BD spécifiques à cet utilisateur
```

### 4. `getCurrentUserEmail()`

**Utilité:** Récupérer l'email

```java
String email = sessionManager.getCurrentUserEmail();
// Afficher dans le profil, envoyer email, etc.
```

### 5. `getCurrentUserName()`

**Utilité:** Récupérer le nom complet

```java
String name = sessionManager.getCurrentUserName();
// Afficher "Bienvenue Jean Dupont"
```

### 6. `logout()`

**Utilité:** Déconnecter l'utilisateur

```java
sessionManager.logout();  // Efface toutes les données
// Rediriger vers Login
```

---

## 🔄 Flux complet avec SessionManager

### Premier lancement (aucune session)

```
1. App démarre
2. MainActivity vérifie isUserLoggedIn()
3. Résultat: false (première fois)
4. Affiche LoginFragment
5. Utilisateur se connecte
6. sessionManager.saveUserSession(...)
7. NavigateToHome()
```

### Deuxième lancement (session existe)

```
1. App démarre
2. MainActivity vérifie isUserLoggedIn()
3. Résultat: true (les données existent)
4. Affiche directement HomeFragment
5. Utilisateur NE voit pas l'écran login
```

### Déconnexion

```
1. Utilisateur clique "Logout"
2. sessionManager.logout()
3. NavigateToLogin()
4. À la prochaine ouverture, isUserLoggedIn() = false
5. Affiche Login
```

---

## 💡 Exemple d'intégration complète

### Dans MainActivity.java

```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SessionManager sessionManager = new SessionManager(this);
        NavHostFragment navHostFragment = ...
        
        // Vérifier si l'utilisateur est déjà connecté
        if (sessionManager.isUserLoggedIn()) {
            // L'utilisateur est connecté → aller à Home
            navController.navigate(R.id.homeFragment);
        } else {
            // L'utilisateur n'est pas connecté → aller à Login
            navController.navigate(R.id.loginFragment);
        }
    }
}
```

### Dans LoginFragment.java

```java
public class LoginFragment extends Fragment {
    
    private SessionManager sessionManager;
    private UserRepository userRepository;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionManager = new SessionManager(requireContext());
        userRepository = new UserRepository(requireActivity().getApplication());
        
        // Si déjà connecté, aller à Home
        if (sessionManager.isUserLoggedIn()) {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment);
            return;
        }
        
        btnLogin.setOnClickListener(v -> login());
    }
    
    private void login() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        
        // Vérifier dans la BD
        User user = userRepository.getUserByEmail(email);
        
        if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
            // ✅ Login réussi
            // Sauvegarder la session
            sessionManager.saveUserSession(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
            );
            
            // Naviguer vers Home
            Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_homeFragment);
        } else {
            // ❌ Login échoué
            tvErrorMessage.setText("Email ou mot de passe incorrect");
        }
    }
}
```

### Dans RegisterFragment.java

```java
private void register() {
    // ... validation et création du user ...
    
    // Créer l'utilisateur en BD
    User newUser = new User(email, hashedPassword, firstName, lastName);
    long userId = userRepository.insertUser(newUser);
    
    if (userId > 0) {
        // ✅ Inscription réussie
        // Sauvegarder la session automatiquement
        sessionManager.saveUserSession(
            userId,
            email,
            firstName,
            lastName
        );
        
        // Naviguer vers Home
        Navigation.findNavController(getView()).navigate(R.id.action_registerFragment_to_homeFragment);
    }
}
```

### Dans ProfileFragment.java (Futur)

```java
public class ProfileFragment extends Fragment {
    
    private SessionManager sessionManager;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionManager = new SessionManager(requireContext());
        
        // Afficher les infos de l'utilisateur
        String userName = sessionManager.getCurrentUserName();
        tvUserName.setText("Bienvenue " + userName);
        
        // Bouton Logout
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();  // Effacer la session
            // Naviguer vers Login
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_loginFragment);
        });
    }
}
```

---

## 🔐 Sécurité

### ✅ À stocker en SharedPreferences

```
- User ID
- Email
- Nom
- Préférences d'app
```

### ❌ À NE PAS stocker

```
- ❌ Password (jamais!)
- ❌ Token sensible
- ❌ Données personnelles sensibles
```

---

## 📊 Comparaison: Avec vs Sans SessionManager

### ❌ SANS SessionManager

```
Chaque ouverture:
1. App démarre
2. Affiche Login
3. Utilisateur doit taper email + password
4. À chaque fois!
```

### ✅ AVEC SessionManager

```
Première ouverture:
1. App démarre
2. Affiche Login (session vide)
3. Utilisateur se connecte
4. Session sauvegardée

Deuxième ouverture:
1. App démarre
2. Affiche Home directement
3. Utilisateur déjà connecté
4. Pas besoin de taper password!
```

---

## 🎯 Résumé

| Aspect | Explication |
|--------|------------|
| **Qu'est-ce?** | Gestionnaire de session utilisateur |
| **Où stocke?** | SharedPreferences (BD locale) |
| **Quoi stocke?** | userId, email, nom |
| **Utilisé par?** | LoginFragment, RegisterFragment, ProfileFragment, MainActivity |
| **Quand utiliser?** | Après login/register + avant navigation |
| **Utilité principale** | Garder l'utilisateur connecté entre sessions |

---

**Date:** April 2026  
**Importance:** ⭐⭐⭐ (Concept fondamental!)

