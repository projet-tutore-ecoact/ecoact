package com.project.ecoact.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.project.ecoact.data.entity.User;
import com.project.ecoact.data.repository.UserRepository;

import java.util.List;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final LiveData<List<User>> users;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        users = userRepository.getAll();
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public void insert(User... users) {
        userRepository.insert(users);
    }
}