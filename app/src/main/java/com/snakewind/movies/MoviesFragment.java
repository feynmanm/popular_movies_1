/*
 * Copyright (C) 2016 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Holds GridView displaying movie poster thumbnails. Allows user to select a thumbnail
 * to bring up details about that movie
 */

public class MoviesFragment extends Fragment {

    public final String LOG_TAG = MoviesFragment.class.getSimpleName();

    // Keys for savedInstanceState
    public final String JSONARRAY_BUNDLEKEY = "movieJson";
    public final String MPOSITION_BUNDLEKEY = "highlightedPosition";

    // Position in the grid of the system's initial item selection
    public final int START_POSITION = 0;

    //Holds the position of the currently selected item (one item is selected at a time)
    private int mPosition;

    // Gridview that holds movie poster thumbnails
    private GridView mGridView;

    // Initial data about all the movies in the grid, which includes the location of the poster
    // thumbnail to be displayed. Also includes further details which are passed
    // to the detail activity, aka "Discover" data.
    private JSONArray mMovieJson;

    // Was the grid just loaded with new data, i.e., the user has not selected an item
    // in the grid yet? This is important because we only want to pull up the detail activity
    // for a 1-pane device when the user makes a selection.
    private boolean mOnStart;

    private MovieAdapter mAdapter;
    private Context mThisContext;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mThisContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAdapter = new MovieAdapter(mThisContext);
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.movie_grid);
        mGridView.setAdapter(mAdapter);

        // Listens when a grid selection is made.
        // Also sets mOnStart to false after notifying the handler because any subsequent selection
        // (without new grid data) is guaranteed to be made by the user (not the system).
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter.getItem(position) != null){
                    Callback callback = (Callback) getActivity();
                    JSONObject jsonObject = (JSONObject) mAdapter.getItem(position);
                    callback.onItemSelect(jsonObject, getSortType(), mOnStart);
                    mOnStart = false;
                }
            }
        });
        return rootView;
    }

    /*
     * The owning Activity must implement this Interface. The method is called when the
     * user selects a movie thumbnail. The activity is expected to start the appropriate
     * detail fragment/activity in response.
     */
    public interface Callback {
        void onItemSelect(JSONObject jsonObject, String sortType, boolean atStart);
    }

    //Returns user's preference for sorting the list of movies being fetched
    private String getSortType() {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        return prefs.getString(getString(R.string.sort_preference),
                getString(R.string.api_value_popularity));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null) {
            mOnStart = true;
            mPosition = START_POSITION;
            requestMoviesData();
        } else {
            try {
                mOnStart = false;
                mPosition = savedInstanceState.getInt(MPOSITION_BUNDLEKEY);
                setMovieJson(new JSONArray(savedInstanceState.getString(JSONARRAY_BUNDLEKEY)));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "json exception in onActivityCreated()", e);
            }
        }
    }

    // Requests Discover data for the grid. It is gathered asynchronously from either themoviedb.org
    // or a database. The data is returned via a callback method (setMovieJson) below.
    private void requestMoviesData() {
        DataFetcher.fetchSortedMoviesData(this, getSortType());
    }

    /*
     * Receives new sorted movie data from either themoviedb.org or the user's favorite movies
     * in the DB, depending on the sort preference, and then directs the grid to update with it.
     */
    public void setMovieJson(JSONArray jsonArray) {
        mMovieJson = jsonArray;
        if (mMovieJson != null) {
            mAdapter.updateMovies(mMovieJson);

            // If the data is new (that is, not from a savedInstanceState), the system selects a
            // an item in the grid whose details will be shown in the 2-pane view
            if (mOnStart) {
                mGridView.performItemClick(mGridView.getChildAt(mPosition), mPosition,
                        mAdapter.getItemId(mPosition));
            }
            String shortName = null;
            if (getSortType().equals(mThisContext.getString(R.string.api_value_popularity))) {
                shortName = mThisContext.getString(R.string.toast_name_popularity, mMovieJson.length());
            } else if (getSortType().equals(mThisContext.getString(R.string.api_value_rating))) {
                shortName = mThisContext.getString(R.string.toast_name_rating, mMovieJson.length());
            } else if (getSortType().equals(mThisContext.getString(R.string.action_showfavorites))) {
                shortName = mThisContext.getString(R.string.toast_name_favorites);
            }
            Toast.makeText(mThisContext, shortName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    // Save the grid data and the selected movie position
    public void onSaveInstanceState(Bundle outState) {
        if (mMovieJson != null) {
            outState.putString(JSONARRAY_BUNDLEKEY, mMovieJson.toString());
        }
        outState.putInt(MPOSITION_BUNDLEKEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_showfavorites: {
                setSortType(getString(R.string.action_showfavorites));
                break;
            }
            case R.id.action_sortbypopularity: {
                setSortType(getString(R.string.api_value_popularity));
                break;
            }
            case R.id.action_sortbyrating: {
                setSortType(getString(R.string.api_value_rating));
                break;
            }
        }

        // Any sort preferences selection results in new grid data being loaded
        mOnStart = true;
        requestMoviesData();
        return true;
    }

    // Puts the user's sort preference in Shared Preferences
    private void setSortType(String api_value) {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.sort_preference), api_value).apply();
    }
}