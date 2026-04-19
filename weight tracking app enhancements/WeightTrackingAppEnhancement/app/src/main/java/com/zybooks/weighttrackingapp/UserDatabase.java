package com.zybooks.weighttrackingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Database helper class used to manage users, weight logs, and goal weights
public class UserDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "AppDB";
    private static final int DB_VERSION = 2; // Increased version to reflect enhancements

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String USERS_COL_USERNAME = "username";
    private static final String USERS_COL_PASSWORD = "password";

    // Weight log table
    private static final String TABLE_WEIGHT_LOG = "weight_log";
    private static final String WEIGHT_COL_ID = "id";
    private static final String WEIGHT_COL_USERNAME = "username";
    private static final String WEIGHT_COL_WEIGHT = "weight";
    private static final String WEIGHT_COL_DATE = "date";

    // Goal weight table
    private static final String TABLE_GOALS = "user_goals";
    private static final String GOAL_COL_USERNAME = "username";
    private static final String GOAL_COL_WEIGHT = "goalWeight";

    public UserDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creates a table for storing user login information
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                USERS_COL_USERNAME + " TEXT PRIMARY KEY, " +
                USERS_COL_PASSWORD + " TEXT NOT NULL)");

        // Creates a table for storing each user's weight history
        db.execSQL("CREATE TABLE " + TABLE_WEIGHT_LOG + " (" +
                WEIGHT_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WEIGHT_COL_USERNAME + " TEXT NOT NULL, " +
                WEIGHT_COL_WEIGHT + " REAL NOT NULL, " +
                WEIGHT_COL_DATE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + WEIGHT_COL_USERNAME + ") REFERENCES " +
                TABLE_USERS + "(" + USERS_COL_USERNAME + "))");

        // Creates a table for storing each user's goal weight
        db.execSQL("CREATE TABLE " + TABLE_GOALS + " (" +
                GOAL_COL_USERNAME + " TEXT PRIMARY KEY, " +
                GOAL_COL_WEIGHT + " REAL NOT NULL, " +
                "FOREIGN KEY(" + GOAL_COL_USERNAME + ") REFERENCES " +
                TABLE_USERS + "(" + USERS_COL_USERNAME + "))");

        // Added index to improve retrieval speed when loading a user's weight history
        db.execSQL("CREATE INDEX idx_weight_user_date ON " + TABLE_WEIGHT_LOG +
                "(" + WEIGHT_COL_USERNAME + ", " + WEIGHT_COL_DATE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Drops old tables if the schema changes and recreates them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Adds a new user to the users table
    public boolean addUser(String username, String password) {
        // Validation added to prevent empty usernames or passwords
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERS_COL_USERNAME, username.trim());
        values.put(USERS_COL_PASSWORD, password.trim());

        return db.insert(TABLE_USERS, null, values) != -1;
    }

    // Checks whether the username and password match an existing user
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                USERS_COL_USERNAME + "=? AND " + USERS_COL_PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Checks whether a username already exists
    public boolean checkUserName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                USERS_COL_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Inserts a weight entry into the weight log table
    public boolean insertWeight(String username, double weight, String date) {
        // Validation added to improve data integrity
        if (username == null || username.trim().isEmpty() || weight <= 0 || date == null || date.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WEIGHT_COL_USERNAME, username.trim());
        values.put(WEIGHT_COL_WEIGHT, weight);
        values.put(WEIGHT_COL_DATE, date);

        return db.insert(TABLE_WEIGHT_LOG, null, values) != -1;
    }

    // Retrieves the first recorded weight for a user
    public double getFirstWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + WEIGHT_COL_WEIGHT + " FROM " + TABLE_WEIGHT_LOG +
                        " WHERE " + WEIGHT_COL_USERNAME + "=? ORDER BY " + WEIGHT_COL_DATE + " ASC LIMIT 1",
                new String[]{username}
        );

        double weight = 0.0;
        if (cursor.moveToFirst()) {
            weight = cursor.getDouble(0);
        }
        cursor.close();
        return weight;
    }

    // Retrieves the latest recorded weight for a user
    public double getLatestWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + WEIGHT_COL_WEIGHT + " FROM " + TABLE_WEIGHT_LOG +
                        " WHERE " + WEIGHT_COL_USERNAME + "=? ORDER BY " + WEIGHT_COL_DATE + " DESC LIMIT 1",
                new String[]{username}
        );

        double weight = 0.0;
        if (cursor.moveToFirst()) {
            weight = cursor.getDouble(0);
        }
        cursor.close();
        return weight;
    }

    // Retrieves all weight entries for a user in reverse chronological order
    public Cursor getAllWeights(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_WEIGHT_LOG +
                        " WHERE " + WEIGHT_COL_USERNAME + "=? ORDER BY " + WEIGHT_COL_DATE + " DESC",
                new String[]{username}
        );
    }

    // Inserts or updates a user's goal weight
    public boolean updateGoalWeight(String username, double goalWeight) {
        // Validation added to prevent invalid goal entries
        if (username == null || username.trim().isEmpty() || goalWeight <= 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GOAL_COL_WEIGHT, goalWeight);

        int updated = db.update(TABLE_GOALS, values, GOAL_COL_USERNAME + "=?", new String[]{username});
        if (updated == 0) {
            values.put(GOAL_COL_USERNAME, username);
            return db.insert(TABLE_GOALS, null, values) != -1;
        }
        return true;
    }

    // Retrieves a user's goal weight
    public double getGoalWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + GOAL_COL_WEIGHT + " FROM " + TABLE_GOALS +
                        " WHERE " + GOAL_COL_USERNAME + "=?",
                new String[]{username}
        );

        double goal = 0.0;
        if (cursor.moveToFirst()) {
            goal = cursor.getDouble(0);
        }
        cursor.close();
        return goal;
    }

    // Deletes a specific weight entry by ID
    public boolean deleteWeightEntry(int id) {
        return getWritableDatabase().delete(TABLE_WEIGHT_LOG, WEIGHT_COL_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }
}
