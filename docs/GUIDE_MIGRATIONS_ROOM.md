# Guide Complet des Migrations Room - EcoAct

## Table des matières

1. [Introduction](#introduction)
2. [Concepts fondamentaux](#concepts-fondamentaux)
3. [Processus étape par étape](#processus-étape-par-étape)
4. [Cas pratiques](#cas-pratiques)
5. [Bonnes pratiques](#bonnes-pratiques)
6. [Troubleshooting](#troubleshooting)

---

## Introduction

Room est une bibliothèque de persistance de données Android qui simplifie l'utilisation de SQLite. Lorsque vous modifiez le schéma de votre base de données (ajout/suppression/modification de colonnes ou de tables), vous devez créer des **migrations** pour transformer les données existantes vers la nouvelle structure.

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

Par exemple, pour ajouter un champ `email` à User:

```java
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    private String lastName;

    // 🆕 Nouveau champ
    @ColumnInfo(name = "email")
    private String email;

    // Getters et setters...
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

### Étape 3: Incrémenter la version

Dans `AppDatabase.java`:

```java
@Database(entities = {User.class}, version = 2)  // 1 → 2
public abstract class AppDatabase extends RoomDatabase {
    // ...
}
```

### Étape 4: Créer la classe Migration

Toujours dans `AppDatabase.java`, ajoutez la migration AVANT la classe:

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Ajouter la colonne email
        database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''");
        
        // Créer un index si nécessaire
        database.execSQL("CREATE INDEX idx_users_email ON users(email)");
    }
};
```

### Étape 5: Enregistrer la migration

Dans la méthode `getInstance()` de `AppDatabase.java`:

```java
INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
        AppDatabase.class, "ecoact_db")
        .addMigrations(MIGRATION_1_2)  // ← Ajouter ici
        .build();
```

### Étape 6: Tester

1. **Test sur un nouvel appareil**: Vérifie que la BD v2 est créée correctement
2. **Test avec migration**: Installer l'ancienne app, puis mettre à jour vers la nouvelle
3. **Test des données**: Vérifier que les anciennes données sont toujours présentes

---

## Cas pratiques

### Cas 1: Ajouter une colonne

**Objectif**: Ajouter un champ `bio` (biographie) à la table users

#### 1. Modifier User.java

```java
@Entity(tableName = "users")
public class User {
    // ... colonnes existantes ...
    
    @ColumnInfo(name = "bio")
    private String bio;  // 🆕
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
```

#### 2. AppDatabase.java

```java
@Database(entities = {User.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Ajouter la colonne bio (NULL autorisé)
            database.execSQL("ALTER TABLE users ADD COLUMN bio TEXT");
        }
    };
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ecoact_db")
                            .addMigrations(MIGRATION_1_2)  // ← Important!
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    
    // ...
}
```

### Cas 2: Ajouter une nouvelle table (Entité)

**Objectif**: Ajouter une table `activities` pour tracker les activités écologiques

#### 1. Créer l'entité Activity

```java
@Entity(tableName = "activities")
public class Activity {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    
    @ColumnInfo(name = "user_id")
    private Long userId;  // Foreign key
    
    @ColumnInfo(name = "name")
    private String name;
    
    @ColumnInfo(name = "points")
    private Integer points;
    
    @ColumnInfo(name = "date")
    private Long date;
    
    // Constructeurs, getters, setters...
}
```

#### 2. Ajouter à @Database

```java
@Database(
    entities = {User.class, Activity.class},  // ← Ajouter Activity
    version = 2,
    exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {
    // ...
    
    public abstract ActivityDao activityDao();  // ← Ajouter le DAO
}
```

#### 3. Créer ActivityDao

```java
@Dao
public interface ActivityDao {
    @Insert
    long insert(Activity activity);
    
    @Query("SELECT * FROM activities WHERE user_id = :userId ORDER BY date DESC")
    LiveData<List<Activity>> getUserActivities(Long userId);
    
    @Delete
    void delete(Activity activity);
}
```

#### 4. Ajouter la migration

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Créer la nouvelle table
        database.execSQL(
            "CREATE TABLE activities (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER NOT NULL," +
            "name TEXT NOT NULL," +
            "points INTEGER NOT NULL DEFAULT 0," +
            "date INTEGER NOT NULL," +
            "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ")"
        );
        
        // Créer les index pour les performances
        database.execSQL("CREATE INDEX idx_activities_user_id ON activities(user_id)");
        database.execSQL("CREATE INDEX idx_activities_date ON activities(date)");
    }
};
```

### Cas 3: Renommer une colonne

**Objectif**: Renommer `first_name` en `givenName` pour plus de clarté

**⚠️ ATTENTION**: SQLite ne supporte pas `RENAME COLUMN` de manière simple avant SQLite 3.25.0. La solution est:
1. Créer une nouvelle table avec le nouveau schéma
2. Copier les données
3. Supprimer l'ancienne table
4. Renommer la nouvelle

#### Solution

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Créer une nouvelle table avec le nouveau schéma
        database.execSQL(
            "CREATE TABLE users_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "givenName TEXT," +                    // Ancien: first_name
            "lastName TEXT," +
            "FOREIGN KEY(...) REFERENCES ..." +
            ")"
        );
        
        // Copier les données de l'ancienne vers la nouvelle
        database.execSQL(
            "INSERT INTO users_new (id, givenName, lastName) " +
            "SELECT id, first_name, last_name FROM users"
        );
        
        // Supprimer l'ancienne table
        database.execSQL("DROP TABLE users");
        
        // Renommer la nouvelle table
        database.execSQL("ALTER TABLE users_new RENAME TO users");
        
        // Recréer les index
        database.execSQL("CREATE INDEX idx_users_email ON users(email)");
    }
};
```

### Cas 4: Supprimer une colonne

**Objectif**: Supprimer une colonne inutilisée

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // SQLite ne supporte pas ALTER TABLE DROP COLUMN directement
        // Solution: créer une nouvelle table sans la colonne
        
        database.execSQL(
            "CREATE TABLE users_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "first_name TEXT," +
            "last_name TEXT" +
            ")"
        );
        
        // Copier seulement les colonnes à conserver
        database.execSQL(
            "INSERT INTO users_new (id, first_name, last_name) " +
            "SELECT id, first_name, last_name FROM users"
        );
        
        database.execSQL("DROP TABLE users");
        database.execSQL("ALTER TABLE users_new RENAME TO users");
    }
};
```

### Cas 5: Modifier le type d'une colonne

**Objectif**: Changer une colonne de TEXT à INTEGER

```java
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Créer nouvelle table avec le type modifié
        database.execSQL(
            "CREATE TABLE users_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "first_name TEXT," +
            "age INTEGER" +              // Changé de TEXT à INTEGER
            ")"
        );
        
        // Copier avec conversion de type
        database.execSQL(
            "INSERT INTO users_new (id, first_name, age) " +
            "SELECT id, first_name, CAST(age AS INTEGER) FROM users"
        );
        
        database.execSQL("DROP TABLE users");
        database.execSQL("ALTER TABLE users_new RENAME TO users");
    }
};
```

---

## Bonnes pratiques

### ✅ À faire

1. **Toujours tester les migrations**
   ```bash
   # En développement, testez:
   # 1. Nouvelle installation (BD vierge)
   # 2. Mise à jour depuis l'ancienne version
   ```

2. **Utiliser des valeurs par défaut appropriées**
   ```sql
   ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT '';
   ```

3. **Créer des index pour les performances**
   ```sql
   CREATE INDEX idx_users_email ON users(email);
   ```

4. **Documenter les migrations**
   ```java
   /**
    * Migration v1→v2: Ajout de la colonne email
    * Raison: Support du système de authentification par email
    * Date: 2026-04-01
    */
   public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
       // ...
   };
   ```

5. **Valider les données après migration**
   ```java
   public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
       @Override
       public void migrate(SupportSQLiteDatabase database) {
           // Changements...
           
           // Validation: vérifier les données critiques
           Cursor cursor = database.query("SELECT COUNT(*) FROM users WHERE email IS NULL");
           if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
               // Gérer les données NULL si nécessaire
           }
       }
   };
   ```

6. **Garder les migrations anciennes**
   - Les migrations doivent rester dans le code
   - Nécessaires pour les utilisateurs qui mettent à jour tard
   - N'occupent presque aucun espace

7. **Incrémenter progressivement**
   ```
   ✅ Bon:   v1 → v2 → v3 → v4
   ❌ Mauvais: v1 → v3 (sauter une version)
   ```

### ❌ À éviter

1. **Oublier d'incrémenter la version**
   - Room ignorera la migration
   - Votre app plantera au démarrage

2. **Ajouter des colonnes NOT NULL sans DEFAULT**
   ```java
   // ❌ Mauvais
   database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL");
   
   // ✅ Bon
   database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''");
   ```

3. **Utiliser `.fallbackToDestructiveMigration()`**
   ```java
   // ❌ À ÉVITER - Supprime TOUTES les données!
   .fallbackToDestructiveMigration()
   ```

4. **Faire des migrations trop complexes**
   - Divisez en étapes si possible
   - Testez chaque étape

5. **Ne pas tester les migrations**
   - Testez toujours avant de déployer
   - Testez avec de vraies données

6. **Modifier directement la BD en production**
   - Toujours passer par des migrations
   - Évite les incohérences

---

## Troubleshooting

### Problème: "SchemaException: Insufficient data to populate"

**Cause**: Une migration manque entre deux versions

**Solution**:
```java
// Vérifier que toutes les migrations sont présentes
.addMigrations(
    MIGRATION_1_2,  // Assurez-vous que celle-ci existe!
    MIGRATION_2_3,
    MIGRATION_3_4
)
```

### Problème: "Cannot find migration from 1 to 2"

**Cause**: Vous avez incrémenté la version mais pas créé la migration

**Solution**:
```java
// Créer la migration
public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Vos changements SQL
    }
};

