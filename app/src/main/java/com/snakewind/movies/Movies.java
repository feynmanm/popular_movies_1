/*
 * Copyright (C) 2015 Michael Reynolds
 */

package com.snakewind.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

/*
 * Initial Movies app activity
 */

public class Movies extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movies, menu);
        return true;
    }

    /**Returns user's preference for sorting the list of movies being fetched*/
    public String getSortType() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        return prefs.getString(getString(R.string.sort_preference),
                getString(R.string.api_value_popularity));
    }

}
