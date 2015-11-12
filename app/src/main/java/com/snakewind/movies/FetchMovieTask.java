/*
 * Copyright (C) 2015 Michael Reynolds
 */

package com.snakewind.movies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Fetches movie information from themoviedb.org
 */

public class FetchMovieTask extends AsyncTask<String, Void, JSONArray> {

    public final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    //set API_KEY to your API key issued from themoviedb.org
    public final static String API_KEY = "<insert API key here>";

    public final static String API_SCHEME = "http";
    public final static String API_AUTHORITY = "api.themoviedb.org";
    public final static String API_PATH_VERSION = "3";
    public final static String API_PATH_DISCOVER = "discover";
    public final static String API_PATH_MOVIE = "movie";
    public final static String API_SORT_NAME = "sort_by";
    public final static String API_KEY_NAME = "api_key";

    private HttpURLConnection mUrlConnection = null;
    private BufferedReader mReader = null;

    private final MovieAdapter mAdapter;

    public FetchMovieTask(MovieAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        Uri.Builder movieQueryBuilder = new Uri.Builder();
        movieQueryBuilder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_PATH_VERSION)
                .appendPath(API_PATH_DISCOVER)
                .appendPath(API_PATH_MOVIE)
                .appendQueryParameter(API_SORT_NAME, params[0])
                .appendQueryParameter(API_KEY_NAME, API_KEY)
                .build();

        String mRawMovieJson = null;

        try {
            URL movieQueryUrl = new URL(movieQueryBuilder.toString());
            mUrlConnection = (HttpURLConnection) movieQueryUrl.openConnection();
            mUrlConnection.setRequestMethod("GET");
            mUrlConnection.connect();

            InputStream inputStream = mUrlConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            mReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = mReader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }

            if (stringBuffer.length() == 0) {
                return null;
            }

            mRawMovieJson = stringBuffer.toString();

        } catch (Exception e) {
            Log.e("IO Error", LOG_TAG, e);
            return null;
        } finally {
            if (mUrlConnection != null) {
                mUrlConnection.disconnect();
            }
            if (mReader != null) {
                try {
                    mReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            JSONObject movieJson = new JSONObject(mRawMovieJson);
            return movieJson.getJSONArray("results");
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json exception", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONArray movieJson) {
        mAdapter.udpateMovies(movieJson);
    }
}
