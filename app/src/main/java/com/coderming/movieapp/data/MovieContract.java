package com.coderming.movieapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by linna on 6/1/2016.
 */
public class MovieContract  {
    public static final String CONTENT_AUTHORITY = "com.coderming.movieapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    public enum MovieSelectionType {
        Popular("popular"), TopRated("top_rated"), Favorite("favorite");
        private final String type;
        MovieSelectionType(String str) {type = str;}
        public String toString() { return type; }
        public int getValue() {
            return type.equals("popular") ? 0 : (type.equals("top_rated") ? 1 : 2);
        }
    }

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String VIEW_POPULAR = "popular_movie";
        public static final String VIEW_TOP_RATED = "top_rated_movie";
        public static final String VIEW_FAVORITE = "favorite_movie";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_POPULARUTY = "popularuty";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";

        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static final Uri CONTENT_POPULAR_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME)
                .appendPath(MovieSelectionType.Popular.toString()).build();
        public static final Uri CONTENT_TOP_RATES_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME)
                .appendPath(MovieSelectionType.TopRated.toString()).build();
        public static final Uri CONTENT_FAVORITE_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME)
                .appendPath(MovieSelectionType.Favorite.toString()).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
    public static final class DetailEntry implements BaseColumns {
        public static final String TABLE_NAME = "detail";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TYPE = "type";            // supoorted video, image, review
        public static final String COLUMN_DETAIL_DATA = "data";

        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        //
        public static Uri buildUri(long movieDbId) {
            return ContentUris.withAppendedId(CONTENT_URI, movieDbId);
        }

    }
    public static final class MovieSelectionEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie_selection";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_SELECTION_TYPE = "selection_type";
    }
}
