package com.coderming.movieapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.coderming.movieapp.utils.Utilities;

/**
 * Created by linna on 6/2/2016.
 */
public class MovieProvider extends ContentProvider {
    private static final String LOG_TAG = MovieProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static public final int MOVIE = 100;
    static public final int MOVIE_POPULAR = 101;
    static public final int MOVIE_TOP_RATE = 102;
    static public final int MOVIE_FAVORITE = 103;
    static public final int MOVIE_BY_ID = 110;

    static public final int DETAIL_MOVIE = 200;
    static public final int DETAIL_MOVIE__ID = 210;

    com.coderming.movieapp.data.MovieDbHelper mOpenHelper;

    static public UriMatcher buildUriMatcher() {
        String path = com.coderming.movieapp.data.MovieContract.MovieEntry.TABLE_NAME;
        UriMatcher ret = new UriMatcher(UriMatcher.NO_MATCH);
        ret.addURI(com.coderming.movieapp.data.MovieContract.CONTENT_AUTHORITY, com.coderming.movieapp.data.MovieContract.MovieEntry.TABLE_NAME, MOVIE);
        ret.addURI(com.coderming.movieapp.data.MovieContract.CONTENT_AUTHORITY,
                String.format("%s/%s", path, com.coderming.movieapp.data.MovieContract.MovieSelectionType.Popular), MOVIE_POPULAR);
        ret.addURI(com.coderming.movieapp.data.MovieContract.CONTENT_AUTHORITY,
                String.format("%s/%s", path, com.coderming.movieapp.data.MovieContract.MovieSelectionType.TopRated), MOVIE_TOP_RATE);
        ret.addURI(com.coderming.movieapp.data.MovieContract.CONTENT_AUTHORITY,
                String.format("%s/%s", path, com.coderming.movieapp.data.MovieContract.MovieSelectionType.Favorite), MOVIE_FAVORITE);
        ret.addURI(com.coderming.movieapp.data.MovieContract.CONTENT_AUTHORITY,
                String.format("%s/#", path), MOVIE_BY_ID);
        ret.addURI(com.coderming.movieapp.data.MovieContract.CONTENT_AUTHORITY, com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME, DETAIL_MOVIE);
        ret.addURI(com.coderming.movieapp.data.MovieContract.CONTENT_AUTHORITY,
                String.format("%s/#", com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME), DETAIL_MOVIE__ID);
        return ret;
    }

