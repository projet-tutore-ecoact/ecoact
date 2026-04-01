# 🔧 Correction des erreurs Room

## Erreur 1: Cannot access database on the main thread

### ❌ Le problème
```
java.lang.IllegalStateException: Cannot access database on the main thread 
since it may potentially lock the UI for a long period of time.
```

### 🎯 Cause
Room n'autorise **PAS l'accès à la BD sur le main thread** car:
- Les requêtes BD peuvent prendre du temps
- Bloquer le main thread gèle l'UI
- L'app devient non-réactive

### ✅ Solution
**TOUJOURS accéder à la BD sur un thread background!**

### 📋 Code avant (❌ Erreur)
```java
// ❌ MAUVAIS - Sur le main thread
Long userId = sessionManager.getCurrentUserId();
User user = userRepository.getUserByIdSync(userId);  // ← ERREUR!
tvUserName.setText(user.getFirstName());  // Crash!
```

### ✅ Code corrigé
```java
// ✅ BON - Sur un thread background
new Thread(() -> {
    // Accès BD sur thread background
    Long userId = sessionManager.getCurrentUserId();
    User user = userRepository.getUserByIdSync(userId);  // ← OK!
    
    // Revenir au main thread pour UI
    getActivity().runOnUiThread(() -> {
        tvUserName.setText(user.getFirstName());  // ← OK!
    });
}).start();
```

### 🏗️ Architecture correcte
```
Main Thread (UI):
├─ onCreate()
├─ Démarrer background thread
└─ Mettre à jour UI (dans runOnUiThread)

Background Thread (BD):
├─ Accéder à la BD
├─ Requête SQLite
└─ Revenir au main thread
```

---

## Erreur 2: Schema export directory was not provided

### ❌ Le problème
```
warning: Schema export directory was not provided to the annotation processor 
so Room cannot export the schema.
```

### 🎯 Cause
- `exportSchema = true` en BD
- Mais pas de dossier configuré pour exporter
- Room ne sait pas où sauvegarder le schéma

### ✅ Solution 1: Configurer le dossier (recommandé)

**Dans build.gradle.kts:**
```kotlin
// Ajouter le plugin Room
plugins {
    id("androidx.room")
}

room {
    schemaDirectory("$projectDir/schemas")  // ← Ajouter cette ligne
}
```

**Puis dans AppDatabase.java:**
```java
@Database(entities = {User.class}, version = 2, exportSchema = true)
// exportSchema = true = exporter le schéma
```

### ✅ Solution 2: Désactiver l'export (simple mais moins bon)

**Dans AppDatabase.java:**
```java
@Database(entities = {User.class}, version = 2, exportSchema = false)
// exportSchema = false = pas d'export
```

### 📁 Dossier créé
```
EcoAct/
└─ app/
   ├─ src/
   └─ schemas/
      └─ com.project.ecoact.data.databaseConf.AppDatabase/
         └─ 1.json (schéma v1)
         └─ 2.json (schéma v2)
```

### 🎯 Avantages du schéma exporté
✅ **Version control** - Tracker les changements BD
✅ **Documentation** - Voir l'évolution du schéma
✅ **Vérification** - Vérifier les migrations
✅ **Collaboration** - Partager le schéma avec l'équipe

---

## 📋 Résumé des corrections

### Correction 1: build.gradle.kts
```diff
+ id("androidx.room")

  room {
+     schemaDirectory("$projectDir/schemas")
  }
```

### Correction 2: ProfileFragment.java
```diff
- loadUserInfo()
+ loadUserInfoAsync()

  private void loadUserInfoAsync() {
      new Thread(() -> {
          // ✅ Accès BD sur background thread
          User user = userRepository.getUserByIdSync(userId);
          
          getActivity().runOnUiThread(() -> {
              // ✅ Mise à jour UI sur main thread
              tvUserName.setText(user.getFirstName());
          });
      }).start();
  }
```

---

## 🧪 Tests

### Test 1: Pas d'erreur main thread
```
1. Ouvrir ProfileFragment
2. ✅ DEVRAIT afficher les infos sans erreur
3. ✅ Pas de crash
```

### Test 2: Pas de warning
```
1. Build → Rebuild Project
2. ✅ DEVRAIT pas avoir de warning Room
3. ✅ Dossier schemas/ créé avec JSON
```

---

## 📊 Pattern à toujours utiliser

### ❌ JAMAIS
```java
// ❌ Accès BD sur main thread
User user = userRepository.getUserByIdSync(userId);
```

### ✅ TOUJOURS
```java
// ✅ Accès BD sur background thread
new Thread(() -> {
    User user = userRepository.getUserByIdSync(userId);
    
    getActivity().runOnUiThread(() -> {
        // UI update
    });
}).start();
```

---

## 🎯 Règles Room

1. **Jamais** accéder à la BD sur le main thread
2. **Toujours** utiliser un background thread
3. **Toujours** revenir au main thread pour l'UI
4. **Toujours** configurer le schemaLocation si exportSchema=true

---

**Date:** April 2026  
**Importance:** ⭐⭐⭐ (Critique!)

Ces erreurs doivent être corrigées pour que l'app fonctionne correctement.

