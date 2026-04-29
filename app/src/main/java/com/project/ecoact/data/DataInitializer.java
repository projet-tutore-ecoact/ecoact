package com.project.ecoact.data;

import android.content.Context;

import com.project.ecoact.data.dao.EnergyDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.EnergyEntity;

public class DataInitializer {

    public static void initData(Context context) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            EnergyDao dao = db.energyDao();

            // Vérifier si des données existent déjà
            if (dao.getAllByPeriod("week").isEmpty()) {

                // Données SEMAINE
                EnergyEntity elecWeek = new EnergyEntity();
                elecWeek.type = "electricite";
                elecWeek.value = 105;
                elecWeek.period = "week";
                elecWeek.trend = 8;
                dao.insert(elecWeek);

                EnergyEntity gasWeek = new EnergyEntity();
                gasWeek.type = "gaz";
                gasWeek.value = 57;
                gasWeek.period = "week";
                gasWeek.trend = 5;
                dao.insert(gasWeek);

                EnergyEntity waterWeek = new EnergyEntity();
                waterWeek.type = "eau";
                waterWeek.value = 41;
                waterWeek.period = "week";
                waterWeek.trend = -8;
                dao.insert(waterWeek);

                // Données MOIS
                EnergyEntity elecMonth = new EnergyEntity();
                elecMonth.type = "electricite";
                elecMonth.value = 420;
                elecMonth.period = "month";
                elecMonth.trend = 6;
                dao.insert(elecMonth);

                EnergyEntity gasMonth = new EnergyEntity();
                gasMonth.type = "gaz";
                gasMonth.value = 228;
                gasMonth.period = "month";
                gasMonth.trend = 3;
                dao.insert(gasMonth);

                EnergyEntity waterMonth = new EnergyEntity();
                waterMonth.type = "eau";
                waterMonth.value = 164;
                waterMonth.period = "month";
                waterMonth.trend = -5;
                dao.insert(waterMonth);
            }
        }).start();
    }
}