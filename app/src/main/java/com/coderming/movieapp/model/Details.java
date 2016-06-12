package com.coderming.movieapp.model;

import com.coderming.movieapp.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linna on 6/5/2016.
 */
public class Details {
    private Details(){;}

    public static List<Video>  parseVideos(String jsonStr) throws JSONException {
        List<Video> ret = new ArrayList<>();
        JSONArray jarr = new JSONArray(jsonStr);
        JSONObject jobj;
        Video video;
        for (int i = 0; i < jarr.length(); i++ ) {
            jobj = jarr.getJSONObject(i);
            String id = jobj.getString(Constants.TAG_ID);
            video = new Video(id);
            video.mName = jobj.getString(Constants.TAG_NAME);
            video.mKey = jobj.getString(Constants.TAG_KEY);
            video.mSite = jobj.getString(Constants.TAG_SITE);
            ret.add(video);
        }
        return ret;
    }
    public static List<Image>  parseImages(String jsonStr) throws JSONException {
        List<Image> ret = new ArrayList<>();
        JSONArray jarr = new JSONArray(jsonStr);
        JSONObject jobj;
        Image image;
        for (int i = 0; i < jarr.length(); i++ ) {
            jobj = jarr.getJSONObject(i);
            String id = jobj.getString(Constants.TAG_ID);
            image = new Image(id);
            image.mPath =  jobj.getString(Constants.TAG_FILE_PATH);
            image.mHeight = jobj.getInt(Constants.TAG_HEIGHT);
            image.mWidth = jobj.getInt(Constants.TAG_WIDTH);
            ret.add(image);
        }
        return ret;
    }
    public static List<Review>  parseReviews(String jsonStr) throws JSONException {
        List<Review> ret = new ArrayList<>();
        JSONArray jarr = new JSONArray(jsonStr);
        JSONObject jobj;
        Review review;
        for (int i = 0; i < jarr.length(); i++ ) {
            jobj = jarr.getJSONObject(i);
            String id = jobj.getString(Constants.TAG_ID);
            review = new Review(id);
            review.mAuthor =  jobj.getString(Constants.TAG_AUTHOR);
            review.mContent = jobj.getString(Constants.TAG_CONTENT);
            ret.add(review);
        }
        return ret;
    }

    public static class Video {
 /* https://api.themoviedb.org/3/movie/244786/videos?api_key=cdf5f229abf9f31735694c38c48a67ac
 * [{"id":"543d8f250e0a266f7d00059f","iso_639_1":"en","iso_3166_1":"US","key":"7d_jQycdQGo","name":"Whiplash trailer","site":"YouTube","size":360,"type":"Trailer"},]}
 */
        public static final String FILTER_TAG = "type";
        public static final String FILTER_VAL = "Trailer";

        String mId;
        String mName;
        String mKey;
        String mSite;

        Video(String id) { mId = id;}
        public String getName() {return mName; }
        public String getVideoKey() { return mKey; }
    }
    public static class Image {
        /* https://api.themoviedb.org/3/movie/244786/images?api_key=cdf5f229abf9f31735694c38c48a67ac
        * (.. "posters":[ ]
        * [{"aspect_ratio":0.666666666666667,"file_path":"/lIv1QinFqz4dlp5U4lQ6HaiskOZ.jpg","height":2100,"iso_639_1":"en","vote_average":6.35531135531136,"vote_count":67,"width":1400},
        */
        static final String FILTER_TAG = "width";
        static final int FILTER_VAL = 1000;

        String mId;
        String mPath;
        int mWidth;
        int mHeight;

        Image(String id) { mId = id;}
        public String getPath() {return mPath; }
    }
    public static class Review {
        /** https://api.themoviedb.org/3/movie/244786/reviews?api_key=cdf5f229abf9f31735694c38c48a67ac
         * {"id":"56ab260cc3a3681c54001f8a","author":"Andres Gomez","content":"Fantastic...performances.\r\n\r\nJust sit ..",...}
         */
        String mId;
        String mAuthor;
        String mContent;

        Review(String id) { mId = id;}
        public String getAuthor() {return mAuthor; }
        public String getContent() {return mContent; }
    }
}
