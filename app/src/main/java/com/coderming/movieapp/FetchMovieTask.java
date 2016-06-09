package com.coderming.movieapp;

import android.os.AsyncTask;
import android.util.Log;

import com.coderming.movieapp.model.MovieSource;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by linna on 5/4/2016.
 */
public class FetchMovieTask extends AsyncTask<String, Void, MovieSource> {
    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    private MovieMainFragment mMovieMainFragment;

    public FetchMovieTask(MovieMainFragment movieMainFragment) {
        mMovieMainFragment = movieMainFragment;
    }

    // https://api.themoviedb.org/3/movie/popular?api_key=
    private String getMovies(String urlStr) {
//        Log.v(LOG_TAG, "urlString="+urlStr);
        HttpURLConnection httpConnection = null;    // android API vs HTTPClient
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String jsonStr = null;
        try {
            httpConnection = (HttpURLConnection) new URL(urlStr).openConnection();
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
                    jsonStr = buffer.toString();
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return jsonStr;
    }
    /***
     *    https://api.themoviedb.org/3/movie/popular?api_key=cdf5f229abf9f31735694c38c48a67ac
     *    https://api.themoviedb.org/3/movie/top_rated?api_key=cdf5f229abf9f31735694c38c48a67ac
     * @param params
     * @return List<MovieItem>
     */
    @Override
    protected MovieSource doInBackground(String... params) {
        try {
            //TODO: params[1] as page number?
            String jsonStr = getMovies(params[0]);
            if (jsonStr != null) {
                return MovieSource.parseFromJSON(jsonStr);
            } else {
                return null;
            }
        } catch (JSONException jex) {
            Log.e(LOG_TAG, "Error", jex);
        }
        return null;
    }

    @Override
    protected void onPostExecute(MovieSource movieSource) {
        if (movieSource != null) {
            mMovieMainFragment.updateAdapter(movieSource);
            mMovieMainFragment = null;
        }
    }
}
