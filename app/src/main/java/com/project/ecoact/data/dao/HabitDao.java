// Gère les accès à la table Habit
package com.project.ecoact.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.ecoact.data.entity.HabitEntity;

import java.util.List;

@Dao
public interface HabitDao {

    // Récupère toutes les habitudes d'un utilisateur donné.
    // Les résultats sont triés par catégorie dans l'ordre alphabétique,
    // puis par date de mise à jour, de la plus récente à la plus ancienne.
    @Query("SELECT * FROM user_habits WHERE user_id = :userId ORDER BY category ASC, updated_at DESC")
    List<HabitEntity> getHabitsByUser(long userId);

    // Récupère une unique habitude identifiée par son id d'un utilisateur donné
    @Query("SELECT * FROM user_habits WHERE user_id = :userId AND habit_key = :habitKey LIMIT 1")
    HabitEntity getHabitByKey(long userId, String habitKey);

    // Insérer une habitude et retourner son id
    @Insert
    long insert(HabitEntity habit);

    // Supprimer une habitude
    @Delete
    void delete(HabitEntity habit);

    // Supprimer une habitude appartenant ç un utilisateur donné
    @Query("DELETE FROM user_habits WHERE id = :id AND user_id = :userId")
    void deleteById(long id, long userId);
}
