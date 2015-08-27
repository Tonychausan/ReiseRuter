package com.reise.ruter.SupportClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.reise.ruter.DataObjects.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tony Chau on 27/08/2015.
 */
public class ReiseRuterDbHelper extends SQLiteOpenHelper {
    // All Static variables
    // Database Version: If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    // DATATYPES
    private static final String DATATYPE_TEXT = "TEXT";
    private static final String DATATYPE_INT = "INTEGER";

    // Database Name
    private static final String DATABASE_NAME = "reiseRuterDatabase";

    // Favorites table name
    private static final String TABLE_FAVORITES = "favorites";

    // Favorite Table Columns names and methods
    public class TableFavorites {
        private static final String KEY_ID = "id";
        private static final String KEY_NAME = "name";
        private static final String KEY_DISTRICT = "district";

    }


    public ReiseRuterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + TableFavorites.KEY_ID + " " + DATATYPE_INT + " PRIMARY KEY," + TableFavorites.KEY_NAME + " " + DATATYPE_TEXT + ","
                + TableFavorites.KEY_DISTRICT + " " + DATATYPE_TEXT + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);

        // Create tables again
        onCreate(db);
    }


    //START FAVORITE TABLE
    public void addFavorite(Place place){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TableFavorites.KEY_ID, place.getId());
        values.put(TableFavorites.KEY_NAME, place.getName());
        values.put(TableFavorites.KEY_DISTRICT, place.getDistrict());

        // Inserting Row
        db.insert(TABLE_FAVORITES, null, values);
        db.close(); // Closing database connection
    }

    public void removeFavorit(int id){
    }

    public List<Place> getFavorites() {
        List<Place> favoriteList = new ArrayList<Place>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITES;

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // (ID, name, district, place type)
                Place place = new Place(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1),
                        cursor.getString(2),
                        null);
                // Adding place to list
                favoriteList.add(place);
            } while (cursor.moveToNext());
        }

        // return contact list
        return favoriteList;
    }
    //END FAVORITE TABLE


}
