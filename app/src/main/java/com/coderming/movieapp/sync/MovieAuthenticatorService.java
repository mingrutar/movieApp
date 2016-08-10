package com.coderming.movieapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by linna on 6/2/2016.
 */
public class MovieAuthenticatorService extends Service {
    private static final String LOG_TAG = MovieAuthenticatorService.class.getSimpleName();

    private MovieAuthenticator mMovieAuthenticator;

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate called");
        mMovieAuthenticator = new MovieAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "onBind called");
        return mMovieAuthenticator.getIBinder();
    }
}
