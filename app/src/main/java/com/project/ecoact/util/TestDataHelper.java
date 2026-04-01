package com.project.ecoact.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.User;

/**
 * Utilitaire pour insérer des données de test dans la BD
 * À utiliser uniquement en développement/debug
 */
public class TestDataHelper {
    private static final String TAG = "TestDataHelper";
    private static final String PREF_TEST_DATA_INSERTED = "test_data_inserted";
    private static final String PREF_NAME = "ecoact_prefs";

    /**
     * Insérer un utilisateur de test au premier démarrage
     * Cette méthode s'exécute une seule fois (vérifiée via SharedPreferences)
     */
    public static void insertTestDataIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // Vérifier si on a déjà inséré les données de test
        if (prefs.getBoolean(PREF_TEST_DATA_INSERTED, false)) {
            Log.d(TAG, "Données de test déjà insérées");
            return;  // Données de test déjà insérées
        }

        // Nettoyer d'abord la BD (utile en développement)
        Log.d(TAG, "🧹 Nettoyage de la base de données...");
        cleanDatabase(context);

        // Insérer sur un thread background
        new Thread(() -> {
            try {
                // Attendre un peu pour que le nettoyage soit terminé
                Thread.sleep(500);
                
                Log.d(TAG, "Insertion des données de test...");
                
                com.project.ecoact.data.databaseConf.AppDatabase db = com.project.ecoact.data.databaseConf.AppDatabase.getInstance(context);
                
                // ✅ Vérifier si l'utilisateur existe déjà avant d'insérer
                User existingUser = db.userDao().findByEmail("test@t.com");
                if (existingUser != null) {
                    Log.d(TAG, "✅ Utilisateur de test existe déjà!");
                    // Marquer que les données de test sont là
                    prefs.edit()
                        .putBoolean(PREF_TEST_DATA_INSERTED, true)
                        .apply();
                    return;
                }
                
                // Créer un utilisateur de test
                User testUser = new User(
                    "test@t.com",
                    PasswordUtils.encodePassword("Test123!"),  // Password hashé
                    "Test",
                    "User"
                );
                
                Log.d(TAG, "Utilisateur de test créé: " + testUser.getEmail());
                
                // Insérer dans la BD
                long userId = db.userDao().insert(testUser);
                
                Log.d(TAG, "Insertion réussie. User ID: " + userId);
                
                if (userId > 0) {
                    // Marquer que les données de test ont été insérées
                    prefs.edit()
                        .putBoolean(PREF_TEST_DATA_INSERTED, true)
                        .apply();
                    
                    Log.d(TAG, "✅ Données de test insérées avec succès!");
                    Log.d(TAG, "Identifiants: test@t.com / Test123!");
                } else {
                    Log.e(TAG, "❌ Erreur: insertion échouée (userId <= 0)");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Erreur lors de l'insertion des données de test", e);
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Nettoyer la base de données complètement
     * À utiliser uniquement en développement!
     */
    private static void cleanDatabase(Context context) {
        try {
            Log.d(TAG, "🧹 Suppression de la base de données...");
            com.project.ecoact.data.databaseConf.AppDatabase.resetDatabase(context);
            Log.d(TAG, "✅ Base de données nettoyée!");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur lors du nettoyage", e);
            e.printStackTrace();
        }
    }

    /**
     * Réinitialiser les données de test (pour déboguer)
     * Appeler cette méthode si tu veux réinsérer les données de test
     */
    public static void resetTestData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(PREF_TEST_DATA_INSERTED, false)
            .apply();
        
        Log.d(TAG, "Données de test réinitialisées. Relance l'app pour réinsérer.");
    }
}



