package com.coderming.movieapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.coderming.movieapp.R;
import com.coderming.movieapp.data.MovieContract.MovieSelectionType;
import com.coderming.movieapp.utils.DataRetriever;
import com.coderming.movieapp.utils.Utilities;
/**
 * Created by linna on 6/2/2016.
 */
public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    private static final String MOVIE_ID = "movie_id";
    // Interval at which to sync with the weather, in milliseconds ?. seems in second
    public static final int SYNC_INTERVAL = 24 * 60 * 60;             // TODO change to 3*60*60, 3hours;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        if (extras.containsKey(MOVIE_ID)) {
            long movieDbId = extras.getLong(MOVIE_ID);
            DataRetriever.retrieveDetails (getContext(), movieDbId);
        }  else {
            int totalPages = -1;
            int[] ret = null;
            for (MovieSelectionType type : new MovieSelectionType[]{MovieSelectionType.Popular,
                    MovieSelectionType.TopRated}) {
                int currentPage = 1;
                ret = DataRetriever.retrieveMovies(getContext(), type, currentPage);
                if (ret != null) {
                    totalPages = Math.min(ret[1], Utilities.getRecordLimmit(getContext(), type));
                    currentPage++;
                    for (; currentPage <= totalPages; currentPage++) {
                        DataRetriever.retrieveMovies(getContext(), type, currentPage);
                    }
                } else {
                }
            }
        }
    }

    /*** Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.v(LOG_TAG, "++++M+++ syncImmediately (movie) is called");
        syncImmediately(context, new Bundle());
    }
    public static void syncImmediately(Context context, long movieId) {
        Log.v(LOG_TAG, "++++D+++ syncImmediately (detail) is called, movieId="+Long.toString(movieId));
        Bundle bundle = new Bundle();
        bundle.putLong(MOVIE_ID, movieId);
        syncImmediately(context, bundle);
    }
    private static void  syncImmediately(Context context, Bundle bundle) {
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        Account acct = getSyncAccount(context);
        ContentResolver.requestSync(acct, context.getString(R.string.content_authority), bundle);
    }
    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * *** delete the account to schedule the task.
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /** If you don't set android:syncable="true" in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1) here.
             */
            Account account = getSyncAccount(context);  // Since we've created an account
            String authority = context.getString(R.string.content_authority);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // we can enable inexact timers in our periodic sync
                SyncRequest request = new SyncRequest.Builder().
                        syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME).
                        setSyncAdapter(account, authority).
                        setExtras(new Bundle()).build();
                ContentResolver.requestSync(request);
            } else {
                ContentResolver.addPeriodicSync(account,
                        authority, new Bundle(), SYNC_INTERVAL);
            }
            // Without calling setSyncAutomatically, our periodic sync will not be enabled.
            ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
            // Finally, let's do a sync to get things started
// ??              syncImmediately(context);
        }
        return newAccount;
    }
}
