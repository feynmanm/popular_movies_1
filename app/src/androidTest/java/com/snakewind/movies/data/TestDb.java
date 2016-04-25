package com.snakewind.movies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

/**
 * Created by Michael on 2/21/2016.
 */
public class TestDb extends AndroidTestCase {

    public final static String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTestDatabase() {
        mContext.deleteDatabase(FavoritesDBHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTestDatabase();
    }

    public void testCreateDb() throws Throwable {
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(FavoritesContract.MovieEntry.TABLE_NAME);

        deleteTestDatabase();
        SQLiteDatabase db = new FavoritesDBHelper(mContext).getWritableDatabase();
        // Did we create a database?
        assertEquals(true, db.isOpen());

        // Did we create the tables we wanted
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);
        assertTrue("Error: This means the database has not been created correctly", c.moveToFirst());
        do {
            String tableName = c.getString(0);
            Log.i(LOG_TAG, "table to remove: " + tableName);
            tableNameHashSet.remove(tableName);
        } while(c.moveToNext());
        assertTrue("Error: database was created with more tables than favorites, trailer, and review",
                tableNameHashSet.isEmpty());

        db.close();
        deleteTestDatabase();
    }
}
