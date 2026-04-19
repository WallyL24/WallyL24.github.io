package com.zybooks.weighttrackingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainScreenFragment extends Fragment {
    private UserDatabase db;
    private String username = "testUser";

    private TextView mCurrentWeight;
    private TextView mGoalWeight;
    private ProgressBar mProgress;
    private static final int SMS_PERMISSION_CODE = 1001;
    private boolean hasSmsPermission = false;
    private boolean goalReachedNotified = false;  // Prevent repeat SMS notifications

    public MainScreenFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_screen, container, false);
        mCurrentWeight = view.findViewById(R.id.currentWeight1);
        mGoalWeight = view.findViewById(R.id.goalWeight1);
        mProgress = view.findViewById(R.id.simpleProgressBar1);

        db = new UserDatabase(requireContext());

        checkSmsPermission();  // Ask for permission


        //updates from AddWeightFragment
        getParentFragmentManager().setFragmentResultListener("weight_update_key", this, (key, bundle) -> {
            boolean updated = bundle.getBoolean("weight_updated", false);
            if (updated) {
                updateProgressUI();
            }
        });


        updateProgressUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgressUI();
    }

    //sms methods to send message to user if allowed
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            hasSmsPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasSmsPermission = true;
                Toast.makeText(getContext(), "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                hasSmsPermission = false;
                Toast.makeText(getContext(), "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //update UI to show current weight and goal weight and starting weight for progress bar
    private void updateProgressUI() {
        double startWeight = db.getFirstWeight(username);
        double currentWeight = db.getLatestWeight(username);
        double goalWeight = db.getGoalWeight(username);

        //if loop that increases or decreases the prgress bar
        if (goalWeight > 0 && startWeight > 0) {
            double progressPercent = 0;

            if (startWeight > goalWeight) {
                // Weight loss
                progressPercent = ((startWeight - currentWeight) / (startWeight - goalWeight)) * 100;
            } else if (startWeight < goalWeight) {
                // Weight gain
                progressPercent = ((currentWeight - startWeight) / (goalWeight - startWeight)) * 100;
            } else {
                progressPercent = 100;  // Already at goal
            }

            progressPercent = Math.max(0, Math.min(progressPercent, 100)); // Clamp between 0-100
            int progress = (int) progressPercent;

            mProgress.setProgress(progress);

            //progress bar is color coated using three colors
            //if loop is used for different levels
            int progressColor;
            if (progress < 33) {
                progressColor = ContextCompat.getColor(requireContext(), R.color.red);
            } else if (progress < 66) {
                progressColor = ContextCompat.getColor(requireContext(), R.color.orange);
            } else {
                progressColor = ContextCompat.getColor(requireContext(), R.color.green);
            }

            mProgress.getProgressDrawable().setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN);

            mCurrentWeight.setText("Current Weight: " + currentWeight);
            mGoalWeight.setText("Goal Weight: " + goalWeight);

            if (Math.abs(currentWeight - goalWeight) < 0.01 && hasSmsPermission && !goalReachedNotified) {
                sendGoalReachedSMS();
                goalReachedNotified = true;  // Avoid duplicate SMS
            }
        } else {

            mProgress.setProgress(0);
        }
    }

    //sends a message when goal is met
    //try and catch is used to send sms message
    private void sendGoalReachedSMS() {
        String phoneNumber = "5551234567";  // Replace with user’s number or store in preferences
        String message = "Congratulations! You've reached your goal weight!";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "Goal reached! SMS sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}