package com.project.ecoact.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.ecoact.data.entity.EnergyEntity;

import java.util.List;

@Dao
public interface EnergyDao {

    // Insérer une nouvelle donnée d'énergie dans la table
    @Insert
    void insert(EnergyEntity data);

    // Séléctionner une donnée à partir de son type et d'une période donnée
    @Query("SELECT * FROM energy_data WHERE type = :type AND period = :period")
    EnergyEntity getByTypeAndPeriod(String type, String period);

    // Seléctonner unne donnée à partir d'une période
    @Query("SELECT * FROM energy_data WHERE period = :period")
    List<EnergyEntity> getAllByPeriod(String period);
}