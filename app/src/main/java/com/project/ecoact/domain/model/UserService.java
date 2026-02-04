package com.project.ecoact.domain.model;

import android.content.Context;

import com.project.ecoact.data.dao.UserDao;
import com.project.ecoact.data.database.AppDatabase;
import com.project.ecoact.data.entity.user.User;

import java.util.List;

public class UserService {

    private final UserDao userDao;

    public UserService(Context context) {
        userDao = AppDatabase.getInstance(context).userDao();
    }

    public void insert(User... user) {
        userDao.insertAll(user);
    }

    public List<User> getAll() {
        return userDao.getAll();
    }
}
