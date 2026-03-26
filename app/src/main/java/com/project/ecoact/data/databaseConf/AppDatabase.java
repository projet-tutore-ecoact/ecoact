package com.project.ecoact.data.databaseConf;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.project.ecoact.data.dao.UserDao;
import com.project.ecoact.data.entity.User;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getInstance(Context context){
        if (INSTANCE==null){
            synchronized (AppDatabase.class){
                if (INSTANCE==null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,"ecoact_db").build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();
}