// L'ajouter
.addMigrations(MIGRATION_1_2)
```

### Problème: "Foreign key constraint failed"

**Cause**: Les données violent une clé étrangère

**Solution**:
```java
// Vérifier les données et corriger
database.execSQL("DELETE FROM activities WHERE user_id NOT IN (SELECT id FROM users)");
```

### Problème: Les données disparaissent après la mise à jour

**Cause**: Probablement une migration incorrecte

**Solution**:
1. Vérifier le code SQL dans la migration
2. Vérifier que les données sont bien copiées
3. Tester avec des données réelles
4. Regarder les logs Android:
   ```bash
   adb logcat | grep "Room"
   ```

### Problème: "database is locked"

**Cause**: Plusieurs threads accèdent simultanément à la BD

**Solution**:
```java
// Utiliser des transactions
database.beginTransaction();
try {
    database.execSQL("..."); // Vos commandes
    database.setTransactionSuccessful();
} finally {
    database.endTransaction();
}
```

### Problème: La migration prend trop longtemps

**Cause**: Les données sont volumineuses

**Solution**:
```java
// Utiliser des transactions pour les performances
database.beginTransaction();
try {
    database.execSQL("CREATE TABLE ...");
    database.execSQL("INSERT INTO ... SELECT ...");
    database.execSQL("DROP TABLE ...");
    database.setTransactionSuccessful();
} finally {
    database.endTransaction();
}
```

---

## Checklist pour une migration

Avant de déployer une migration:

- [ ] Version incrémentée dans `@Database`
- [ ] Classe Migration créée (MIGRATION_X_Y)
- [ ] Migration ajoutée à `.addMigrations()`
- [ ] Entité Java modifiée si nécessaire
- [ ] DAO modifié si nécessaire
- [ ] Testé sur un nouvel appareil
- [ ] Testé en mettant à jour depuis l'ancienne version
- [ ] Les données anciennes sont préservées
- [ ] Les requêtes SQL testées
- [ ] Pas de valeurs NULL sur colonnes NOT NULL
- [ ] Index créés pour les performances
- [ ] Documentation/commentaires ajoutés
- [ ] Commit Git avec description claire

---

## Ressources utiles

- [Documentation officielle Room - Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [SQLite - ALTER TABLE](https://www.sqlite.org/lang_altertable.html)
- [SQLite - Data Types](https://www.sqlite.org/datatype3.html)
- [Android Architecture Components](https://developer.android.com/topic/architecture)

---

## Questions fréquentes

**Q: Dois-je garder les anciennes migrations?**
R: OUI! Elles sont nécessaires pour les utilisateurs qui mettent à jour tard. Elles occupent peu d'espace.

**Q: Que faire si je découvre une erreur dans une migration après déploiement?**
R: Créer une nouvelle migration pour corriger. Jamais modifier une migration existante.

**Q: Puis-je relancer une migration?**
R: Non, Room suit la version. Pour relancer, il faudrait réinstaller l'app ou réinitialiser la BD.

**Q: Comment tester les migrations?**
R: Installez l'ancienne app, créez des données, puis mettez à jour vers la nouvelle app.

**Q: Peut-on faire plusieurs migrations d'un coup?**
R: Oui, mais c'est plus risqué. Préférez des petites migrations progressives.

---

**Dernière mise à jour**: April 2026  
**Version**: 1.0  
**Statut**: ✅ Complet

