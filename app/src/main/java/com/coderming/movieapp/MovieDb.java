package com.coderming.movieapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by linna on 5/4/2016.
 */
public class MovieDb {
    //https://api.themoviedb.org/3/movie/top_rated?api_key=cdf5f229abf9f31735694c38c48a67ac&page=1&language=fr
    static final String TAG_POSTER_PATH = "poster_path";
    static final String TAG_OVERVIEW = "overview";
    static final String TAG_RELEASE_DATE = "release_date";

    static final String TAG_ID = "id";
    static final String TAG_ORIGINAL_TITLE = "original_title";
    static final String TAG_ORIGINAL_LANGUAGE = "original_language";
    static final String TAG_TITLE = "title";
    static final String TAG_BACKDROP_PATH = "backdrop_path";
    static final String TAG_POPULARITY = "popularity";
    static final String TAG_VOTE_COUNT = "vote_count";
    static final String TAG_VOTE_AVERAGE = "vote_average";

    static final String TAG_page = "page";
    static final String TAG_RESULTS = "results";
    static final String TAG_TOTAL_RESULTS = "total_results";
    static final String TAG_TOTAL_PAGES = "total_pages";

    static final String DATE_FORMAT = "yyyy-MM-dd";

    List<MovieItem>  mItemList;
    int mCurrentPage;
    int mTotalPages;
    int mTotalResults;

    MovieDb() {
        mItemList = new ArrayList<>();
    }
    static public MovieDb parseFromJSON(String jsonStr) throws JSONException {
        MovieDb ret = new MovieDb();
        JSONObject jobj = new JSONObject(jsonStr);
        ret.mCurrentPage = jobj.getInt(TAG_page);
        ret.mTotalPages = jobj.getInt(TAG_TOTAL_PAGES);
        ret.mTotalResults = jobj.getInt(TAG_TOTAL_RESULTS);

        JSONArray jarr = jobj.getJSONArray(TAG_RESULTS);
        SimpleDateFormat dateFormater = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        for (int i = 0; i < jarr.length(); i++ ) {
            MovieItem item = new MovieItem();
            jobj = jarr.getJSONObject(i);
            item.mPosterPath = jobj.getString(TAG_POSTER_PATH);
            item.mOverview = jobj.getString(TAG_OVERVIEW);
            item.mReleaseDate =jobj.getString(TAG_RELEASE_DATE);
            item.mId = jobj.getInt(TAG_ID);
            item.mOriginalTitle = jobj.getString(TAG_ORIGINAL_TITLE);
            item.mOriginalLanguage = jobj.getString(TAG_ORIGINAL_LANGUAGE);
            item.mTitle = jobj.getString(TAG_TITLE);
            item.mBackdropPath = jobj.getString(TAG_BACKDROP_PATH);
            item.mPopularuty = jobj.getDouble(TAG_POPULARITY);
            item.mVoteCount = jobj.getInt(TAG_VOTE_COUNT);
            item.mVoteAverage = jobj.getDouble(TAG_VOTE_AVERAGE);
            ret.mItemList.add(item);
        }
        return ret;
    }
}
