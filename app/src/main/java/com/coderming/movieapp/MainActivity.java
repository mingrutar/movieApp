package com.coderming.movieapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.data.MovieContract.MovieSelectionType;
import com.coderming.movieapp.sync.MovieSyncAdapter;
import com.coderming.movieapp.utils.Constants;
import com.coderming.movieapp.utils.DataRetriever;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity implements MovieRecyclerViewAdapter.ItemClickedCallback  {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String SELECTED_FRAG = "selectedFragment";
    public static final String PAGE_DATA_URI = "page_data_uri";

    public static final long DAY_IN_MILLISEC = 24 * 60 * 60 * 1000;          // TODO change to 3*60*60, 3hours;

    public static final String MOVIEMAIN_TAG = "MOVIEMAIN_TAG";
    public static final String DETAILFRAGMENT_TAG = "DETAILFRAGMENT_TAG";
    public static final String LAST_SYNC_TIME = "LAST_SYNC_TIME";

    private Spinner mSpinner;
    private int mSelectedFrag;
    private ViewPager mViewPager;
    private boolean mTwoPane;

    static final  MovieSelectionType[] listTypes =  new MovieSelectionType[] {MovieSelectionType.Popular,
            MovieSelectionType.TopRated, MovieSelectionType.Favorite} ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTwoPane = ( findViewById(R.id.detail_container) != null );
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(adapter);
        if (savedInstanceState == null) {
            if (mTwoPane) {
              getSupportFragmentManager().beginTransaction()
                        .add(R.id.detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }  else {
                getSupportActionBar().setElevation(0f);
            }
        } else if (savedInstanceState.containsKey(LAST_SYNC_TIME)){
            DataRetriever.sLastMovieSyncTime = savedInstanceState.getLong(LAST_SYNC_TIME);
        }
        if (isSyncTime(this)) {
            Log.v(LOG_TAG, "++++ calling syncImmediately ");
            MovieSyncAdapter.syncImmediately(this);
        }
        registerReceiver(mBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    private boolean isSyncTime(Context context) {
        return ((System.currentTimeMillis() > DataRetriever.sLastMovieSyncTime + DAY_IN_MILLISEC )
                && DataRetriever.isNetworkAvailable(this)) ;
    }
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "++ onReceive received intent");
            if (isSyncTime(context)) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;  // not in use
                MovieSyncAdapter.syncImmediately(context);
            }
        }
    };
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(LAST_SYNC_TIME, DataRetriever.sLastMovieSyncTime);
    }
    @Override
    protected void onPause() {
        Log.v(LOG_TAG, String.format("----onPause mSelectedFrag=%d,mSinner?=%s", mSelectedFrag,(mSpinner != null)));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SELECTED_FRAG, mSelectedFrag);
        editor.commit();
        super.onPause();
    }
    @Override
    public void onResume() {
        Log.v(LOG_TAG, String.format("----onResume mSelectedFrag=%d,mSinner?=%s", mSelectedFrag,(mSpinner != null)));
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.mSelectedFrag = prefs.getInt(SELECTED_FRAG, 0);
//        onPageSelected(mSelectedFrag);
        if (mSpinner != null) {
            mSpinner.setSelection(mSelectedFrag);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(LOG_TAG, String.format("----onCreateOptionsMenu, mSelectedFrag=%d", mSelectedFrag));
        getMenuInflater().inflate(R.menu.movie_main, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        android.support.v7.app.ActionBar actionBar= this.getSupportActionBar();
        final Context themedContext = actionBar.getThemedContext();
        ArrayAdapter spinnerAdapter =  ArrayAdapter.createFromResource(
                themedContext, R.array.movieListOrderValue, android.R.layout.simple_spinner_dropdown_item); //  create the adapter from a StringArray
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner = (Spinner) MenuItemCompat.getActionView(item);
        mSpinner.setAdapter(spinnerAdapter); // set the adapter to provide layout of rows and content
        mSpinner.setSelection(mSelectedFrag);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                if (item != null) {
                    mSelectedFrag = pos;
                    onPageSelected(pos);
                } else {
                    Toast.makeText(themedContext, "Selected unknown", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {
            Intent settingIntent  = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;               // stop here
        }
        return super.onOptionsItemSelected(item);
    }
    public void onPageSelected(int position) {
        Log.v(LOG_TAG, "****** onPageSelected called, pos="+Integer.toString(position));
        mViewPager.setCurrentItem(position);
        mViewPager.getAdapter().notifyDataSetChanged();
        // TODO: remove
        String name =   ((MyFragmentPagerAdapter)mViewPager.getAdapter()).getPageTitle(position);
        TextView textView = (TextView) findViewById(R.id.page_name);
        String pname = "Page " + name;
        textView.setText(pname);
        mViewPager.requestLayout();
    }

    private void showinDetailPane(Uri uri) {
        DetailFragment df = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.DETAIL_URI, uri);
        df.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.detail_container, df, DETAILFRAGMENT_TAG)
                .commit();
    }
    @Override
    public void initialSelection(Uri uri) {
        Log.v(LOG_TAG, "+++BV+++ mTwoPane="+mTwoPane);
        if (mTwoPane) {
            showinDetailPane(uri);
        }
    }
    @Override
    public void onItemClicked(Uri uri) {
        Log.v(LOG_TAG, "+++BV+++ mTwoPane="+mTwoPane);
        if (mTwoPane) {
            showinDetailPane(uri);
        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class).setData(uri);
            startActivity(detailIntent);
        }
    }

    /**
     * MyFragmentPagerAdapter
     */
    static class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private static final String LOG_TAG = MyFragmentPagerAdapter.class.getSimpleName();
        private Map<MovieSelectionType, Fragment> mFramentsMap;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            mFramentsMap = new ConcurrentHashMap<>();
        }
        @Override
        public String getPageTitle(int pos) {
            if (pos < listTypes.length) {
                return listTypes[pos].toString();
            } else {
                Log.w(LOG_TAG, "----- getPageTitle, wrong pos =" + Integer.toString(pos));
                return null;
            }
        }
        @Override
        public int getCount() {
//            Log.v(LOG_TAG, "----- getCount, size=" + Integer.toString(mFragments.size()));
            return listTypes.length;
        }
        public Fragment getItem(int position) {
            if (position < listTypes.length) {
                MovieSelectionType key = listTypes[position];
                boolean newFrag = !mFramentsMap.containsKey(key);
                if (newFrag) {
                    MovieMainFragment mmf = new MovieMainFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(PAGE_DATA_URI, MovieContract.MovieEntry.getTypeUri(key));
                    mmf.setArguments(args);
                    mFramentsMap.put(key, mmf);
                }
                Log.v(LOG_TAG, String.format("!!!!!! getItem called, key=%s pos=%d, newFrag=%s",
                        key, position, newFrag));
                return mFramentsMap.get(key);
            } else {
                Log.w(LOG_TAG, "!!!!ERROR invalid position=" + Integer.toString(position));
                return null;
            }
        }
    }
}
