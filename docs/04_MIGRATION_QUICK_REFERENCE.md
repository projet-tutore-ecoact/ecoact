# 04 - Référence Rapide Migrations

📁 **Location:** `docs/04_MIGRATION_QUICK_REFERENCE.md`

---

## Checklist rapide

```
☐ Modifier la classe entité
☐ Incrémenter la version dans @Database
☐ Créer la classe Migration MIGRATION_X_Y
☐ Ajouter à .addMigrations(...)
☐ Tester sur nouvel appareil
☐ Tester avec migration depuis ancienne version
☐ Vérifier que les données sont conservées
```

---

## Template minimal - Migration

### 1. Modifier l'entité

```java
@Entity(tableName = "ma_table")
public class MaClasse {
    // ... champs existants ...
    
    @ColumnInfo(name = "nouveau_champ")
    private String nouveauChamp;
    
    public String getNouveauChamp() { return nouveauChamp; }
    public void setNouveauChamp(String value) { this.nouveauChamp = value; }
}
```

### 2. AppDatabase.java

```java
@Database(entities = {MaClasse.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ma_table ADD COLUMN nouveau_champ TEXT");
        }
    };
    
    // Dans getInstance()
    INSTANCE = Room.databaseBuilder(...)
        .addMigrations(MIGRATION_1_2)
        .build();
}
```

---

## Commandes SQL courantes

### Ajouter une colonne
```sql
ALTER TABLE ma_table ADD COLUMN nouveau_champ TEXT NOT NULL DEFAULT '';
ALTER TABLE ma_table ADD COLUMN age INTEGER;
ALTER TABLE ma_table ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0;
```

### Créer une table
```sql
CREATE TABLE ma_table (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nom TEXT NOT NULL,
    email TEXT UNIQUE,
    created_at INTEGER
);
```

### Créer un index
```sql
CREATE INDEX idx_table_colonne ON ma_table(colonne);
CREATE UNIQUE INDEX idx_users_email ON users(email);
```

---

## 4 Patterns courants

### Pattern 1: Ajouter une colonne simple

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE users ADD COLUMN bio TEXT");
    }
};
```

### Pattern 2: Renommer (changement de schéma)

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE users_new (id INTEGER PRIMARY KEY, givenName TEXT, lastName TEXT)");
        database.execSQL("INSERT INTO users_new SELECT id, first_name, last_name FROM users");
        database.execSQL("DROP TABLE users");
        database.execSQL("ALTER TABLE users_new RENAME TO users");
    }
};
```

### Pattern 3: Ajouter une table

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL(
            "CREATE TABLE activities (id INTEGER PRIMARY KEY, user_id INTEGER NOT NULL)"
        );
        database.execSQL("CREATE INDEX idx_activities_user_id ON activities(user_id)");
    }
};
```

### Pattern 4: Supprimer une colonne

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE users_new (id INTEGER PRIMARY KEY, first_name TEXT, last_name TEXT)");
        database.execSQL("INSERT INTO users_new SELECT id, first_name, last_name FROM users");
        database.execSQL("DROP TABLE users");
        database.execSQL("ALTER TABLE users_new RENAME TO users");
    }
};
```

---

## Tableau Erreurs/Solutions

| Erreur | Cause | Solution |
|--------|-------|----------|
| `Cannot find migration from 1 to 2` | Migration non enregistrée | Ajouter à `.addMigrations()` |
| `Uniqueness constraint failed` | Doublons dans colonne UNIQUE | Vérifier les données |
| `Foreign key constraint failed` | Données orphelines | Nettoyer les données |
| `Insufficient data to populate` | Migration manquante | Créer la migration manquante |
| `database is locked` | Accès concurrent | Utiliser transactions |
| Données vides après mise à jour | Migration incorrecte | Vérifier le SQL |

---

## Tips & Tricks

✅ **Always** garder les anciennes migrations dans le code  
✅ **Always** tester avant déployer  
✅ **Always** utiliser des valeurs par défaut pour NOT NULL  
✅ **Always** créer des index pour les colonnes souvent cherchées  

❌ **Never** supprimer une migration  
❌ **Never** oublier d'ajouter à `.addMigrations()`  
❌ **Never** utiliser `.fallbackToDestructiveMigration()` en production  
❌ **Never** faire une colonne NOT NULL sans DEFAULT  

---

## Comparaison: Avant vs Après

### AVANT (Sans support migrations)
```java
@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    // Pas de migration!
    // Si le schéma change → crash de l'app
}
```

### APRÈS (Avec support migrations)
```java
@Database(entities = {User.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN email TEXT");
        }
    };
    
    // .addMigrations(MIGRATION_1_2)
}
```

---

**Version:** 1.0  
**Date:** April 2026

