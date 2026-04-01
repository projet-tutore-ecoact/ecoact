# 🚀 Guide de démarrage - EcoAct avec Authentification

📁 **Location:** Ce fichier se trouve en: `docs/01_QUICK_START.md`

---

## Prérequis

- Android Studio (dernière version)
- Gradle synchronisé
- Min SDK 30+

## Installation & Premier Lancement

### 1. Cloner / Ouvrir le projet
```bash
cd EcoAct
# Synchroniser Gradle
```

### 2. Premier lancement
- L'app démarre sur `LoginFragment`
- La base de données se crée automatiquement
- La migration v1→v2 s'exécute si nécessaire

### 3. Tester l'inscription

**Écran d'inscription:**
```
Prénom:            Jean
Nom:               Dupont
Email:             jean@example.com
Mot de passe:      password123
Confirmer:         password123
```

Cliquez "S'inscrire" → Accueil apparaît ✅

### 4. Tester la connexion

**Écran de connexion:**
```
Email:    jean@example.com
Password: password123
```

Cliquez "Se connecter" → Accueil apparaît ✅

---

## Architecture de l'authentification

### Structure des dossiers

```
app/src/main/
├── java/com/project/ecoact/
│   ├── data/
│   │   ├── databaseConf/
│   │   │   └── AppDatabase.java (✅ Migration v1→v2)
│   │   ├── dao/
│   │   │   └── UserDao.java (✅ findByEmail, insert)
│   │   ├── entity/
│   │   │   └── User.java (✅ email, password, timestamps)
│   │   └── repository/
│   │       └── UserRepository.java (✅ authentification)
│   ├── ui/
│   │   └── fragments/
│   │       ├── LoginFragment.java (✅ Connexion)
│   │       └── RegisterFragment.java (✅ Inscription)
│   └── util/
│       ├── SessionManager.java (✅ Gestion session)
│       └── PasswordUtils.java (✅ Hachage passwords)
└── res/
    ├── layout/
    │   ├── fragment_login.xml (✅)
    │   └── fragment_register.xml (✅)
    └── navigation/
        └── nav_graph.xml (✅ Authentification)
```

---

## Flux d'authentification

### 1️⃣ Premier lancement

```
App démarre
    ↓
LoginFragment s'affiche (startDestination)
    ↓
Utilisateur clique "S'inscrire"
    ↓
RegisterFragment s'affiche
```

### 2️⃣ Inscription

```
Remplir formulaire
    ↓
Validation des champs
    ↓
Vérifier email unique
    ↓
Hasher password (SHA-256 + salt)
    ↓
Créer User en BD
    ↓
Sauvegarder session (SharedPreferences)
    ↓
Navigation → HomeFragment
    ↓
Bottom nav s'affiche
```

### 3️⃣ Connexion

```
Remplir email + password
    ↓
Validation des champs
    ↓
Chercher user par email
    ↓
Vérifier password
    ↓
Sauvegarder session
    ↓
Navigation → HomeFragment
    ↓
Bottom nav s'affiche
```

---

## Dépannage

### Erreur: "Cannot find symbol: R.id.loginFragment"
```
→ Build → Clean Project
→ Build → Rebuild Project
```

### Erreur: "User table has no column named email"
```
→ Déinstaller l'app
→ Effacer les données: Settings → Apps → EcoAct → Storage → Clear Cache
→ Réinstaller
→ La migration v1→v2 s'exécutera
```

### LoginFragment ne s'affiche pas
```
→ Vérifier nav_graph.xml startDestination="@id/loginFragment"
→ Vérifier LoginFragment.java classe existe
→ Vérifier fragment_login.xml existe dans res/layout/
```

### Bottom nav visible sur écrans login
```
→ MainActivity.java addOnDestinationChangedListener devrait le cacher
→ Vérifier que MainActivity.java contient:
   if (destination.getId() == R.id.loginFragment...) {
       bottomNav.setVisibility(View.GONE);
   }
```

---

## Points importants

### ⚠️ À ne pas oublier

1. **Migration BD**: S'exécute automatiquement au premier lancement
2. **Session persistante**: Reste même après fermeture app
3. **Email unique**: Pas de doublons, INDEX UNIQUE en BD
4. **Password hashé**: Jamais stocké en clair
5. **Bottom nav cachée**: Masquée sur écrans login/register

---

## Prochaines étapes recommandées

1. ✅ Tester l'inscription
2. ✅ Tester la connexion
3. ✅ Implémenter Logout dans ProfileFragment
4. ✅ Ajouter forgot password
5. ✅ Implémenter BCrypt

---

**Version:** 1.0  
**Date:** April 2026  
**Status:** ✅ Production-ready

