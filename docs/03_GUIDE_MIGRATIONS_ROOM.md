# 03 - Guide des Migrations Room

📁 **Location:** `docs/03_GUIDE_MIGRATIONS_ROOM.md`

---

## Table des matières

1. [Introduction](#introduction)
2. [Concepts fondamentaux](#concepts-fondamentaux)
3. [Processus étape par étape](#processus-étape-par-étape)
4. [Cas pratiques](#cas-pratiques)
5. [Bonnes pratiques](#bonnes-pratiques)
6. [Troubleshooting](#troubleshooting)

---

## Introduction

Room est une bibliothèque de persistance de données qui fournit une couche d'abstraction sur SQLite. Lors de modifications du schéma de la base de données, il est nécessaire de créer des migrations.

### Pourquoi les migrations sont importantes?

- **Compatibilité rétroactive**: Les utilisateurs existants gardent leurs données
- **Déploiement sécurisé**: Évite les pertes de données en production
- **Versioning**: Traçabilité des changements du schéma
- **Flexibilité**: Permet des changements structurels complexes

---

## Concepts fondamentaux

### Version du schéma

Chaque base de données Room a une **version** définie dans `@Database`:

```java
@Database(entities = {User.class}, version = 1)  // ← version
public abstract class AppDatabase extends RoomDatabase {
    // ...
}
```

### Migration

Une migration est un objet qui transforme la base de données d'une version à l'autre:

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Code SQL pour passer de v1 à v2
        database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''");
    }
};
```

### Règles importantes

1. **Les migrations doivent être ajoutées dans l'ordre**: 1→2→3→4 (pas 1→3)
2. **Une migration ne doit jamais être supprimée**: Elle est nécessaire pour les utilisateurs qui mettent à jour tard
3. **La version doit être incrémentée**: À chaque changement du schéma
4. **Tous les nouveaux champs doivent avoir une valeur par défaut** (sauf PRIMARY KEY)

---

## Processus étape par étape

### Étape 1: Identifier le changement

Décidez quel changement vous voulez faire:
- ✅ Ajouter une colonne
- ✅ Supprimer une colonne
- ✅ Modifier le type d'une colonne
- ✅ Ajouter une table
- ✅ Ajouter des constraints/indexes
- ✅ Renommer une table/colonne

### Étape 2: Modifier l'entité (classe Java)

```java
@Entity(tableName = "users")
public class User {
    // ... champs existants ...
    
    @ColumnInfo(name = "email")
    private String email;  // ← Ajouter
    
    // Getters et setters...
}
```

### Étape 3: Incrémenter la version

```java
@Database(entities = {User.class}, version = 2)  // 1 → 2
public abstract class AppDatabase extends RoomDatabase {
    // ...
}
```

### Étape 4: Créer la classe Migration

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''");
    }
};
```

### Étape 5: Enregistrer la migration

```java
INSTANCE = Room.databaseBuilder(...)
        .addMigrations(MIGRATION_1_2)  // ← Ajouter
        .build();
```

### Étape 6: Tester

1. Test sur nouvel appareil
2. Test avec migration depuis ancienne version
3. Vérifier que les anciennes données sont toujours présentes

---

## Cas pratiques

### Cas 1: Ajouter une colonne

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE users ADD COLUMN bio TEXT");
    }
};
```

### Cas 2: Ajouter une table

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL(
            "CREATE TABLE activities (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "name TEXT NOT NULL" +
            ")"
        );
    }
};
```

### Cas 3: Renommer une colonne

Utiliser la technique de créer nouvelle table + copier données.

### Cas 4: Supprimer une colonne

Même technique que renommer.

### Cas 5: Modifier le type d'une colonne

Créer nouvelle table avec nouveau type + copier données.

---

## Bonnes pratiques

### ✅ À faire

1. Toujours tester les migrations
2. Utiliser des valeurs par défaut appropriées
3. Documenter les migrations
4. Incrémenter la version progressivement (1→2→3)
5. Créer des index pour les performances
6. Tester les données existantes
7. Garder les anciennes migrations

### ❌ À éviter

1. Oublier d'incrémenter la version
2. Oublier d'ajouter à `.addMigrations()`
3. Modifier une migration existante
4. Utiliser `.fallbackToDestructiveMigration()` en production
5. Ajouter NOT NULL sans DEFAULT
6. Ne pas tester avant déployer

---

## Troubleshooting

### Erreur: "SchemaException: Insufficient data to populate"

**Cause:** Une migration manque entre deux versions

**Solution:** Vérifier que toutes les migrations sont présentes et enregistrées

### Erreur: "Cannot find migration from 1 to 2"

**Cause:** Migration manquante

**Solution:** Créer la migration et l'ajouter à `.addMigrations()`

### Erreur: "Foreign key constraint failed"

**Cause:** Les données violent une clé étrangère

**Solution:** Vérifier et corriger les données

### Les données disparaissent

**Cause:** Migration incorrecte ou `.fallbackToDestructiveMigration()`

**Solution:** Vérifier le code SQL, tester avec données réelles

---

## Checklist

- [ ] Version incrémentée dans `@Database`
- [ ] Classe Migration créée (MIGRATION_X_Y)
- [ ] Migration ajoutée à `.addMigrations()`
- [ ] Entité Java modifiée
- [ ] DAO modifié si nécessaire
- [ ] Testé sur nouvel appareil
- [ ] Testé avec migration depuis ancienne version
- [ ] Les données existantes sont préservées
- [ ] Pas de valeurs NULL sur colonnes NOT NULL
- [ ] Index créés si nécessaire
- [ ] Documentation ajoutée
- [ ] Commit Git avec description

---

**Version:** 1.0  
**Date:** April 2026  
**Status:** ✅ Complet

