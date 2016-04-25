package com.snakewind.movies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Michael on 2/15/2016.
 */
public class FavoritesContract implements BaseColumns {

    public final static String CONTENT_AUTHORITY = "com.snakewind.movies";
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public final static class MovieEntry implements BaseColumns {
        // table name
        public final static String TABLE_NAME = "movies";
        public final static String OLD_TABLE_NAME = "movie";
        // columns
        public final static String COLUMN_DISCOVER_DATA = "discover_data";
        public final static String COLUMN_SPECIFIC_DATA = "specific_data";
        public final static String COLUMN_TRAILERS_DATA = "trailers_data";
        public final static String COLUMN_REVIEWS_DATA = "reviews_data";

        // content uri
        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();

        // mime type for directories
        public final static String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                + TABLE_NAME;

        // mime type for items
        public final static String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + TABLE_NAME;

        // for building URIs on insertion
        public static Uri buildFavoritesUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }
    }
}
