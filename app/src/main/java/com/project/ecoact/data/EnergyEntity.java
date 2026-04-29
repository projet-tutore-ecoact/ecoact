package com.project.ecoact.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "energy_data")
public class EnergyEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String type;
    public double value;
    public String period;
    public double trend;
}