# Aide-mémoire - Migrations Room

Référence rapide pour créer une migration Room dans EcoAct.

## Checklist rapide

```
AVANT DE COMMENCER:
☐ Sauvegardez votre travail (git commit)
☐ Vérifiez quelle version est actuellement utilisée dans @Database

CRÉER UNE MIGRATION:
☐ Modifier la classe entité (ajouter/modifier/supprimer champs)
☐ Incrémenter la version dans @Database (1 → 2, 2 → 3, etc.)
☐ Créer la classe Migration (MIGRATION_X_Y)
☐ Ajouter la migration à .addMigrations(...)
☐ Tester sur nouvel appareil
☐ Tester avec migration depuis ancienne version
☐ Vérifier que les anciennes données sont conservées

AVANT LE DÉPLOIEMENT:
☐ Revoir le code SQL
☐ Tester les cas limites (NULL, clés étrangères, etc.)
☐ Vérifier les performances (surtout pour gros volumes)
☐ Documenter le changement
```

---

## Template - Migration minimale

### 1. Modifier l'entité

```java
@Entity(tableName = "ma_table")
public class MaClasse {
    // ... champs existants ...
    
    @ColumnInfo(name = "nouveau_champ")  // ← Ajouter
    private String nouveauChamp;
    
    public String getNouveauChamp() { return nouveauChamp; }
    public void setNouveauChamp(String value) { this.nouveauChamp = value; }
}
```

### 2. AppDatabase.java

```java
@Database(entities = {MaClasse.class}, version = 2, exportSchema = true)  // ← Incrémenter
public abstract class AppDatabase extends RoomDatabase {
    
    // Ajouter AVANT la classe
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ma_table ADD COLUMN nouveau_champ TEXT");
        }
    };
    
    // Dans getInstance()
    INSTANCE = Room.databaseBuilder(...)
        .addMigrations(MIGRATION_1_2)  // ← Ajouter
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

### Clé étrangère

```sql
CREATE TABLE activities (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## Patterns courants

### Pattern 1: Ajouter une colonne simple

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE users ADD COLUMN bio TEXT");
    }
};
```

### Pattern 2: Renommer (changement de schéma complet)

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Créer nouveau schéma
        database.execSQL(
            "CREATE TABLE users_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "givenName TEXT," +           // Nouveau nom
            "lastName TEXT" +
            ")"
        );
        
        // Copier données
        database.execSQL(
            "INSERT INTO users_new (id, givenName, lastName) " +
            "SELECT id, first_name, last_name FROM users"
        );
        
        // Nettoyer
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
            "CREATE TABLE activities (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "name TEXT NOT NULL," +
            "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ")"
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
        // SQLite n'a pas DROP COLUMN facile
        database.execSQL(
            "CREATE TABLE users_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "first_name TEXT," +
            "last_name TEXT" +
            ")"
        );
        database.execSQL(
            "INSERT INTO users_new (id, first_name, last_name) " +
            "SELECT id, first_name, last_name FROM users"
        );
        database.execSQL("DROP TABLE users");
        database.execSQL("ALTER TABLE users_new RENAME TO users");
    }
};
```

---

## Erreurs courantes et solutions

| Erreur | Cause | Solution |
|--------|-------|----------|
| `Cannot find migration from 1 to 2` | Migration non enregistrée | Ajouter à `.addMigrations()` |
| `Uniqueness constraint failed` | Doublons dans une colonne UNIQUE | Vérifier les données avant migration |
| `Foreign key constraint failed` | Données orphelines | Nettoyer les données avant migration |
| `Insufficient data to populate` | Migration manquante entre versions | Créer la migration manquante |
| `database is locked` | Accès concurrent | Utiliser transactions |
| Données vides après mise à jour | Migration incorrecte ou `.fallbackToDestructiveMigration()` | Vérifier le code SQL |

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
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN email TEXT");
        }
    };
    
    public static AppDatabase getInstance(Context context) {
        return Room.databaseBuilder(...)
            .addMigrations(MIGRATION_1_2)
            .build();
    }
}
```

---

## Ressources rapides

| Ressource | Lien |
|-----------|------|
| Doc Room | https://developer.android.com/training/data-storage/room |
| Migrations | https://developer.android.com/training/data-storage/room/migrating-db-versions |
| SQLite ALTER | https://www.sqlite.org/lang_altertable.html |
| Guide complet | Voir `GUIDE_MIGRATIONS_ROOM.md` |
| Exemple complet | Voir `EXEMPLE_MIGRATION_COMPLETE.md` |

---

## Étapes pour ajouter une migration

```
1. Modifier l'entité Java
   ↓
2. Incrémenter version dans @Database
   ↓
3. Créer classe Migration MIGRATION_X_Y
   ↓
4. Écrire le code SQL dans migrate()
   ↓
5. Ajouter à .addMigrations() dans getInstance()
   ↓
6. Tester sur appareil
   ↓
7. Tester avec migration depuis ancienne version
   ↓
8. Valider que les données sont conservées
```

---

## Tips & Tricks

✅ **Always** garder les anciennes migrations dans le code  
✅ **Always** tester avant déployer  
✅ **Always** utiliser des valeurs par défaut pour NOT NULL  
✅ **Always** créer des index pour les colonnes souvent cherchées  

❌ **Never** supprimer une migration  
❌ **Never** oublier d'ajouter la migration à .addMigrations()  
❌ **Never** utiliser `.fallbackToDestructiveMigration()` en production  
❌ **Never** faire une colonne NOT NULL sans DEFAULT  

---

**Version**: 1.0  
**Date**: April 2026  
**Utilisation**: Imprimer ou bookmarquer pour référence rapide

