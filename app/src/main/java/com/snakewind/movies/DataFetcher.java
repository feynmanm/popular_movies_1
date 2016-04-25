/*
 * Copyright (C) 2016 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.snakewind.movies.DetailActivity.DetailFragment;
import com.snakewind.movies.data.FavoritesContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class contains information and functions specific to themoviedb.org, particularly
 * involving calls to themoviedb.org or the Content Provider.
 */
public class DataFetcher {
    private final static String LOG_TAG = DataFetcher.class.getSimpleName();

    public final static String API_SCHEME = "http";
    public final static String API_AUTHORITY = "api.themoviedb.org";
    public final static String API_PATH_VERSION = "3";
    public final static String API_PATH_MOVIE = "movie";
    public final static String API_PATH_REVIEWS = "reviews";
    public final static String API_PATH_VIDEOS = "videos";
    public final static String API_KEY_NAME = "api_key";
    public final static String API_KEY = "XXX";

    private final static String IMAGE_SCHEME = "http";
    private final static String IMAGE_AUTHORITY = "image.tmdb.org";
    private final static String IMAGE_PATH1 = "t";
    private final static String IMAGE_PATH2 = "p";

    private static class FetchMovieDataTask extends AsyncTask<String, Void, String> {
        private DataSetter mDataSetter;

        FetchMovieDataTask(DataSetter dataSetter) {
            super();
            mDataSetter = dataSetter;
        }
        @Override
        protected String doInBackground(String... urls) {
            return Utility.getStringFromWeb(urls[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            mDataSetter.setData(result);
        }
    }

    private interface DataSetter {
        void setData(String text);
    }

    static void getAdditionalDetails(final DetailFragment fragment, String id, String sortType) {
        Context context = fragment.getContext();
        if (sortType.equals(context.getString(R.string.api_value_popularity)) ||
                sortType.equals(context.getString(R.string.api_value_rating))) {
            fetchSpecificMovieData(fragment, id);
            fetchVideosData(fragment, id);
            fetchReviewsData(fragment, id);
        } else if (sortType.equals(context.getString(R.string.action_showfavorites))) {
            String selection = MovieEntry._ID + " = ?";
            String[] selectionArgs = {id};
            Cursor cursor = context.getContentResolver()
                    .query(MovieEntry.CONTENT_URI,
                            null,
                            selection,
                            selectionArgs,
                            null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    JSONObject specificDataObject = null;
                    int specificIdx = cursor.getColumnIndex(MovieEntry.COLUMN_SPECIFIC_DATA);
                    JSONObject trailersDataObject = null;
                    int trailersIdx = cursor.getColumnIndex(MovieEntry.COLUMN_TRAILERS_DATA);
                    JSONObject reviewsDataObject = null;
                    int reviewsIdx = cursor.getColumnIndex(MovieEntry.COLUMN_REVIEWS_DATA);
                    try {
                        specificDataObject = new JSONObject(cursor.getString(specificIdx));
                        trailersDataObject = new JSONObject(cursor.getString(trailersIdx));
                        reviewsDataObject = new JSONObject(cursor.getString(reviewsIdx));
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error with JSON object when fetching movie-by-id data", e);
                    }
                    fragment.setSpecificData(specificDataObject);
                    fragment.setTrailersData(trailersDataObject);
                    fragment.setReviewsData(reviewsDataObject);
                }else {
                    Log.e(LOG_TAG, "Error fetching movie-by-id data: query returned empty cursor");
                }
                cursor.close();
            }else {
                Log.e(LOG_TAG, "Error fetching movie-by-id data: query returned null");
            }
        }
    }

