package com.project.ecoact.util;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitaire pour la gestion des mots de passe
 * IMPORTANT: Utiliser BCrypt en production pour plus de sécurité
 */
public class PasswordUtils {

    private static final String TAG = "PasswordUtils";

    /**
     * Hash un mot de passe avec SHA-256
     */
    public static String hashPassword(String password, String salt) {
        try {
            String input = password + salt;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Erreur lors du hachage du mot de passe", e);
            return null;
        }
    }

    /**
     * Génère un salt aléatoire
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Valide les critères d'un mot de passe sécurisé
     */
    public static boolean isValidPassword(String password) {
        // Minimum 6 caractères (adapté pour une app mobile)
        if (password.length() < 6) {
            return false;
        }
        return true;
    }

    /**
     * Valide un format d'email basique
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Encode le mot de passe avec salt (format: salt$hash)
     */
    public static String encodePassword(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return salt + "$" + hash;
    }

    /**
     * Vérifie si un mot de passe correspond au hash stocké
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        try {
            String[] parts = storedPassword.split("\\$");
            if (parts.length != 2) {
                return false;
            }
            String salt = parts[0];
            String hash = parts[1];
            String providedHash = hashPassword(password, salt);
            return hash.equals(providedHash);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la vérification du mot de passe", e);
            return false;
        }
    }
}

