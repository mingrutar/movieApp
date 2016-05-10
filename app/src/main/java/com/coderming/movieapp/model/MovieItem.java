package com.coderming.movieapp.model;

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

    public String getOriginalLanguage() {
        return mOriginalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        mOriginalLanguage = originalLanguage;
    }

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        mOriginalTitle = originalTitle;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public double getPopularuty() {
        return mPopularuty;
    }

    public void setPopularuty(double popularuty) {
        mPopularuty = popularuty;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        mPosterPath = posterPath;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public double getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        mVoteAverage = voteAverage;
    }

    public int getVoteCount() {
        return mVoteCount;
    }

    public void setVoteCount(int voteCount) {
        mVoteCount = voteCount;
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        mBackdropPath = backdropPath;
    }

    double mVoteAverage;
    int mVoteCount;

    MovieItem() {}
}
