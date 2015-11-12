/*
 * Copyright (C) 2015 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

/*
 * Holds GridView displaying movie posters
 */

public class MoviesFragment extends Fragment {

    private Movies mThisContext;
    private MovieAdapter mAdapter;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mThisContext = (Movies) getActivity();
        mAdapter = new MovieAdapter(mThisContext);
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.movie_grid);
        gridView.setAdapter(mAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mAdapter.getItem(position).toString());
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        populateImageAdapter();
    }

    //repopulate ImageAdapter with updated movie info from the web API (using an AsyncTask)
    private void populateImageAdapter() {
        FetchMovieTask task = new FetchMovieTask(mAdapter);
        task.execute(mThisContext.getSortType());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sortbyrating) {
            setSortType(getString(R.string.api_value_rating));
            populateImageAdapter();
            return true;
        }
        if (id == R.id.action_sortbypopularity) {
            setSortType(getString(R.string.api_value_popularity));
            populateImageAdapter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSortType(String api_value) {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.sort_preference), api_value).apply();
    }
}
