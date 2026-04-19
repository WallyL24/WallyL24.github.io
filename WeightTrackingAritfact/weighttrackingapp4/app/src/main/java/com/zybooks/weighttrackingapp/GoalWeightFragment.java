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
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goal_weight, container, false);

        enterGoal = view.findViewById(R.id.enterGoal);
        saveGoal = view.findViewById(R.id.saveGoal);
        db = new UserDatabase(requireContext());

        //method to use button to add a goal weight
        //if loops are used to save weight inside a try and catch method
        saveGoal.setOnClickListener(v -> {
            String goalStr = enterGoal.getText().toString().trim();
            if (goalStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a goal weight", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double goalWeight = Double.parseDouble(goalStr);
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