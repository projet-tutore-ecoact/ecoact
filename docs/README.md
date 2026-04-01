# 📚 Documentation EcoAct

## Structure

Cette folder contient toute la documentation du projet EcoAct organisée par thème:

### 🔐 Authentification
- **AUTHENTICATION_SETUP.md** - Guide complet d'authentification
- **QUICK_START.md** - Démarrage rapide

### 🗄️ Migrations de base de données
- **GUIDE_MIGRATIONS_ROOM.md** - Guide complet des migrations (⭐ À lire!)
- **MIGRATION_QUICK_REFERENCE.md** - Aide-mémoire et templates

### 📱 Session et persistance
- **SESSION_MANAGER_EXPLAINED.md** - ⭐ Ce que fait SessionManager (TRÈS IMPORTANT!)
- **GENERATED_FILES.md** - UserDao_impl et fichiers générés

---

## 🎯 Par où commencer?

### Pour utiliser l'app
1. Lire: **QUICK_START.md**
2. Suivre les étapes

### Pour comprendre SessionManager
1. Lire: **SESSION_MANAGER_EXPLAINED.md** ⭐ (IMPORTANT!)
   - À quoi ça sert
   - Comment ça marche
   - Quand l'utiliser

### Pour comprendre les migrations
1. Lire: **GUIDE_MIGRATIONS_ROOM.md**
2. Consulter: **MIGRATION_QUICK_REFERENCE.md**
3. Référencer: **AUTHENTICATION_SETUP.md**

### Pour comprendre les fichiers générés
- Lire: **GENERATED_FILES.md** (UserDao_impl expliqué)

---

## 📖 Guide de lecture recommandé

### Ordre d'apprentissage (2-3 heures)

1. **QUICK_START.md** (30 min)
   - Aperçu rapide
   - Installation
   - Tests basiques

2. **SESSION_MANAGER_EXPLAINED.md** (20 min) ⭐
   - À quoi ça sert
   - Comment ça marche
   - Intégration complète

3. **GUIDE_MIGRATIONS_ROOM.md** (1 heure)
   - Comprendre les migrations
   - 5 cas pratiques
   - Best practices

4. **AUTHENTICATION_SETUP.md** (30 min)
   - Architecture authentification
   - Flux utilisateur
   - Détails techniques

5. **MIGRATION_QUICK_REFERENCE.md** (20 min)
   - Templates réutilisables
   - Commandes SQL
   - Patterns courants

6. **GENERATED_FILES.md** (10 min)
   - Comprendre UserDao_impl
   - Fichiers à ignorer

---

## 🔍 Recherche rapide

### Si vous cherchez...

| Besoin | Fichier |
|--------|---------|
| Démarrer rapidement | QUICK_START.md |
| À quoi sert SessionManager? | SESSION_MANAGER_EXPLAINED.md ⭐ |
| Comprendre les migrations | GUIDE_MIGRATIONS_ROOM.md |
| Voir un exemple d'auth | AUTHENTICATION_SETUP.md |
| Un template à copier | MIGRATION_QUICK_REFERENCE.md |
| UserDao_impl c'est quoi? | GENERATED_FILES.md |

---

## 📱 Fichiers correspondants

### Authentification & Session
- `LoginFragment.java`
- `RegisterFragment.java`
- `SessionManager.java` ⭐
- `PasswordUtils.java`
- `fragment_login.xml`
- `fragment_register.xml`

### Base de données
- `User.java`
- `UserDao.java`
- `AppDatabase.java`
- `UserRepository.java`

### Configuration
- `MainActivity.java`
- `nav_graph.xml`
- `strings.xml`

---

## ⚡ Accès rapide

### SessionManager
→ **SESSION_MANAGER_EXPLAINED.md** - Tout savoir sur SessionManager

### Besoin d'aide?
→ **QUICK_START.md** → Troubleshooting

### Erreur de migration?
→ **GUIDE_MIGRATIONS_ROOM.md** → Troubleshooting

### Besoin d'une template?
→ **MIGRATION_QUICK_REFERENCE.md** → Patterns

### Ne sais pas par où commencer?
→ Lire ce README, puis QUICK_START.md

---

**Dernière mise à jour:** April 2026  
**Version:** 1.0

⭐ **SessionManager est très important!** Voir **SESSION_MANAGER_EXPLAINED.md**


