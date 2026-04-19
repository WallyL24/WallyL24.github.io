package com.zybooks.weighttrackingapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_activity);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Enhancement: retrieve the logged-in username so fragments can load user-specific data
        username = getIntent().getStringExtra("username");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MainScreenFragment.newInstance(username))
                    .commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = MainScreenFragment.newInstance(username);
            } else if (id == R.id.nav_entries) {
                selectedFragment = AddWeightFragment.newInstance(username);
            } else if (id == R.id.nav_goal) {
                selectedFragment = GoalWeightFragment.newInstance(username);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });
    }
}


