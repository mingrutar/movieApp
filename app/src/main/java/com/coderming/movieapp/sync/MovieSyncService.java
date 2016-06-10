package com.coderming.movieapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by linna on 6/2/2016.
 */
public class MovieSyncService extends Service {
    private static final String LOG_TAG = MovieSyncService.class.getSimpleName();

    private static MovieSyncAdapter mMovieSyncAdapter;
    private static final int[] mLock = new int[1];

    @Override
    public void onCreate() {
        Log.v(LOG_TAG, "onCreate called");
        synchronized (mLock) {
            if (mMovieSyncAdapter == null) {
                mMovieSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "onBind called");
        return mMovieSyncAdapter.getSyncAdapterBinder();
    }
}
