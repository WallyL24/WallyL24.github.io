package com.zybooks.weighttrackingapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class logIn extends AppCompatActivity {

    private EditText mEnterPassword;
    private EditText mEnterUsername;
    private UserDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mEnterUsername = findViewById(R.id.enter_username);
        mEnterPassword = findViewById(R.id.enter_password);
        Button mLogin = findViewById(R.id.buttonLogIn);
        Button mRegister = findViewById(R.id.buttonCreateAccount);
        db = new UserDatabase(this);




        //click to login a user after they enter the required information
        //if loop is used to check the database for the required information
        //if correct intent is created to go to main screen
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mEnterUsername.getText().toString();
                String password = mEnterPassword.getText().toString();

                if(username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(logIn.this, "enter valid username and password", Toast.LENGTH_SHORT).show();
                }
                else {
                    boolean checkUser = db.checkUser(username, password);
                    if (checkUser) {
                        Toast.makeText(logIn.this, "Welcome", Toast.LENGTH_SHORT).show();
                        Intent login = new Intent(getApplicationContext(), NavigationActivity.class);

                        // Enhancement: pass the logged-in username so the app loads the correct user data
                        login.putExtra("username", username);

                        startActivity(login);
                    }
                    else {
                        Toast.makeText(logIn.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

        //click to send user to the create profile screen
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

    }

    //registered method with an intent to send user to create profile screen
    private void registerUser() {
        Intent registerIntent = new Intent(logIn.this, CreateProfile.class);
        startActivity(registerIntent);
        finish();
    }

}