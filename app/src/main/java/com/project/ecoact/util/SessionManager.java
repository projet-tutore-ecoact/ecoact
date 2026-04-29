package com.project.ecoact.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gère la session utilisateur
 * 
 * Stocke SEULEMENT l'ID de l'utilisateur connecté.
 * Les autres infos (email, nom) sont récupérées de la BD quand nécessaire.
 * 
 * Avantages:
 * - Une seule source de vérité (la BD)
 * - Toujours synchronisé
 * - Plus simple et plus léger
 * - Meilleure pratique mobile
 */
public class SessionManager {
    private static final String PREF_NAME = "ecoact_session";
    private static final String KEY_USER_ID = "user_id";  // SEULE clé stockée

    private SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Sauvegarder l'ID de l'utilisateur connecté
     * 
     * @param userId L'ID unique de l'utilisateur
     */
    public void saveUserSession(Long userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Récupérer l'ID de l'utilisateur connecté
     * 
     * @return L'ID de l'utilisateur, ou null si personne n'est connecté
     */
    public Long getCurrentUserId() {
        long id = sharedPreferences.getLong(KEY_USER_ID, -1);
        return id == -1 ? null : id;
    }

    /**
     * Vérifier si un utilisateur est connecté
     * 
     * @return true si quelqu'un est connecté, false sinon
     */
    public boolean isUserLoggedIn() {
        return sharedPreferences.getLong(KEY_USER_ID, -1) != -1;
    }

    /**
     * Déconnexion - efface la session
     * 
     * Les données utilisateur dans la BD ne sont pas affectées,
     * seulement la session locale est supprimée.
     */
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}

