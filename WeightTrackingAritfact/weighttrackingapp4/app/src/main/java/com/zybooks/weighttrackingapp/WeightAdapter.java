package com.zybooks.weighttrackingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//recycler view to show weight entries
public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {

    //notify when a weight entry is deleted
    interface OnEntryDeletedListener {
        void onEntryDeleted();
    }

    private List<WeightEntry> entries;
    private UserDatabase db;
    private OnEntryDeletedListener listener;

    public WeightAdapter(List<WeightEntry> entries, UserDatabase db, OnEntryDeletedListener listener) {
        this.entries = entries;
        this.db = db;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeight, tvDate;
        Button btnDelete;

        public ViewHolder(View v) {
            super(v);
            tvWeight = v.findViewById(R.id.tvWeight);
            tvDate = v.findViewById(R.id.tvDate);
            btnDelete = v.findViewById(R.id.button_delete);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    //bind data to each row
    //if loop for the delete button when clicked
    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        WeightEntry e = entries.get(pos);
        holder.tvWeight.setText(String.valueOf(e.weight));
        holder.tvDate.setText(e.date);
        holder.btnDelete.setOnClickListener(v -> {
            if (db.deleteWeightEntry(e.id)) {
                entries.remove(pos);
                notifyItemRemoved(pos);
                listener.onEntryDeleted();
            }
        });
    }

    //total item count is returned
    @Override
    public int getItemCount() {
        return entries.size();
    }
}
