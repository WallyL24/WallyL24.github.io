package com.zybooks.weighttrackingapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GoalWeightFragment extends Fragment {
    private EditText enterGoal;
    private Button saveGoal;
    private UserDatabase db;
    private String username = "testUser";

    public GoalWeightFragment() {
    }

    // Enhancement: allows the fragment to receive the logged-in username
    public static GoalWeightFragment newInstance(String username) {
        GoalWeightFragment fragment = new GoalWeightFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal_weight, container, false);

        enterGoal = view.findViewById(R.id.enterGoal);
        saveGoal = view.findViewById(R.id.saveGoal);
        db = new UserDatabase(requireContext());

        saveGoal.setOnClickListener(v -> {
            String goalStr = enterGoal.getText().toString().trim();

            // Enhancement: validate goal input before saving
            if (goalStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a goal weight", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double goalWeight = Double.parseDouble(goalStr);

                // Enhancement: prevent invalid goal values from being stored
                if (goalWeight <= 0) {
                    Toast.makeText(getContext(), "Please enter a valid goal weight", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean saved = db.updateGoalWeight(username, goalWeight);
                if (saved) {
                    Toast.makeText(getContext(), "Goal weight saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to save goal weight", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}