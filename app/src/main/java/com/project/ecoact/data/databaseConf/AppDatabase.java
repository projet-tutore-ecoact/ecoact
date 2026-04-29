package com.project.ecoact.data.databaseConf;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.project.ecoact.data.dao.DeviceDao;
import com.project.ecoact.data.dao.EnergyDao;
import com.project.ecoact.data.dao.HabitDao;
import com.project.ecoact.data.dao.UserDao;
import com.project.ecoact.data.entity.DeviceEntity;
import com.project.ecoact.data.entity.EnergyEntity;
import com.project.ecoact.data.entity.HabitEntity;
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
@Database(entities = {User.class, EnergyEntity.class, DeviceEntity.class, HabitEntity.class}, version = 5, exportSchema = true)
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
     * Migration v3->v4: Ajout de la gestion des appareils utilisateur.
     */
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `devices` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`user_id` INTEGER NOT NULL, " +
                    "`type` TEXT, " +
                    "`reference` TEXT, " +
                    "`daily_usage_hours` REAL NOT NULL, " +
                    "`daily_consumption_kwh` REAL NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_devices_user_id` ON `devices` (`user_id`)");
        }
    };

    /**
     * Migration v4->v5: Ajout de la gestion des habitudes utilisateur.
     */
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `user_habits` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`user_id` INTEGER NOT NULL, " +
                    "`habit_key` TEXT, " +
                    "`category` TEXT, " +
                    "`question` TEXT, " +
                    "`frequency` TEXT, " +
                    "`inverted` INTEGER NOT NULL, " +
                    "`points` INTEGER NOT NULL, " +
                    "`max_points` INTEGER NOT NULL, " +
                    "`created_at` INTEGER NOT NULL, " +
                    "`updated_at` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_habits_user_id` ON `user_habits` (`user_id`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_habits_user_id_habit_key` ON `user_habits` (`user_id`, `habit_key`)");
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
                            .addMigrations(MIGRATION_1_2, MIGRATION_3_4, MIGRATION_4_5)
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
    public abstract EnergyDao energyDao();
    public abstract DeviceDao deviceDao();
    public abstract HabitDao habitDao();

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
