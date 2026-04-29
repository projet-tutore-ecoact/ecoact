package com.project.ecoact.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {EnergyEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EnergyDao energyDao();

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppDatabase.class, "ecoact_db")
                    .build();
        }
        return instance;
    }
}