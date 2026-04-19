package com.zybooks.weighttrackingapp;

//a weight entry for recycler view
public class WeightEntry {
    public int id;
    public double weight;
    public String date;
    public WeightEntry(int id, double weight, String date) {
        this.id = id;
        this.weight = weight;
        this.date = date;
    }
}
