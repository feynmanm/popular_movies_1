/*
 * Copyright (C) 2015 Michael Reynolds
 */

package com.snakewind.movies;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Presents details of users movie selection
 */

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**Holds selected movie details*/
    public static class DetailFragment extends Fragment {
        public DetailFragment() {}

        private final static String LOG_TAG = DetailFragment.class.getSimpleName();
        private final static String DETAIL_IMAGE_WIDTH = "w342";

        JSONObject mJsonObject;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            try {
                mJsonObject = new JSONObject(getActivity().getIntent()
                        .getStringExtra(Intent.EXTRA_TEXT));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "json exception", e);
                return null;
            }

            View rootView = inflater.inflate(R.layout.content_detail, container, false);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_image);
            Picasso.with(getActivity())
                    .load(MovieAdapter.getPosterUrlFromJson(DETAIL_IMAGE_WIDTH,
                            getStringFromJson(R.string.json_poster_path)).toString())
                    .into(imageView);

            TextView titleText = (TextView) rootView.findViewById(R.id.movie_title);
            titleText.setText(getStringFromJson(R.string.json_name_title));
            TextView plotText = (TextView) rootView.findViewById(R.id.plot);
            plotText.setText(getStringFromJson(R.string.json_name_plot));
            TextView ratingText = (TextView) rootView.findViewById(R.id.rating);
            ratingText.setText("Rating: " + getStringFromJson(R.string.json_name_rating));
            TextView releaseDateText = (TextView) rootView.findViewById(R.id.release_date);
            releaseDateText.setText(getStringFromJson(R.string.json_name_release_date));
            return rootView;
        }

        private String getStringFromJson (int keyResId) {
            try {
                return mJsonObject.getString(getString(keyResId));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "json exception", e);
                return null;
            }
        }
    }
}
