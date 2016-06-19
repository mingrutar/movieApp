package com.coderming.movieapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.coderming.movieapp.BuildConfig;
import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.data.MovieContract.DetailEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by linna on 6/2/2016.
 */
public class DataRetriever {
    private static final String LOG_TAG = DataRetriever.class.getSimpleName();
    //
    public static long sLastMovieSyncTime;

    //  detail tags
    public static final String[] SUPPORTED_DETAIL_TYPES = new String[] {"videos", "reviews",  "images",};
    private static  final Map<String, String> SetailTypeJsonTag =  new HashMap<>();
    static {
        SetailTypeJsonTag.put(SUPPORTED_DETAIL_TYPES[0], "results");
        SetailTypeJsonTag.put(SUPPORTED_DETAIL_TYPES[1], "results");
        SetailTypeJsonTag.put(SUPPORTED_DETAIL_TYPES[2], "posters");
    }
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String mMovieUri = "https://api.themoviedb.org/3/movie/%s?page=%d&api_key=%s";
    private static final String mDetailUri = "https://api.themoviedb.org/3/movie/%d/%s?api_key=%s";

    public static void retrieveDetails(Context context, long movieDbID ) {
        int movieId = Utilities.getMovieId(context, movieDbID) ;
        if (movieId != -1) {
            ContentValues values;
            for (String data : SUPPORTED_DETAIL_TYPES) {
                try {
                    String urlStr = String.format(mDetailUri, movieId, data, BuildConfig.MOVIE_DB_API_KEY);
                    Log.v(LOG_TAG, "++++s+++ retrieveDetails: uri=" + urlStr);
                    String jsonStr = retrieveData(context, new URL(urlStr));
                    if (jsonStr != null) {
                        JSONObject jobj = new JSONObject(jsonStr);
                        JSONArray jarr = jobj.getJSONArray(SetailTypeJsonTag.get(data));
                        if (jarr.length() > 0) {
                            values = new ContentValues();
                            values.put(DetailEntry.COLUMN_MOVIE_ID, movieDbID);
                            values.put(DetailEntry.COLUMN_DETAIL_DATA, jarr.toString());
                            values.put(DetailEntry.COLUMN_TYPE, data);
                            Uri uri = context.getContentResolver().insert(DetailEntry.CONTENT_URI, values);
                            Log.v(LOG_TAG, String.format("retrieveDetails inserted movieId=%d, type=%s =>uri=%s",
                                    movieDbID, data, uri));
                        }
                    }
                } catch (MalformedURLException mfe) {
                    Log.w(LOG_TAG, "Error: retrieveDetail  "+mfe.getMessage(), mfe);
                } catch (JSONException jex) {
                    Log.w(LOG_TAG, "Error: retrieveDetail  "+jex.getMessage(), jex);
                } catch (RuntimeException rex) {
                    Log.w(LOG_TAG, "Error: retrieveDetail "+rex.getMessage(), rex);
                }
            }
        } else {
            Log.w(LOG_TAG, "Could not find movie id for movie DB id "+Long.toString(movieDbID));
        }
    }

