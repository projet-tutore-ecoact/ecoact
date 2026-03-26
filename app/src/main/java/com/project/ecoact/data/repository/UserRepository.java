package com.project.ecoact.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.project.ecoact.data.dao.UserDao;
import com.project.ecoact.data.databaseConf.AppDatabase;
import com.project.ecoact.data.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserRepository(Application application){
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
    }

    public LiveData<List<User>> getAll(){
        return userDao.getAll();
    }

    public void insert(User... user) {
        executor.execute(() -> userDao.insertAll(user));
    }

}
