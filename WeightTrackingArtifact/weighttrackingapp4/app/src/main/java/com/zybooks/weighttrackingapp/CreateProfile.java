package com.zybooks.weighttrackingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateProfile extends AppCompatActivity {
    private EditText mEnterPassword;
    private EditText mEnterUsername;

    Button mRegister;

    private UserDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        mEnterUsername = findViewById(R.id.enter_username);
        mEnterPassword = findViewById(R.id.enter_password);
        mRegister = findViewById(R.id.buttonLogIn);
        db = new UserDatabase(this);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = mEnterUsername.getText().toString();
                String pass = mEnterPassword.getText().toString();

                if(user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(CreateProfile.this, "enter valid username and password", Toast.LENGTH_SHORT).show();
                }
                else {
                    boolean checkUser = db.checkUser(user, pass);
                    if (!checkUser) {
                        Boolean insert = db.addUser(user,pass);
                        if (insert) {
                            Toast.makeText(CreateProfile.this, "Account registered successfully", Toast.LENGTH_SHORT).show();
                            Intent login = new Intent(getApplicationContext(), NavigationActivity.class);
                            startActivity(login);
                        }
                        else {
                            Toast.makeText(CreateProfile.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(CreateProfile.this, "User already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });


    }


}
