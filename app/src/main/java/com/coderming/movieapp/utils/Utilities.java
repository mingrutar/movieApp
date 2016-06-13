package com.coderming.movieapp.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.data.MovieContract.MovieSelectionType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by linna on 6/5/2016.
 */
public class Utilities {
    private static final String LOG_TAG = Utilities.class.getSimpleName();

    public static final String RELEASE_DATE = "YYYY";       // "MMM yyyy";
    public static final String sDeleteMovie = String.format("%s.%s!=%s.%s",MovieContract.MovieEntry.TABLE_NAME, BaseColumns._ID,
    MovieContract.MovieSelectionEntry.TABLE_NAME, MovieContract.MovieSelectionEntry.COLUMN_MOVIE_ID);

    private static List<Long> FavoriteList = new ArrayList<>();

    public static String releaseDate2Str(long timeinMilli) {
        SimpleDateFormat dateFormater = new SimpleDateFormat(RELEASE_DATE, Locale.getDefault());
        Date date = new Date(timeinMilli);
        return String.format("(%s)", dateFormater.format(date));
    }
    public static int getRecordLimmit(MovieSelectionType type) {
        //TODO: get from shared preference, setting
        return (type == MovieSelectionType.Popular) ? 3 : 2;
    }
    public static void playYouTube(Context context, String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,      // try web
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            context.startActivity(intent);
        }
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
    static public int getMovieId(Context context, long moviewDbId) {
        int ret = -1;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                    BaseColumns._ID + "=?",
                    new String[]{Long.toString(moviewDbId)}, null);
            if (cursor.moveToFirst()) {
                ret = cursor.getInt(0);
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
        addFavoriteMovie(movieDbId);
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieSelectionEntry.COLUMN_MOVIE_ID, movieDbId);
        values.put(MovieContract.MovieSelectionEntry.COLUMN_SELECTION_TYPE, MovieSelectionType.Favorite.getValue());
        return context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_FAVORITE_URI, values);
    }
    static public void addFavoriteMovie(Long movieDbId) {
        if (!FavoriteList.contains(movieDbId)) {
            FavoriteList.add(movieDbId);
        }
    }
    static public boolean isFavoritePage(Uri uri) {
        return uri.equals(MovieContract.MovieEntry.CONTENT_FAVORITE_URI);
    }
    static public boolean isFavorite(long movieDbId) {
        return FavoriteList.contains(movieDbId);
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
