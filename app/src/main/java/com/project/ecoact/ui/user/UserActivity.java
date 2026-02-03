package com.project.ecoact.ui.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.project.ecoact.R;

public class UserActivity extends AppCompatActivity {
    private EditText lastNameId, firstNameId;

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
                String username = lastNameId.getText().toString().trim();
                String email = firstNameId.getText().toString().trim();

                if(username.isEmpty() || email.isEmpty()) {
                    Toast.makeText(UserActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserActivity.this, "Username: " + username + "\nEmail: " + email, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
