package com.project.ecoact.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.project.ecoact.data.entity.HabitEntity;

import java.util.List;

@Dao
public interface HabitDao {
    @Query("SELECT * FROM user_habits WHERE user_id = :userId ORDER BY category ASC, updated_at DESC")
    List<HabitEntity> getHabitsByUser(long userId);

    @Query("SELECT * FROM user_habits WHERE user_id = :userId AND habit_key = :habitKey LIMIT 1")
    HabitEntity getHabitByKey(long userId, String habitKey);

    @Insert
    long insert(HabitEntity habit);

    @Delete
    void delete(HabitEntity habit);

    @Query("DELETE FROM user_habits WHERE id = :id AND user_id = :userId")
    void deleteById(long id, long userId);
}
