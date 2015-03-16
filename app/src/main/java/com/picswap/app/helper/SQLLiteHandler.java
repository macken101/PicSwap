package com.picswap.app.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by mark on 13/03/2015.
 */
public class SQLLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLLiteHandler.class.getSimpleName();

    // All Static Variables
    // Database Version
    private static final int DB_VERSION = 1;

    // Database Name
    private static final String DB_NAME = "auleyai_macken";

    // Login table name
    private static final String TABLE_LOGIN = "login";

    //Login table column names
    private static final String KEY_ID = "id";
    private static final String _USER_ID = "user_id";
    private static final String _NAME = "username";
    private static final String _EMAIL = "email";
    private static final String _DATE_CREATED = "date_created";

    //Constructor
    public SQLLiteHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase database){
        // Create a login table
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + "INTEGER PRIMARY KEY," + _NAME + " TEXT," + _EMAIL + " TEXT UNIQUE, "
                + _USER_ID + " TEXT," + _DATE_CREATED + " TEXT" + ")";
        // Executing SQL Statement
        database.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // For upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase database, int previousDatabaseVersion, int newDatabaseVersion){
        //Drop older table if it existed
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);

        // Then create tables again.
        onCreate(database);
    }

    // Store User Information into the Database
    public void addUserToDB(String name, String email, String user_id, String date_created) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(_NAME, name);
        values.put(_EMAIL, email);
        values.put(_USER_ID, user_id);
        values.put(_DATE_CREATED, date_created);

        // Insert new Row to database
        long id = database.insert(TABLE_LOGIN, null, values);
        database.close();

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    //Getting User information from the Database
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_LOGIN;

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);

        //Move cursor to firstrow
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("username", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("user_id", cursor.getString(3));
            user.put("date_created", cursor.getString(4));
        }
        cursor.close();
        database.close();

        // Return user and log information
        Log.d(TAG, "Fetching User from SQLite: " + user.toString());

        return user;
    }

    /**
     *  Getting user login status
     *
     */
    public int getRowCount(){
        String countQuery = "SELECT * FROM " + TABLE_LOGIN;
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();

        database.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    // Delete all database tables and recreating them again
    public void deleteUsers(){
        SQLiteDatabase database = this.getWritableDatabase();

        // Delete Rows
        database.delete(TABLE_LOGIN, null, null);
        database.close();

        Log.d(TAG, "Deleted User Information from SQLite.");
    }
}