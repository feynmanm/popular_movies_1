/*
 * Copyright (C) 2016 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.snakewind.movies.data.FavoritesContract.MovieEntry;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

/*
 * Presents details of user's movie selection
 */

public class DetailActivity extends AppCompatActivity {

    public final static String LOG_TAG = DetailActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            try {
                JSONObject jsonObject = new JSONObject(getIntent().getStringExtra(Movies.MOVIEDATA));
                String sortType = getIntent().getStringExtra(Movies.SORTDATA);
                getSupportFragmentManager().beginTransaction().add(R.id.detail_fragment,
                        DetailFragment.newInstance(jsonObject, sortType)).commit();
            } catch (JSONException e) {
                Log.e(LOG_TAG, "json exception", e);
            }
        }
    }

    /**Holds selected movie details*/
    public static class DetailFragment extends Fragment {

        private final static String LOG_TAG = DetailFragment.class.getSimpleName();
        private final static String DISCOVER_ARGKEY = "discoverArgKey";
        private final static String SORTTYPE_ARGKEY = "sorttypeArgKey";
        private final static String DISCOVER_SAVEKEY = "discoverSaveKey";
        private final static String SPECIFIC_SAVEKEY = "specificSaveKey";
        private final static String VIDEOS_SAVEKEY = "videosSaveKey";
        private final static String REVIEWS_SAVEKEY = "reviewsSaveKey";
        private final static String SORTTYPE_SAVEKEY = "sorttypeSaveKey";

        private final static String VIDEOS_KEY = "results";
        private final static String VIDEONAME_KEY = "name";
        private final static String VIDEOKEY_KEY = "key";
        private final static String REVIEWS_KEY = "results";
        private final static String REVIEWAUTHOR_KEY = "author";
        private final static String REVIEWURL_KEY = "url";

        private String mSortType;

        //Holds the movie's data originating from the app's discovery query
        private JSONObject mDiscoverJsonObject;
        private String mId;

        //Holds the data originating from the movie's "id" query
        private JSONObject mSpecificJsonObject;

        //Holds the data originating from the movie's videos query
        private JSONObject mTrailersJsonObject;

        //Holds the data originating from the movie's reviews query
        private JSONObject mReviewsJsonObject;

        LayoutInflater mInflator;
        View rootView;

        //Bindings of View data to resources using Butterknife
        @Bind(R.id.movie_title) TextView mTitleTextView;
        @Bind(R.id.detail_image) ImageView mPosterImageView;
        @Bind(R.id.release_year) TextView mReleaseYearTextView;
        @Bind(R.id.running_time) TextView mRuntimeTextView;
        @Bind(R.id.rating) TextView mRatingTextView;
        @Bind(R.id.plot) TextView mPlotTextView;

        Button mFavoriteButton;

        private String mTitle;
        private String mPosterPath;
        private String mRelaseDate;
        private String mRuntime;
        private String mRating;
        private String mPlot;

        //Size of the poster on the Details page
        private final static String DETAIL_IMAGE_WIDTH = "w342";

        public DetailFragment() {}

        public static DetailFragment newInstance(JSONObject jsonObject, String sortType) {
            DetailFragment detailFragment = new DetailFragment();

            Bundle args = new Bundle();
            if (jsonObject != null) {
                args.putCharSequence(DISCOVER_ARGKEY, jsonObject.toString());
                args.putCharSequence(SORTTYPE_ARGKEY, sortType);
            }
            detailFragment.setArguments(args);
            return detailFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mInflator = inflater;
            rootView = inflater.inflate(R.layout.content_detail, container, false);
            ButterKnife.bind(this, rootView);

            mFavoriteButton = (Button) rootView.findViewById(R.id.favorite_button);
            mFavoriteButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mId != null) {
                            ContentValues generalValues = new ContentValues();
                            generalValues.put(MovieEntry._ID, mId);
                            generalValues.put(MovieEntry.COLUMN_DISCOVER_DATA, mDiscoverJsonObject.toString());
                            generalValues.put(MovieEntry.COLUMN_SPECIFIC_DATA, mSpecificJsonObject.toString());
                            generalValues.put(MovieEntry.COLUMN_TRAILERS_DATA, mTrailersJsonObject.toString());
                            generalValues.put(MovieEntry.COLUMN_REVIEWS_DATA, mReviewsJsonObject.toString());
                            getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, generalValues);
                            setButtonToAlreadyFavorited();
                            Toast.makeText(getContext(),
                                    getContext().getString(R.string.toast_favorite_successful),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    getContext().getString(R.string.toast_favorite_unsuccessful),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            return rootView;
        }

        private void setButtonToAlreadyFavorited() {
            mFavoriteButton.setEnabled(false);
            mFavoriteButton.setText(R.string.already_favorited_text);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            try {
                //If there is no savedInstanceState, get initial movie data from the arguments
                //passed from main activity and fetch all other data from the DataFetcher class
                if (savedInstanceState == null) {
                    if (getArguments().containsKey(SORTTYPE_ARGKEY)) {
                        mSortType = getArguments().getString(SORTTYPE_ARGKEY);
                    } else {
                        mSortType = Movies.defaultSort;
                    }
                    if (getArguments().containsKey(DISCOVER_ARGKEY)) {
                        setDiscoverData(new JSONObject(getArguments().getString(DISCOVER_ARGKEY)));
                        DataFetcher.getAdditionalDetails(this, mId, mSortType);
                    } else {
                        throw new RuntimeException("DetailActivity created without movie data... huh?");
                    }
                //Otherwise, expect to get all data from the savedInstanceState
                } else {
                    if (savedInstanceState.containsKey(DISCOVER_SAVEKEY)) {
                        setDiscoverData(new JSONObject(savedInstanceState.getString(DISCOVER_SAVEKEY)));
                        mSortType = savedInstanceState.getString(SORTTYPE_SAVEKEY);
                        setSpecificData(new JSONObject(savedInstanceState.getString(SPECIFIC_SAVEKEY)));
                        setTrailersData(new JSONObject(savedInstanceState.getString(VIDEOS_SAVEKEY)));
                        setReviewsData(new JSONObject(savedInstanceState.getString(REVIEWS_SAVEKEY)));
                    } else {
                        throw new RuntimeException("DetailActivity not properly saved");
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "json exception in OnCreateView()", e);
            }
        }

        //Set the movie's ID and bind discover data to views
        private void setDiscoverData(JSONObject object) {
            mDiscoverJsonObject = object;

            mPosterPath = Utility.getStringFromJson(getContext(), mDiscoverJsonObject, R.string.json_poster_path);
            Picasso.with(getActivity())
                    .load(DataFetcher.buildPosterUrl(DETAIL_IMAGE_WIDTH, mPosterPath).toString())
                    .placeholder(R.drawable.poster_placeholder)
                    .error(R.drawable.error_placeholder)
                    .into(mPosterImageView);

            mTitle = Utility.getStringFromJson(getContext(), mDiscoverJsonObject, R.string.json_title);
            mTitleTextView.setText(mTitle);

            mRelaseDate = Utility.getStringFromJson(getContext(), mDiscoverJsonObject, R.string.json_release_date);
            mReleaseYearTextView.setText(Utility.getReleaseYear(mRelaseDate));

            mRating = Utility.getStringFromJson(getContext(), mDiscoverJsonObject, R.string.json_rating);
            mRatingTextView.setText(getString(R.string.format_rating, Float.parseFloat(mRating)));

            mPlot = Utility.getStringFromJson(getContext(), mDiscoverJsonObject, R.string.json_plot);
            mPlotTextView.setText(mPlot);

            mId = Utility.getStringFromJson(getContext(), mDiscoverJsonObject, R.string.json_id);
            if (Utility.isThisMovieFavorited(getContext(), mId)) {
                setButtonToAlreadyFavorited();
            }
        }

        //Bind data gotten from the movie/id call (like runtime) to views
        void setSpecificData(JSONObject object) {
            mSpecificJsonObject = object;
            mRuntime = Utility.getStringFromJson(getContext(), mSpecificJsonObject, R.string.json_runtime);
            mRuntimeTextView.setText(getString(R.string.format_runtime, Integer.parseInt(mRuntime)));
        }

        //Bind data for associated videos to views, create URLs, and launch implicit intent on click
        void setTrailersData(JSONObject object) {
            mTrailersJsonObject = object;
            LinearLayout parentView = (LinearLayout) getActivity().findViewById(R.id.trailer_group);
            View view;
            JSONArray videosArray = null;
            try {
                videosArray = mTrailersJsonObject.getJSONArray(VIDEOS_KEY);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error setting JSON array of trailers", e);
            }
            if (videosArray != null) {
                for (int i = 0; i < videosArray.length(); i++) {
                    view = mInflator.inflate(R.layout.trailer_layout, parentView, false);
                    TextView textView = (TextView) view.findViewById(R.id.trailer);
                    JSONObject trailerObject;
                    String trailerName = "";
                    String trailerKey = "";
                    try {
                        trailerObject = videosArray.getJSONObject(i);
                        trailerName = trailerObject.getString(VIDEONAME_KEY);
                        trailerKey = trailerObject.getString(VIDEOKEY_KEY);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "problem reading trailer name", e);
                    }
                    textView.setText(trailerName);
                    parentView.addView(view);

                    final String trailerUrl = "https://www.youtube.com/watch?v=" + trailerKey;
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri trailerSite = Uri.parse(trailerUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, trailerSite);
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        }

        //Bind data for associated reviews to views, create URLs, and launch implicit intent on click
        void setReviewsData(JSONObject object) {
            mReviewsJsonObject = object;
            LinearLayout parentView = (LinearLayout) getActivity().findViewById(R.id.review_group);
            View view;
            JSONArray reviewsArray = null;
            try {
                reviewsArray = mReviewsJsonObject.getJSONArray(REVIEWS_KEY);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error setting JSON array of reviews", e);
            }
            if (reviewsArray != null) {
                for (int i = 0; i < reviewsArray.length(); i++) {
                    view = mInflator.inflate(R.layout.review_layout, parentView, false);
                    TextView textView = (TextView) view.findViewById(R.id.review);
                    JSONObject reviewObject;
                    String reviewAuthor = "";
                    String reviewUrlTemp = "";
                    try {
                        reviewObject = reviewsArray.getJSONObject(i);
                        reviewAuthor = reviewObject.getString(REVIEWAUTHOR_KEY);
                        reviewUrlTemp = reviewObject.getString(REVIEWURL_KEY);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, "problem reading trailer name", e);
                    }
                    textView.setText(reviewAuthor);
                    parentView.addView(view);

                    final String reviewUrl = reviewUrlTemp;
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri trailerSite = Uri.parse(reviewUrl);
                            Intent intent = new Intent(Intent.ACTION_VIEW, trailerSite);
                            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            if (mDiscoverJsonObject != null &&
                    mSpecificJsonObject != null &&
                    mTrailersJsonObject != null &&
                    mReviewsJsonObject != null &&
                    mSortType != null) {
                outState.putString(DISCOVER_SAVEKEY, mDiscoverJsonObject.toString());
                outState.putString(SPECIFIC_SAVEKEY, mSpecificJsonObject.toString());
                outState.putString(VIDEOS_SAVEKEY, mTrailersJsonObject.toString());
                outState.putString(REVIEWS_SAVEKEY, mReviewsJsonObject.toString());
                outState.putString(SORTTYPE_SAVEKEY, mSortType);
                super.onSaveInstanceState(outState);
            }
        }
    }
}