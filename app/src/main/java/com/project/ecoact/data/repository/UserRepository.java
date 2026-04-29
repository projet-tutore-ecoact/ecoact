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

    public LiveData<User> getUserById(Long id) {
        return userDao.getUserById(id);
    }

    public User getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    /**
     * Récupérer un utilisateur par ID (synchrone)
     * Utile pour la session et ProfileFragment
     */
    public User getUserByIdSync(Long id) {
        final User[] user = new User[1];
        Thread thread = new Thread(() -> user[0] = userDao.getUserByIdSync(id));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user[0];
    }

    public long insertUser(User user) throws InterruptedException {
        final long[] userId = new long[1];
        Thread thread = new Thread(() -> userId[0] = userDao.insert(user));
        thread.start();
        thread.join();
        return userId[0];
    }

    public void insert(User... user) {
        executor.execute(() -> userDao.insertAll(user));
    }

    public void update(User user) {
        executor.execute(() -> userDao.update(user));
    }

    public void delete(User user) {
        executor.execute(() -> userDao.delete(user));
    }
}
