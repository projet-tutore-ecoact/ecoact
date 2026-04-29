package com.project.ecoact.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.ecoact.data.entity.EnergyEntity;

import java.util.List;

@Dao
public interface EnergyDao {
    @Insert
    void insert(EnergyEntity data);

    @Query("SELECT * FROM energy_data WHERE type = :type AND period = :period")
    EnergyEntity getByTypeAndPeriod(String type, String period);

    @Query("SELECT * FROM energy_data WHERE period = :period")
    List<EnergyEntity> getAllByPeriod(String period);
}