# Exemple d'implémentation - Migration Room

Ce fichier montre un exemple complet d'implémentation d'une migration.

## Scénario: Ajouter le support d'authentification

Nous allons modifier la table `users` pour ajouter les champs nécessaires à l'authentification par email/password.

### Étape 1: Modifier l'entité User.java

```java
package com.project.ecoact.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    // Nouveaux champs pour l'authentification
    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password")
    private String password;

    // Champs existants
    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    private String lastName;

    // Timestamps pour l'audit
    @ColumnInfo(name = "created_at")
    private Long createdAt;

    @ColumnInfo(name = "updated_at")
    private Long updatedAt;

    // Constructeur complet
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructeur vide pour Room
    public User() {
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
```

### Étape 2: Ajouter des méthodes au DAO

```java
package com.project.ecoact.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.project.ecoact.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(Long id);

    // 🆕 Nouvelle méthode pour l'authentification
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Insert
    void insertAll(User... users);

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}
```

### Étape 3: Modifier AppDatabase.java avec la migration

```java
package com.project.ecoact.data.databaseConf;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.project.ecoact.data.dao.UserDao;
import com.project.ecoact.data.entity.User;

/**
 * Configuration de la base de données avec migration v1 → v2
 */
@Database(entities = {User.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    /**
     * Migration de v1 à v2
     * Ajoute les colonnes nécessaires pour l'authentification par email
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Ajouter la colonne email (unique pour prévenir les doublons)
            database.execSQL(
                "ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''"
            );

            // Ajouter la colonne password
            database.execSQL(
                "ALTER TABLE users ADD COLUMN password TEXT NOT NULL DEFAULT ''"
            );

            // Ajouter les timestamps
            database.execSQL(
                "ALTER TABLE users ADD COLUMN created_at INTEGER NOT NULL DEFAULT " +
                System.currentTimeMillis()
            );

            database.execSQL(
                "ALTER TABLE users ADD COLUMN updated_at INTEGER NOT NULL DEFAULT " +
                System.currentTimeMillis()
            );

            // Créer un index UNIQUE sur email pour les performances et l'intégrité
            database.execSQL(
                "CREATE UNIQUE INDEX idx_users_email ON users(email)"
            );
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "ecoact_db"
                    )
                    // ✅ Important: enregistrer la migration
                    .addMigrations(MIGRATION_1_2)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void resetDatabase(Context context) {
        INSTANCE = null;
        context.deleteDatabase("ecoact_db");
    }

    public abstract UserDao userDao();
}
```

### Étape 4: Mettre à jour le Repository

```java
package com.project.ecoact.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.project.ecoact.data.dao.UserDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    public LiveData<List<User>> getAll() {
        return userDao.getAll();
    }

    public LiveData<User> getUserById(Long id) {
        return userDao.getUserById(id);
    }

    // 🆕 Méthode pour l'authentification
    public User getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public void insert(User user) {
        executor.execute(() -> userDao.insert(user));
    }

    public void insert(User... users) {
        executor.execute(() -> userDao.insertAll(users));
    }

    public void update(User user) {
        executor.execute(() -> userDao.update(user));
    }

    public void delete(User user) {
        executor.execute(() -> userDao.delete(user));
    }
}
```

### Étape 5: Utiliser le nouveau système

Dans votre Fragment ou ViewModel:

```java
// Enregistrement d'un nouvel utilisateur
User newUser = new User(
    "user@example.com",
    "hashedPassword123",  // Doit être hashé avec BCrypt
    "John",
    "Doe"
);
userRepository.insert(newUser);

// Connexion
User user = userRepository.getUserByEmail("user@example.com");
if (user != null && user.getPassword().equals(providedPassword)) {
    // Authentification réussie
    Toast.makeText(context, "Bienvenue " + user.getFirstName(), Toast.LENGTH_SHORT).show();
} else {
    // Authentification échouée
    Toast.makeText(context, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
}
```

## Points clés

✅ **Version incrémentée** de 1 à 2  
✅ **Migration créée** avec le code SQL  
✅ **Migration enregistrée** dans getInstance()  
✅ **Entité modifiée** avec les nouveaux champs  
✅ **DAO mis à jour** avec les nouvelles méthodes  
✅ **Index créé** sur email pour les performances  
✅ **Valeurs par défaut** pour les colonnes NOT NULL  

## Test de cette migration

1. **Installation initiale**: La BD v2 sera créée directement
2. **Mise à jour depuis v1**: La migration s'exécutera automatiquement
3. **Données existantes**: Les utilisateurs conserveront leurs données (email et password vides par défaut)

## Prochaine migration (Exemple)

Si vous voulez ajouter plus tard une table `activities`:

1. Créer l'entité Activity
2. Incrémenter la version à 3
3. Créer MIGRATION_2_3
4. L'ajouter à .addMigrations(MIGRATION_1_2, MIGRATION_2_3)

---

**Date**: April 2026  
**Status**: ✅ Exemple fonctionnel

