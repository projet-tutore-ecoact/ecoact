package com.project.ecoact.data.repository;

import android.app.Application;

import com.project.ecoact.data.dao.HabitDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.HabitEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// Repository qui gere les habitudes utilisateur.
public class HabitRepository {
    // DAO utilise pour acceder aux habitudes en base de donnees.
    private final HabitDao habitDao;

    // Recupere la base de donnees et initialise le DAO.
    public HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        habitDao = db.habitDao();
    }

    // Recupere toutes les habitudes d'un utilisateur.
    public List<HabitEntity> getHabitsByUserSync(long userId) {
        // Stocke le resultat venant du thread.
        AtomicReference<List<HabitEntity>> habits = new AtomicReference<>();
        // Lance la requete dans un thread separe.
        Thread thread = new Thread(() -> habits.set(habitDao.getHabitsByUser(userId)));
        thread.start();
        try {
            // Attend la fin de la requete.
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return habits.get();
    }

    // Recupere une habitude precise avec sa cle.
    public HabitEntity getHabitByKeySync(long userId, String habitKey) {
        // Stocke l'habitude trouvee.
        AtomicReference<HabitEntity> habit = new AtomicReference<>();
        // Lance la recherche dans un thread separe.
        Thread thread = new Thread(() -> habit.set(habitDao.getHabitByKey(userId, habitKey)));
        thread.start();
        try {
            // Attend la fin de la recherche.
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return habit.get();
    }

    // Ajoute une nouvelle habitude.
    public long insertHabit(HabitEntity habit) throws InterruptedException {
        // Stocke l'id genere apres insertion.
        final long[] habitId = new long[1];
        // Insere l'habitude dans un thread separe.
        Thread thread = new Thread(() -> habitId[0] = habitDao.insert(habit));
        thread.start();
        thread.join();
        return habitId[0];
    }

    // Supprime une habitude avec son id et l'id utilisateur.
    public void deleteHabitById(long id, long userId) throws InterruptedException {
        // Lance la suppression dans un thread separe.
        Thread thread = new Thread(() -> habitDao.deleteById(id, userId));
        thread.start();
        thread.join();
    }

    // Cree un resume simple des habitudes d'un utilisateur.
    public String getHabitsSummary(Long userId) {
    List<HabitEntity> habits = getHabitsByUserSync(userId);
    if (habits == null || habits.isEmpty()) {
        return "Aucune habitude enregistrée";
    }
    // Construit le texte du resume.
    StringBuilder summary = new StringBuilder();
    for (HabitEntity habit : habits) {
        summary.append(habit.getQuestion())
               .append(" (")
               .append(habit.getFrequency())
               .append("), ");
    }
    return summary.toString();
}
}
