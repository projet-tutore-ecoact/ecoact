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
    @Query("SELECT * FROM devices WHERE user_id = :userId ORDER BY updated_at DESC")
    List<DeviceEntity> getDevicesByUser(long userId);

    @Query("SELECT * FROM devices WHERE id = :id AND user_id = :userId LIMIT 1")
    DeviceEntity getDeviceById(long id, long userId);

    @Insert
    long insert(DeviceEntity device);

    @Update
    void update(DeviceEntity device);

    @Delete
    void delete(DeviceEntity device);

    @Query("DELETE FROM devices WHERE id = :id AND user_id = :userId")
    void deleteById(long id, long userId);
}