    @Override
    public boolean onCreate() {
        Log.v(LOG_TAG, "+++ onCreate called");
        mOpenHelper = new com.coderming.movieapp.data.MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        if (uri != null) {
            int match = sUriMatcher.match(uri);
            switch (match) {
                case MOVIE:
                case  MOVIE_TOP_RATE:
                case  MOVIE_POPULAR:
                case  MOVIE_FAVORITE:
                    return com.coderming.movieapp.data.MovieContract.MovieEntry.CONTENT_TYPE;
                case MOVIE_BY_ID:
                    return com.coderming.movieapp.data.MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
                case DETAIL_MOVIE:
                    return com.coderming.movieapp.data.MovieContract.DetailEntry.CONTENT_TYPE;
                case DETAIL_MOVIE__ID:
                    return com.coderming.movieapp.data.MovieContract.DetailEntry.CONTENT_ITEM_TYPE;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int matchCode = sUriMatcher.match(uri);
        if (matchCode == MOVIE_BY_ID) {
            String movieId = uri.getPathSegments().get(1);          //TODO: getLastSegment work?
            cursor = db.query(com.coderming.movieapp.data.MovieContract.MovieEntry.TABLE_NAME, projection,
                String.format("%s.%s = %s", com.coderming.movieapp.data.MovieContract.MovieEntry.TABLE_NAME, com.coderming.movieapp.data.MovieContract.MovieEntry._ID, movieId),
                null, null, null, sortOrder);
        } else if (matchCode == DETAIL_MOVIE__ID)  {
            String movieDbId = uri.getPathSegments().get(1);
            cursor = db.query(com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME, projection,
                    String.format("%s.%s = %s", com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME, com.coderming.movieapp.data.MovieContract.DetailEntry.COLUMN_MOVIE_ID,movieDbId),
                    null , null, null, sortOrder);
        }
        else {
            String tableName = null;
            if (matchCode == MOVIE) {
                tableName = com.coderming.movieapp.data.MovieContract.MovieEntry.TABLE_NAME;
            } else if (matchCode == MOVIE_TOP_RATE) {
                tableName = com.coderming.movieapp.data.MovieContract.MovieEntry.VIEW_TOP_RATED;
            } else if (matchCode == MOVIE_POPULAR) {
                tableName = com.coderming.movieapp.data.MovieContract.MovieEntry.VIEW_POPULAR;
            } else if (matchCode == MOVIE_FAVORITE) {
                tableName = com.coderming.movieapp.data.MovieContract.MovieEntry.VIEW_FAVORITE;
            } else if (matchCode == DETAIL_MOVIE) {
                tableName = com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME;
            }
            cursor = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
            }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int matchCode = sUriMatcher.match(uri);
        Uri ret = null;
        long id;
        SQLiteDatabase db = null;
        db = mOpenHelper.getWritableDatabase();
        if (matchCode == DETAIL_MOVIE) {
            id = db.insert(com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME, null, values);
            if (id > 0) {
                ret = com.coderming.movieapp.data.MovieContract.DetailEntry.buildUri(id);
            } else {
                throw new android.database.SQLException("Failed to insert row into " + uri);
            }
        } else if (matchCode == MOVIE_FAVORITE) {        // used in add favorite
            long dbId = db.insert(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.TABLE_NAME, null, values);
            if (dbId > 0) {
                ret = com.coderming.movieapp.data.MovieContract.MovieEntry.buildUri(values.getAsLong(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.COLUMN_MOVIE_ID));
            } else {
                Log.e(LOG_TAG, "insert: failed to add favorite movie " + uri);
                throw new android.database.SQLException("Failed to add uri " + uri);
            }
        } else {
            Log.i(LOG_TAG, "insert: not supported Uri for insertion, uri="+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int matchCode = sUriMatcher.match(uri);
        int numrow = -1;
        if (null == selection) {
            selection = "1";
        }
        SQLiteDatabase db = null;
        db = mOpenHelper.getWritableDatabase();
        if ((matchCode == MOVIE_POPULAR) || (matchCode == MOVIE_TOP_RATE) || (matchCode == MOVIE) ) {
            numrow = db.delete(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.TABLE_NAME, selection, selectionArgs);
            numrow = db.delete(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.TABLE_NAME, selection, selectionArgs);

        } else if (matchCode == MOVIE_FAVORITE) {
            numrow = db.delete(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.TABLE_NAME,
                    com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.COLUMN_SELECTION_TYPE+"="+ com.coderming.movieapp.data.MovieContract.MovieSelectionType.Favorite.toString(), null);
        } else if (matchCode == DETAIL_MOVIE) {
             numrow = db.delete(com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME, selection, selectionArgs);
        } else if (matchCode == DETAIL_MOVIE__ID) {
            String movieDbId = uri.getPathSegments().get(1);
            numrow = db.delete(com.coderming.movieapp.data.MovieContract.DetailEntry.TABLE_NAME,
                    com.coderming.movieapp.data.MovieContract.DetailEntry.COLUMN_MOVIE_ID+"="+movieDbId, null );
        }
        if (numrow > 0)   // with selection = "1", we can do this
            getContext().getContentResolver().notifyChange(uri, null);
        return numrow;
    }
    // we don't case about updates
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return 0;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int matchCode = sUriMatcher.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numrow = 0;
        if ((matchCode == MOVIE_POPULAR) || (matchCode == MOVIE_TOP_RATE)) {
            com.coderming.movieapp.data.MovieContract.MovieSelectionType type = (matchCode == MOVIE_POPULAR) ?
                    com.coderming.movieapp.data.MovieContract.MovieSelectionType.Popular : com.coderming.movieapp.data.MovieContract.MovieSelectionType.TopRated;
            Log.v(LOG_TAG, String.format("++++ bulkInsert: #rec=%d, type=%s ", values.length, type.toString() ));
            try {
                long id;
                ContentValues sel_values = new ContentValues();
                db.beginTransaction();
                for (ContentValues contentValues : values) {
                    int movieId = (int) contentValues.get(com.coderming.movieapp.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                    id = Utilities.getMovieDbId(getContext(), movieId );
                    if (id == -1) {
                        id = db.insert(com.coderming.movieapp.data.MovieContract.MovieEntry.TABLE_NAME, null, contentValues);
                    }
                    if (id != -1) {
                        sel_values.clear();
                        sel_values.put(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.COLUMN_MOVIE_ID, id);
                        sel_values.put(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.COLUMN_SELECTION_TYPE, type.getValue());
                        id = db.insert(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.TABLE_NAME, null, sel_values);
                        id = db.insert(com.coderming.movieapp.data.MovieContract.MovieSelectionEntry.TABLE_NAME, null, sel_values);
                        if (id != -1) {
                            numrow++;
                        }
                    } else {
                        Log.w(LOG_TAG, "failed to insert movie " + Integer.toString(movieId));
                    }
                }
                db.setTransactionSuccessful();
            } catch (IllegalStateException esex) {
                Log.w(LOG_TAG, "bulkInsert db.setTransactionSuccessful() caught IllegalStateException. numrow=" + Integer.toString(numrow));
            } finally {
                try {
                    db.endTransaction();
                } catch (IllegalStateException esex) {
                    Log.w(LOG_TAG, "bulkInsert db.endTransaction() caught IllegalStateException. numrow=" + Integer.toString(numrow));
                }
//                if ((db != null) && db.isOpen())
//                    db.close();
            }
            return numrow;
        } else {
            return super.bulkInsert(uri, values);
        }
    }
}
