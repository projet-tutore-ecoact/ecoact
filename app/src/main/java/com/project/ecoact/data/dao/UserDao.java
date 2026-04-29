package com.project.ecoact.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.project.ecoact.data.entity.EnergyEntity;
import com.project.ecoact.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    List<User> getAllById(long[] userIds);

    @Query("SELECT * FROM users WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserByIdSync(Long id);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<User> getUserById(Long id);

    @Insert
    long insert(User user);

    @Insert
    void insertAll(User... users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(Long id);
}