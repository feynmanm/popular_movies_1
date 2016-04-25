/*
 * Copyright (C) 2016 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

/*
 * Provides ImageViews containing movie poster thumbnails from themoviedb.org;
 * Stores sorted movies data for potential display in details activity
 */

public class MovieAdapter extends BaseAdapter {

    private final static String LOG_TAG = MovieAdapter.class.getSimpleName();

    private final static String IMAGE_PATH_WIDTH = "w185";

    private Context mThisContext;
    private JSONArray mMovieData;

    public MovieAdapter (Context thisContext) {
        mThisContext = thisContext;
        mMovieData = new JSONArray();
    }

    /**Directs UI to update when the movie list is updated*/
    public void updateMovies (JSONArray movies) {
        mMovieData = movies;
        notifyDataSetChanged();
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
        Object object = null;
        try {
            object = mMovieData.get(position);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json exception", e);
        }
        return object;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mThisContext);
            imageView.setLayoutParams(new GridView.LayoutParams(220, 320));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            int padding = (int) mThisContext.getResources().getDimension(R.dimen.content_margin);
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setBackgroundResource(R.drawable.touch_selector);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso.with(mThisContext)
                .load(DataFetcher.buildPosterUrl(IMAGE_PATH_WIDTH,
                        getRelativePath(position)).toString())
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.error_placeholder)
                .into(imageView);

        return imageView;
    }

    private String getRelativePath(int position) {
        String path = null;
        try {
            path = mMovieData.getJSONObject(position)
                    .getString(mThisContext.getString(R.string.json_poster_path));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json exception", e);
        }
        return path;
    }
}
