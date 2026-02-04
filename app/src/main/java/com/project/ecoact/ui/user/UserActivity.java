package com.project.ecoact.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.project.ecoact.R;
import com.project.ecoact.data.dao.UserDao;
import com.project.ecoact.data.entity.user.User;

import java.util.List;

public class UserActivity extends AppCompatActivity {
    private EditText lastNameId, firstNameId;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_main);

        lastNameId = findViewById(R.id.last_name_id);
        firstNameId = findViewById(R.id.first_name_id);
        Button addButtonId = findViewById(R.id.add_button_id);

        addButtonId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = lastNameId.getText().toString().trim();
                String lastName = firstNameId.getText().toString().trim();

                User toCreate = new User();
                toCreate.setFirstName(firstName);
                toCreate.setLastName(lastName);

                userDao.insertAll(toCreate);

                User user = userDao.getAll().get(0);

                if(user.getFirstName().isEmpty() || user.getLastName().isEmpty()) {
                    Toast.makeText(UserActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserActivity.this, "First name: " + firstName + "\nLast Name: " + lastName, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
