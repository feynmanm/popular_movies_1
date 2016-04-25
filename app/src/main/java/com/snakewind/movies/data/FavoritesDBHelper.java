package com.snakewind.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.snakewind.movies.data.FavoritesContract.MovieEntry;

/**
 * Created by Michael on 2/21/2016.
 */
public class FavoritesDBHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = FavoritesDBHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "favorites.db";
    public static final int DATABASE_VERSION = 4;

    public FavoritesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE "
                + MovieEntry.TABLE_NAME + " ("
                + MovieEntry._ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, "
                + MovieEntry.COLUMN_DISCOVER_DATA + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_SPECIFIC_DATA + " TEXT NOT NULL, "
                + MovieEntry.COLUMN_TRAILERS_DATA + " TEXT NULL, "
                + MovieEntry.COLUMN_REVIEWS_DATA + " TEXT NULL);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    //This needs to be re-implemented for production to preserve data
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " +
                newVersion + ". OLD DATA WILL BE DESTROYED");

        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.OLD_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
    }
}
