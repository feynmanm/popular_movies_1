package com.snakewind.movies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.snakewind.movies.data.FavoritesContract.MovieEntry;

/**
 * Created by Michael on 2/21/2016.
 */
public class FavoritesProvider extends ContentProvider {
    //private static final String LOG_TAG = FavoritesProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoritesDBHelper mDbHelper;

    private static final int MOVIES = 100;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoritesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieEntry.TABLE_NAME, MOVIES);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new FavoritesDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return MovieEntry.CONTENT_DIR_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri returnUri;
        long _id = -1;

        switch (sUriMatcher.match(uri)) {
            case (MOVIES): {
                _id = db.insert(MovieEntry.TABLE_NAME, null, values);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown URI on insert: " + uri);
            }
        }
        if ( _id > 0 ) {
            returnUri = MovieEntry.buildFavoritesUri(Long.toString(_id));
        } else {
            throw new android.database.SQLException("Failed to insert row into: " + uri);
        }
        db.close();
        return returnUri;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor returnedCursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                returnedCursor = mDbHelper.getReadableDatabase().query(
                        MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        return returnedCursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                count = db.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        db.close();
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case MOVIES: {
                count = db.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        db.close();
        return count;
    }
}
