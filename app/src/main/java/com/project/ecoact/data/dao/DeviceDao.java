// Gère l'accès à la table Device
package com.project.ecoact.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.project.ecoact.data.entity.DeviceEntity;

import java.util.List;

@Dao
public interface DeviceDao {

    // Récupérer tous les appareils appartenant à un utilisateur
    // triés du plus récemment modifié au plus ancien
    @Query("SELECT * FROM devices WHERE user_id = :userId ORDER BY updated_at DESC")
    List<DeviceEntity> getDevicesByUser(long userId); // Méthode pour appliquer la requete au dessus

    // Récupérer un seul et unique appareil précis avec son id
    @Query("SELECT * FROM devices WHERE id = :id AND user_id = :userId LIMIT 1")
    DeviceEntity getDeviceById(long id, long userId);

    // Insère un nouvel appareil et retourne l'identifiant généré
    @Insert
    long insert(DeviceEntity device);

    // Met à jour les info d'un appareil existant
    @Update
    void update(DeviceEntity device);

    // Supprime un appareil de la base de données
    @Delete
    void delete(DeviceEntity device);

    // Supprime l'appareil appartenant à un utlisateur donné avec son id
    @Query("DELETE FROM devices WHERE id = :id AND user_id = :userId")
    void deleteById(long id, long userId);
}
