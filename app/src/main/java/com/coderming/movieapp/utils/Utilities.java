package com.coderming.movieapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.data.MovieContract.MovieSelectionType;

import java.util.Random;

/**
 * Created by linna on 6/5/2016.
 */
public class Utilities {
   private static final String LOG_TAG = Utilities.class.getSimpleName();
    public static final String sDeleteMovie = String.format("%s.%s!=%s.%s",MovieContract.MovieEntry.TABLE_NAME, BaseColumns._ID,
    MovieContract.MovieSelectionEntry.TABLE_NAME, MovieContract.MovieSelectionEntry.COLUMN_MOVIE_ID);

    public static int getRecordLimmit(MovieSelectionType type) {
        //TODO: get from shared preference, setting
        return (type == MovieSelectionType.Popular) ? 3 : 2;
    }

    static public long getMovieDbId(Context context, int moview_id) {
        long ret = -1;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{BaseColumns._ID},
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                    new String[]{Integer.toString(moview_id)}, null);
            if (cursor.moveToFirst()) {
                ret = cursor.getLong(0);
            }
        } catch (RuntimeException rex ) {
            Log.w(LOG_TAG, "getMovieDbId caught an exception.", rex);
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return ret;
    }
    static public boolean hasMovieDetailInDB(Context context, long id ) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MovieContract.DetailEntry.buildUri(id),
                    new String[] {BaseColumns._ID}, null, null, null, null);
           return cursor.moveToFirst();
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }
    }
    //TODO: do not delete movie has favority
    static public  int removeMovies(Context context) {
        context.getContentResolver().delete( MovieContract.DetailEntry.CONTENT_URI, null, null);
        return context.getContentResolver().delete( MovieContract.MovieEntry.CONTENT_URI, null, null);
    }
    static public Uri addFavoriteMovie(Context context, long movieDbId) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieSelectionEntry.COLUMN_MOVIE_ID, movieDbId);
        values.put(MovieContract.MovieSelectionEntry.COLUMN_SELECTION_TYPE, MovieSelectionType.Favorite.getValue());
        return context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_FAVORITE_URI, values);
    }

    /***
     * the following for helps testing
     * @param context
     * @return
     */
    static  public int getRandonMovieId(Context context) {
        Cursor cursor = null;
        int ret = 4;
        int val = -1;
        try {
            cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID}, null, null, null);
            if (cursor.moveToFirst()) {
                val = Math.abs( new Random().nextInt() % cursor.getCount());
                if (cursor.moveToPosition(val)) {
                    ret = cursor.getInt(0);
                    Log.v(LOG_TAG, String.format("getRandonMovieId get rec pos=%d out of total %d. movieId=%d",
                            val, cursor.getCount(), ret));
                }
            }
        }catch (Exception ex) {
            Log.e(LOG_TAG, String.format("getRandonMovieId, randon #rec=%d, exception=%s",val,ex.getMessage()), ex);
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return ret;
    }
    static public Uri addRandonFavorMovie(Context context) {
        Cursor cursor = null;
        Uri ret = null;
        try {
            cursor = context.getContentResolver().query (MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{BaseColumns._ID}, null, null, null);
            if (cursor.moveToFirst()) {
                int val = Math.abs(new Random().nextInt() % cursor.getCount());
                cursor.moveToPosition(val);
                long id = cursor.getInt(0);
                Log.v(LOG_TAG, String.format("getRandonMovieId get rec pos=%d out of total %d. movieId=%d",
                        val, cursor.getCount(),id));
                ret = addFavoriteMovie(context, id);
            }
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return ret;
    }
}
