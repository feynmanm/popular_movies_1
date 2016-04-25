/*
 * Copyright (C) 2016 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import org.json.JSONObject;

/*
 * Initial activity; supports a 2-pane grid/detail view side-by-side in wide displays
 */

public class Movies extends AppCompatActivity implements MoviesFragment.Callback {

    public final String DETAILFRAGMENT_TAG = "DFTAG";

    // Keys for data put into intent that opens the detail activity
    public static final String MOVIEDATA = "moviedata";
    public static final String SORTDATA = "sortdata";

    public static String defaultSort;

    // Will we be using a 2-pane view?
    private boolean mTwoPane;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        defaultSort = getString(R.string.api_value_popularity);

        setContentView(R.layout.activity_movies);
        mTwoPane = findViewById(R.id.detail_fragment) != null;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movies, menu);
        return true;
    }

    @Override
    // Opens a detail fragment in response to the user selecting a movie in the main fragment.
    // This is called by the gridview when a selection is made.
    public void onItemSelect(JSONObject jsonObject, String sortType, boolean atStart) {

        //Opens the detail fragment for a two-pane layout
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment,
                            DetailActivity.DetailFragment.newInstance(jsonObject, sortType),
                            DETAILFRAGMENT_TAG).commit();

        // Opens the detail fragment for a one-pane layout.
        // Note that this does nothing in response to the system's default selection when
        // new grid data is initially loaded, i.e., at start.
        } else if (!atStart) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(MOVIEDATA, jsonObject.toString());
            intent.putExtra(SORTDATA, sortType);
            startActivity(intent);
        }
    }
}