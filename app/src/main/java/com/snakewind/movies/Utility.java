/*
 * Copyright (C) 2016 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.snakewind.movies.data.FavoritesContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Few utility static methods.
 */
public class Utility {

    final static String LOG_TAG = Utility.class.getSimpleName();

    /** Returns a 4 digit year from a raw data*/
    public static String getReleaseYear(String rawDate) {
        return rawDate.substring(0, 4);
    }

    /** Returns a specific value from a JSON Object given a resource ID for the key*/
    public static String getStringFromJson(Context context, JSONObject jsonObject, int keyResId) {
        String returnString = "";
        try {
            returnString = jsonObject.getString(context.getString(keyResId));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json exception", e);
        }
        return returnString;
    }

    /** Given a string representation of a URL, returns the string returned from that location*/
    public static String getStringFromWeb(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            URL queryUrl = new URL(urlString);
            connection = (HttpURLConnection) queryUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream stream = connection.getInputStream();
            if (stream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(stream));
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            if (builder.length() == 0) {
                return null;
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL when fetching data", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "problem connecting to URL", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return builder.toString();
    }

    // Is this movie favorited?
    static boolean isThisMovieFavorited(final Context context, String id) {
        String selection = FavoritesContract.MovieEntry._ID + " =?";
        String[] selectionArgs = {id};
        boolean isFavorited = false;
        Cursor cursor = context.getContentResolver().query(
                FavoritesContract.MovieEntry.CONTENT_URI,
                        null,
                        selection,
                        selectionArgs,
                        null);
        if (cursor != null) {
            isFavorited = cursor.moveToFirst();
            cursor.close();
        }else {
            Log.e(LOG_TAG, "Error fetching isFavorited data: query returned null");
        }
        return isFavorited;
    }
}