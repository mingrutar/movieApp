package com.coderming.movieapp;

import java.io.Serializable;

/**
 * Created by linna on 5/3/2016.
 */
public class MovieItem implements Serializable {
    int mId;
    String mOverview;
    String mTitle;
    String mReleaseDate;
    String mPosterPath;             // for main activity
    String mBackdropPath;
    String mOriginalTitle;
    String mOriginalLanguage;
    double mPopularuty;
    double mVoteAverage;
    int mVoteCount;

    MovieItem() {}
}
