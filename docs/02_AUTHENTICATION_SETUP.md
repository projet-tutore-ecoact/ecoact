# 02 - Documentation de l'Authentification

📁 **Location:** `docs/02_AUTHENTICATION_SETUP.md`

---

## Vue d'ensemble

L'authentification a été implémentée avec:
- ✅ Login et Registration avec email/password
- ✅ Hachage sécurisé des mots de passe (SHA-256 avec salt)
- ✅ Gestion de session utilisateur
- ✅ Interface intuitive suivant le design existant
- ✅ Navigation automatique après authentification
- ✅ Validation complète des entrées

---

## Architecture

### 1. Entité User (Mise à jour)
**Fichier:** `data/entity/User.java`

Champs:
- `id` - Clé primaire
- `email` - Email unique
- `password` - Mot de passe hashé
- `firstName` - Prénom
- `lastName` - Nom
- `createdAt` - Timestamp création
- `updatedAt` - Timestamp modification

### 2. Migration de base de données
**Fichier:** `data/databaseConf/AppDatabase.java`

Migration v1→v2:
- Ajoute les 4 colonnes (email, password, timestamps)
- Crée un index UNIQUE sur email
- Exécutée automatiquement lors du premier lancement

### 3. UserRepository (Mise à jour)
**Fichier:** `data/repository/UserRepository.java`

Méthodes disponibles:
- `getUserByEmail(String email)` - Trouve un utilisateur par email
- `insertUser(User user)` - Insère un utilisateur (thread-safe)
- `update(User user)` - Met à jour un utilisateur

### 4. SessionManager
**Fichier:** `util/SessionManager.java`

Gère la session utilisateur via SharedPreferences:
- `saveUserSession(...)` - Sauvegarde la session après login
- `getCurrentUserId()` - Récupère l'ID utilisateur
- `isUserLoggedIn()` - Vérifie si l'utilisateur est connecté
- `logout()` - Efface la session

### 5. PasswordUtils
**Fichier:** `util/PasswordUtils.java`

Utilitaires de sécurité:
- `encodePassword(String password)` - Génère salt + hash
- `verifyPassword(String password, String stored)` - Vérifie le mot de passe
- `isValidEmail(String email)` - Valide le format email
- `isValidPassword(String password)` - Valide la complexité

### 6. Fragments d'Authentification

#### LoginFragment
- Fichier: `ui/fragments/LoginFragment.java`
- Layout: `layout/fragment_login.xml`

Fonctionnalités:
- Entrée email/password
- Validation complète
- Gestion des erreurs
- Affichage du loading
- Lien vers l'inscription
- Redirection vers home après succès

#### RegisterFragment
- Fichier: `ui/fragments/RegisterFragment.java`
- Layout: `layout/fragment_register.xml`

Fonctionnalités:
- Entrée firstName/lastName/email/password
- Validation complète
- Vérification email unique
- Confirmation password
- Hachage password
- Insertion en BD

---

## Flux d'authentification

### Connexion (Login)
```
1. Utilisateur entre email et password
2. Validation des champs
3. Recherche de l'utilisateur par email
4. Vérification du password
5. Si succès:
   - Sauvegarde session
   - Navigation vers Home
6. Si erreur:
   - Affichage du message d'erreur
```

### Inscription (Register)
```
1. Utilisateur entre nom, email, password
2. Validation des champs
3. Vérification si email existe
4. Génération salt + hash du password
5. Création utilisateur en BD
6. Si succès:
   - Sauvegarde session
   - Navigation vers Home
7. Si erreur:
   - Affichage du message d'erreur
```

---

## Design et Couleurs

### Header
- Fond: `primary_orange` (#FF6B3D)
- Texte: `white`

### Inputs
- Fond: `white`
- Texte: `text_dark` (#333333)
- Placeholder: `text_light` (#999999)

### Boutons
- Fond: `primary_orange`
- Texte: `white`
- Style: drawable `button_orange_background`

### Textes
- Titre: `text_dark`, 24sp, bold
- Sous-titre: `text_gray`, 14sp
- Lien: `primary_orange`, 14sp, bold
- Erreur: rouge (#D32F2F)

---

## Sécurité

### Hachage des mots de passe
```
Format stocké: "salt$hash"
- salt: Base64 encodé (32 bytes aléatoires)
- hash: SHA-256(password + salt) encodé en Base64
```

### Vérification
```
1. Extraire salt du mot de passe stocké
2. Hasher le password fourni avec le salt
3. Comparer les hash
```

### SharedPreferences
- Stocke uniquement: ID, email, nom
- Pas de password stocké
- Utile pour la navigation sans ressaisir

---

## Tests

### Test de login
```
1. Email: test@test.com
2. Password: 123456
3. S'inscrire d'abord si compte n'existe pas
```

### Test de registration
```
1. Prénom: Jean
2. Nom: Dupont
3. Email: jean@test.com
4. Password: 123456
5. Confirmer: 123456
```

### Test de validation
```
1. Essayer email sans @
2. Essayer mot de passe < 6 caractères
3. Essayer passwords non identiques
4. Vérifier message d'erreur s'affiche
```

---

## Améliorations futures

1. **BCrypt** - Remplacer SHA-256 par BCrypt
2. **Forgot Password** - Ajouter réinitialisation
3. **Email Verification** - Vérifier l'email avec un lien
4. **2FA** - Authentification à deux facteurs
5. **OAuth** - Login avec Google/Facebook
6. **Biometric** - Login avec empreinte/face

---

**Date:** April 2026  
**Version:** 1.0  
**Status:** ✅ Production-ready

