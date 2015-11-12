/*
 * Copyright (C) 2015 Michael Reynolds
 */

package com.snakewind.movies;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;

/*
 * Provides ImageViews containing movie posters from themoviedb.org to AdapterView;
 * Stores associated movie info for display in details
 */

public class MovieAdapter extends BaseAdapter {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();

    private final static String IMAGE_SCHEME = "http";
    private final static String IMAGE_AUTHORITY = "image.tmdb.org";
    private final static String IMAGE_PATH1 = "t";
    private final static String IMAGE_PATH2 = "p";
    private final static String IMAGE_PATH_WIDTH = "w185";

    private Movies mThisContext;
    private JSONArray mMovieData;

    public MovieAdapter (Movies thisContext) {
        mThisContext = thisContext;
        mMovieData = new JSONArray();
    }

    static URL getPosterUrlFromJson(String imageWidth, String relativePath) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(IMAGE_SCHEME)
                .authority(IMAGE_AUTHORITY)
                .appendPath(IMAGE_PATH1)
                .appendPath(IMAGE_PATH2)
                .appendPath(imageWidth)
                .appendEncodedPath(relativePath);

        try {
            return new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "malformed URL", e);
            return null;
        }
    }

    /**Manages UI updates when the movie list is updated*/
    public void udpateMovies (JSONArray movies) {
        mMovieData = movies;

        String shortName = null;
        if (mThisContext.getSortType().equals(mThisContext.getString(R.string.api_value_popularity))) {
            shortName = mThisContext.getString(R.string.short_name_popularity);
        } else if (mThisContext.getSortType().equals(mThisContext.getString(R.string.api_value_rating))) {
            shortName = mThisContext.getString(R.string.short_name_rating);
        }

        notifyDataSetChanged();

        Toast.makeText(mThisContext, "Top " + movies.length() + " Movies, " +
                "Sorted by " + shortName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getCount() {
        return mMovieData.length();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        try {
            return mMovieData.get(position);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json exception", e);
            return null;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mThisContext);
            imageView.setLayoutParams(new GridView.LayoutParams(160, 250));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mThisContext)
                .load(getPosterUrlFromJson(IMAGE_PATH_WIDTH, getRelativePath(position)).toString())
                .into(imageView);

        return imageView;
    }

    private String getRelativePath(int position) {
        try {
            return mMovieData.getJSONObject(position)
                    .getString(mThisContext.getString(R.string.json_poster_path));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json exception", e);
            return null;
        }
    }
}
