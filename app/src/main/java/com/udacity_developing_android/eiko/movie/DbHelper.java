package com.udacity_developing_android.eiko.movie;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by eiko on 11/9/2017.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static final String DATABESE_NAME = "movies.db";
    public static final int DATABESE_NUM = 1;
    private static DbHelper helper;
    public DbHelper(Context context) {
        super(context, DATABESE_NAME, null, DATABESE_NUM);
    }

    public static synchronized DbHelper getHelper(Context context) {
        if (helper == null)
            helper = new DbHelper(context);
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                Contract.Entry.TABLE_NAME + " (" +
                Contract.Entry.COLUMN_TITLE + " TEXT NOT NULL, " +
                Contract.Entry.COLUMN_RELEASEDATE + " TEXT NOT NULL, " +
                Contract.Entry.COLUMN_RATING + " TEXT NOT NULL, " +
                Contract.Entry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                Contract.Entry.COLUMN_POSTER + " TEXT NOT NULL, " +
                Contract.Entry.COLUMN_FAVORITE_OR_NOT + " TEXT, " +
                Contract.Entry.COLUMN_MOVIE_ID +
                " INTEGER PRIMARY KEY ON CONFLICT REPLACE " +
                " );");
        Log.v("TABLE id name: ", Contract.Entry.COLUMN_MOVIE_ID);
        Log.v("TABLE favorite status: ", Contract.Entry.COLUMN_FAVORITE_OR_NOT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contract.Entry.TABLE_NAME);
        onCreate(db);
    }
}