    static void fetchSortedMoviesData(final MoviesFragment fragment, String sortType) {
        final Context context = fragment.getContext();
        if (sortType.equals(context.getString(R.string.api_value_popularity)) ||
                sortType.equals(context.getString(R.string.api_value_rating))) {
            new FetchMovieDataTask(new DataSetter() {
                @Override
                public void setData(String text) {
                    if (text.equals("")) {
                        Toast.makeText(context, context.getString(R.string.toast_no_internet),
                                Toast.LENGTH_SHORT).show();
                    }
                    try {
                        JSONObject object = new JSONObject(text);
                        JSONArray jsonArray = object.getJSONArray("results");
                        fragment.setMovieJson(jsonArray);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "Error with JSON object when fetching discover data", e);
                    }
                }
            }).execute(getSortedMoviesUrl(sortType));
        } else if (sortType.equals(context.getString(R.string.action_showfavorites))) {
            Cursor cursor = context.getContentResolver()
                    .query(MovieEntry.CONTENT_URI, null, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    JSONArray jsonArray = new JSONArray();
                    int index = cursor.getColumnIndex(MovieEntry.COLUMN_DISCOVER_DATA);
                    do {
                        try {
                            JSONObject object = new JSONObject(cursor.getString(index));
                            jsonArray.put(object);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Exception creating json object from cursor data", e);
                        }
                    } while (cursor.moveToNext());
                    fragment.setMovieJson(jsonArray);
                } else {
                    Log.e(LOG_TAG, "Error fetching discover data: query returned empty cursor");
                }
                cursor.close();
            } else {
                Log.e(LOG_TAG, "Error fetching discover data: query returned null");
            }
        }
    }

    static void fetchSpecificMovieData(final DetailFragment fragment, String id) {
        new FetchMovieDataTask(new DataSetter() {
            @Override
            public void setData(String text) {
                try {
                    fragment.setSpecificData(new JSONObject(text));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error with JSON object when fetching movie-by-id data", e);
                }
            }
        }).execute(getMovieByIdUrl(id));
    }

    static void fetchVideosData(final DetailFragment fragment, String id) {
        new FetchMovieDataTask(new DataSetter() {
            @Override
            public void setData(String text) {
                try {
                    fragment.setTrailersData(new JSONObject(text));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error with JSON object when fetching videos data", e);
                }
            }
        }).execute(getVideosByIdUrl(id));
    }

    static void fetchReviewsData(final DetailFragment fragment, String id) {
        new FetchMovieDataTask(new DataSetter() {
            @Override
            public void setData(String text) {
                try {
                    fragment.setReviewsData(new JSONObject(text));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error with JSON object when fetching reviews data", e);
                }
            }
        }).execute(getReviewsByIdUrl(id));
    }

    private static String getSortedMoviesUrl(String sortType) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_PATH_VERSION)
                .appendPath(API_PATH_MOVIE)
                .appendPath(sortType)
                .appendQueryParameter(API_KEY_NAME, API_KEY)
                .build();
        return uri.toString();
    }

    private static String getMovieByIdUrl(String movieId) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_PATH_VERSION)
                .appendPath(API_PATH_MOVIE)
                .appendPath(movieId)
                .appendQueryParameter(API_KEY_NAME, API_KEY)
                .build();
        return uri.toString();
    }

    private static String getReviewsByIdUrl(String movieId) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_PATH_VERSION)
                .appendPath(API_PATH_MOVIE)
                .appendPath(movieId)
                .appendPath(API_PATH_REVIEWS)
                .appendQueryParameter(API_KEY_NAME, API_KEY)
                .build();
        return uri.toString();
    }

    private static String getVideosByIdUrl(String movieId) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme(API_SCHEME)
                .authority(API_AUTHORITY)
                .appendPath(API_PATH_VERSION)
                .appendPath(API_PATH_MOVIE)
                .appendPath(movieId)
                .appendPath(API_PATH_VIDEOS)
                .appendQueryParameter(API_KEY_NAME, API_KEY)
                .build();
        return uri.toString();
    }

    public static URL buildPosterUrl(String imageWidth, String relativePath) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(IMAGE_SCHEME)
                .authority(IMAGE_AUTHORITY)
                .appendPath(IMAGE_PATH1)
                .appendPath(IMAGE_PATH2)
                .appendPath(imageWidth)
                .appendEncodedPath(relativePath);

        URL url = null;
        try {
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "malformed poster URL", e);
        }
        return url;
    }
}
