package com.zybooks.weighttrackingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "AppDB";
    private static final int DB_VERSION = 1;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String USERS_COL_USERNAME = "username";
    private static final String USERS_COL_PASSWORD = "password";

    // Weight log table
    private static final String TABLE_WEIGHT_LOG = "weight_log";

    // Goal weight table
    private static final String TABLE_GOALS = "user_goals";

    public UserDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                USERS_COL_USERNAME + " TEXT PRIMARY KEY, " +
                USERS_COL_PASSWORD + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_WEIGHT_LOG + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "weight REAL, " +
                "date TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_GOALS + " (" +
                "username TEXT PRIMARY KEY, " +
                "goalWeight REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERS_COL_USERNAME, username);
        values.put(USERS_COL_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values) != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                USERS_COL_USERNAME + "=? AND " + USERS_COL_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
        return cursor.getCount() > 0;
    }

    public boolean checkUserName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                USERS_COL_USERNAME + "=?", new String[]{username}, null, null, null);
        return cursor.getCount() > 0;
    }

    // Insert weight entry
    public boolean insertWeight(String username, double weight, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("weight", weight);
        values.put("date", date);
        return db.insert(TABLE_WEIGHT_LOG, null, values) != -1;
    }

    public double getFirstWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT weight FROM weight_log WHERE username = ? ORDER BY date ASC LIMIT 1", new String[]{username});

        if (cursor.moveToFirst()) {
            double weight = cursor.getDouble(0);
            cursor.close();
            return weight;
        }

        cursor.close();
        return 0.0;
    }
    public double getLatestWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT weight FROM weight_log WHERE username = ? ORDER BY date DESC LIMIT 1", new String[]{username});

        if (cursor.moveToFirst()) {
            double weight = cursor.getDouble(0);
            cursor.close();
            return weight;
        }

        cursor.close();
        return 0.0;
    }

    // Get all weight entries
    public Cursor getAllWeights(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_WEIGHT_LOG + " WHERE username=?", new String[]{username});
    }

    // Set or update goal weight
    public boolean updateGoalWeight(String username, double goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("goalWeight", goalWeight);

        int updated = db.update(TABLE_GOALS, values, "username=?", new String[]{username});
        if (updated == 0) {
            values.put("username", username);
            return db.insert(TABLE_GOALS, null, values) != -1;
        }
        return true;
    }

    // Get current goal weight
    public double getGoalWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT goalWeight FROM " + TABLE_GOALS + " WHERE username=?",
                new String[]{username});
        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0.0;
    }

    //delete a weight entry
    public boolean deleteWeightEntry(int id){
        return getWritableDatabase().delete("weight_log","id=?",new String[]{String.valueOf(id)})>0;
    }
}
