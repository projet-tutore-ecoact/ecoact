package com.project.ecoact.data.repository;

import android.app.Application;

import com.project.ecoact.data.dao.HabitDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.HabitEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HabitRepository {
    private final HabitDao habitDao;

    public HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        habitDao = db.habitDao();
    }

    public List<HabitEntity> getHabitsByUserSync(long userId) {
        AtomicReference<List<HabitEntity>> habits = new AtomicReference<>();
        Thread thread = new Thread(() -> habits.set(habitDao.getHabitsByUser(userId)));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return habits.get();
    }

    public HabitEntity getHabitByKeySync(long userId, String habitKey) {
        AtomicReference<HabitEntity> habit = new AtomicReference<>();
        Thread thread = new Thread(() -> habit.set(habitDao.getHabitByKey(userId, habitKey)));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return habit.get();
    }

    public long insertHabit(HabitEntity habit) throws InterruptedException {
        final long[] habitId = new long[1];
        Thread thread = new Thread(() -> habitId[0] = habitDao.insert(habit));
        thread.start();
        thread.join();
        return habitId[0];
    }

    public void deleteHabitById(long id, long userId) throws InterruptedException {
        Thread thread = new Thread(() -> habitDao.deleteById(id, userId));
        thread.start();
        thread.join();
    }
    public String getHabitsSummary(Long userId) {
    List<HabitEntity> habits = getHabitsByUserSync(userId);
    if (habits == null || habits.isEmpty()) {
        return "Aucune habitude enregistrée";
    }
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
