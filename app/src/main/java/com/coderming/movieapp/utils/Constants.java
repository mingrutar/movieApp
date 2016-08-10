package com.coderming.movieapp.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by linna on 6/6/2016.
 */
public class Constants {
    private static final AtomicInteger uniqueNum = new AtomicInteger(0);
    public static int nextId() {
        return uniqueNum.getAndIncrement();
    }
    public static final String FORMATTER_PICASSO_IMAGE_LOADER = "http://image.tmdb.org/t/p/w%s/%s";
    public static final String DETAIL_URI = "detail_uri";
    public static final String MORE_DETAIL_URI = "more_detail_uri";

    // movie tags
    public static final String TAG_POSTER_PATH = "poster_path";
    public static final String TAG_OVERVIEW = "overview";
    public static final String TAG_RELEASE_DATE = "release_date";
    // movies
    public static final String TAG_ID = "id";
    public static final String TAG_ORIGINAL_TITLE = "original_title";
    public static final String TAG_ORIGINAL_LANGUAGE = "original_language";
    public static final String TAG_TITLE = "title";
    public static final String TAG_BACKDROP_PATH = "backdrop_path";
    public static final String TAG_POPULARITY = "popularity";
    public static final String TAG_VOTE_COUNT = "vote_count";
    public static final String TAG_VOTE_AVERAGE = "vote_average";

    public static final String TAG_page = "page";
    public static final String TAG_RESULTS = "results";
    public static final String TAG_TOTAL_RESULTS = "total_results";
    public static final String TAG_TOTAL_PAGES = "total_pages";
    // videos
    public static final String TAG_NAME = "name";
    public static final String TAG_SITE = "site";
    public static final String TAG_KEY = "key";
    // images
    public static final String TAG_ASPECT_RATIO = "aspect_ratio";
    public static final String TAG_FILE_PATH = "file_path";
    public static final String TAG_HEIGHT = "height";
    public static final String TAG_WIDTH = "width";
    //review
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_CONTENT = "content";

}
