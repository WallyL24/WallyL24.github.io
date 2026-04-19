package com.zybooks.weighttrackingapp;


import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AddWeightFragment extends Fragment {

    private EditText weightEntryEditText;
    private Button addWeightButton;
    private UserDatabase db;
    private String username = "testUser";
    private RecyclerView recyclerView;
    private WeightAdapter adapter;
    private List<WeightEntry> entries = new ArrayList<>();

    public AddWeightFragment() {
        // Required empty public constructor
    }

    //method to create instance using username
    public static AddWeightFragment newInstance(String username) {
        AddWeightFragment fragment = new AddWeightFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new UserDatabase(requireContext());

        //return username from arguments
        if (getArguments() != null) {
            username = getArguments().getString("username");
            Log.d("AddWeightFragment", "Username from bundle: " + username);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_weight, container, false);

        weightEntryEditText = view.findViewById(R.id.weightEntry);
        addWeightButton = view.findViewById(R.id.button); // ID for your "Add Weight" button
        recyclerView = view.findViewById(R.id.recycler_view);

        //recycler view is set up
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WeightAdapter(entries, db, this::loadEntries);
        recyclerView.setAdapter(adapter);

        db = new UserDatabase(requireContext());

        //weight is added on click
        //if loop is used to validate information
        addWeightButton.setOnClickListener(new View.OnClickListener() {
            String weightStr = weightEntryEditText.getText().toString().trim();

            // Enhancement: validate that the field is not empty
            if (TextUtils.isEmpty(weightStr)) {
                Toast.makeText(getContext(), "Please enter a weight", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double weight = Double.parseDouble(weightStr);

                // Enhancement: prevent invalid weight values from being stored
                if (weight <= 0) {
                    Toast.makeText(getContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show();
                    return;
                }

                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                boolean inserted = db.insertWeight(username, weight, date);
                if (inserted) {
                    Toast.makeText(getContext(), "Weight logged!", Toast.LENGTH_SHORT).show();
                    weightEntryEditText.setText("");
                    loadEntries();

                    // Enhancement: notify other fragments that weight data has changed
                    Bundle result = new Bundle();
                    result.putBoolean("weight_updated", true);
                    getParentFragmentManager().setFragmentResult("weight_update_key", result);
                } else {
                    Toast.makeText(getContext(), "Failed to log weight", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Enter a valid numeric weight", Toast.LENGTH_SHORT).show();
            }
    

        loadEntries();

        return view;
    }

    //methos to load all weights in recycler view
    private void loadEntries() {
        entries.clear();
        Cursor c = db.getAllWeights(username);
        while (c.moveToNext()) {
            entries.add(new WeightEntry(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getDouble(c.getColumnIndexOrThrow("weight")),
                    c.getString(c.getColumnIndexOrThrow("date"))
            ));
        }

        // Enhancement: close the cursor after use to prevent resource leaks
        c.close();
        adapter.notifyDataSetChanged();   //UI is updated
    }

    }