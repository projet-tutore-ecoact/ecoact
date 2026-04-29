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
 * Configuration principale de la base de données Room pour EcoAct.
 * 
 * Gère:
 * - Définition des entités
 * - Version du schéma
 * - Migrations des versions
 * - Instance singleton
 */
@Database(entities = {User.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    /**
     * Migration v1→v2: Ajout des colonnes d'authentification
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE users ADD COLUMN password TEXT NOT NULL DEFAULT ''");
            database.execSQL("ALTER TABLE users ADD COLUMN created_at INTEGER NOT NULL DEFAULT " + System.currentTimeMillis());
            database.execSQL("ALTER TABLE users ADD COLUMN updated_at INTEGER NOT NULL DEFAULT " + System.currentTimeMillis());
            database.execSQL("CREATE UNIQUE INDEX idx_users_email ON users(email)");
        }
    };

    /**
     * Obtient l'instance singleton de la base de données.
     * Thread-safe avec double-check locking.
     * 
     * @param context Contexte Android
     * @return Instance unique de AppDatabase
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ecoact_db")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()  // Utile en dev pour les problèmes de migration
                            .allowMainThreadQueries()           // Temporaire pour déboguer
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Réinitialise l'instance singleton (utiliser avec prudence, uniquement en développement).
     * ATTENTION: Cela supprime toutes les données!
     * 
     * @param context Contexte Android
     */
    public static void resetDatabase(Context context) {
        INSTANCE = null;
        context.deleteDatabase("ecoact_db");
    }

    // ============ DAOs ============
    public abstract UserDao userDao();

    // ============ Migrations ============
    // 
    // Pour ajouter une migration:
    // 1. Créer une classe Migration statique finale avec le nom MIGRATION_X_Y
    // 2. Implémenter la méthode migrate() avec le code SQL
    // 3. Incrémenter la version dans @Database
    // 4. Ajouter la migration à .addMigrations() dans getInstance()
    //
    // Exemple:
    // public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    //     @Override
    //     public void migrate(SupportSQLiteDatabase database) {
    //         database.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''");
    //     }
    // };
}