    private static int[] parseJson2Db(Context context, String jsonStr, MovieContract.MovieSelectionType type) throws JSONException {
        JSONObject jobj = new JSONObject(jsonStr);
        Log.v(LOG_TAG, "++++s+++ parseJson2Db. json str length= "+Integer.toString(jsonStr.length()));
        int[] ret = new int[3];
        ret[0] = jobj.getInt(Constants.TAG_page);             //currentPage
        ret[1] = jobj.getInt(Constants.TAG_TOTAL_PAGES);      //totalPages
        ret[2] = jobj.getInt(Constants.TAG_TOTAL_RESULTS);    //totalResults

        JSONArray jarr = jobj.getJSONArray(Constants.TAG_RESULTS);
        SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        List<ContentValues> movies = new ArrayList<>();
        for (int i = 0; i < jarr.length(); i++ ) {
            jobj = jarr.getJSONObject(i);
            int moview_id = jobj.getInt(Constants.TAG_ID);
            ContentValues cv = new ContentValues();
            cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, moview_id);
            cv.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, jobj.getString(Constants.TAG_POSTER_PATH));
            cv.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, jobj.getString(Constants.TAG_OVERVIEW));
            try {
                Date date = dateFormater.parse(jobj.getString(Constants.TAG_RELEASE_DATE));
                cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, date.getTime());
            } catch (ParseException pex) {
                Log.w(LOG_TAG, "failed to parse release time");
                cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, 0);
            }
            cv.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, jobj.getString(Constants.TAG_ORIGINAL_TITLE));
            cv.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, jobj.getString(Constants.TAG_ORIGINAL_LANGUAGE));
            cv.put(MovieContract.MovieEntry.COLUMN_TITLE, jobj.getString(Constants.TAG_TITLE));
            cv.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, jobj.getString(Constants.TAG_BACKDROP_PATH));
            cv.put(MovieContract.MovieEntry.COLUMN_POPULARUTY, jobj.getDouble(Constants.TAG_POPULARITY));
            cv.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, jobj.getInt(Constants.TAG_VOTE_COUNT));
            cv.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, jobj.getDouble(Constants.TAG_VOTE_AVERAGE));
            movies.add(cv);
        }
        if (movies.size() > 0) {
            Uri uri = type.equals(MovieContract.MovieSelectionType.Popular) ?
                    MovieContract.MovieEntry.CONTENT_POPULAR_URI : MovieContract.MovieEntry.CONTENT_TOP_RATES_URI;
            ContentValues[] contentValues = new ContentValues[movies.size()];
            movies.toArray(contentValues);
            int inserted = context.getContentResolver().bulkInsert(uri, contentValues);
            Log.v(LOG_TAG, String.format("+++ %d record inserted, %d moview received", inserted, movies.size()));
        }
        return ret;
    }
    @Nullable
    public static int[] retrieveMovies(Context context, MovieContract.MovieSelectionType type, int page) {
        // Will contain the raw JSON response as a string.
        try {
            String urlStr = String.format(mMovieUri, type.toString(), page, BuildConfig.MOVIE_DB_API_KEY);
            Log.v(LOG_TAG, "++++s+++ retrieveMovies: uri=" + urlStr);
            String jsonStr = retrieveData(context, new URL(urlStr));
            if (jsonStr != null) {
                sLastMovieSyncTime = System.currentTimeMillis();
                return parseJson2Db(context, jsonStr, type);
            }
        } catch (JSONException jsex) {
            Log.e(LOG_TAG, "Error retrieveMovies "+jsex.getMessage(), jsex);
        } catch (MalformedURLException mfe) {
            Log.e(LOG_TAG, "Error retrieveMovies "+ mfe.getMessage(), mfe);
        }
        return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.isConnected();
    }
    @Nullable
    private static String retrieveData(Context context, URL url) {
        if (isNetworkAvailable(context)) {
            HttpURLConnection httpConnection = null;    // android API vs HTTPClient
            BufferedReader reader = null;
            String jsonStr = null;
            try {
                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = httpConnection.getInputStream();
                if (inputStream != null) {
                    StringBuilder buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }
                    if (buffer.length() != 0) {
                        Log.v(LOG_TAG, "++++ got " + Integer.toString(buffer.length()) + " bytes from remote: url=" + url);
                        return buffer.toString();
                    } else {
                        Log.v(LOG_TAG, "++++ got 0 bytes from remote: url=" + url);
                    }
                }
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "====Exception retrieveMovies " + ioe.getMessage(), ioe);
            } finally {
                if (httpConnection != null) {
                    httpConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Exception retrieveMovies " + e.getMessage(), e);
                    }
                }
            }
        } else {
            Log.i(LOG_TAG, "Notwork is unavailable");
        }
        return null;
    }
}
