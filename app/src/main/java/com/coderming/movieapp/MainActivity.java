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

import com.coderming.movieapp.data.MovieContract.MovieEntry;
import com.coderming.movieapp.data.MovieContract.MovieSelectionType;
import com.coderming.movieapp.sync.MovieSyncAdapter;
import com.coderming.movieapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieRecyclerViewAdapter.ItemClickedCallback  {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String SELECTED_FRAG = "selectedFragment";
    public static final String PAGE_DATA_URI = "page_data_uri";

    public static final String MOVIEMAIN_TAG = "MOVIEMAIN_TAG";
    public static final String DETAILFRAGMENT_TAG = "DETAILFRAGMENT_TAG";

    private Spinner mSpinner;
    private int mSelectedFrag;
    private ViewPager mViewPager;
    private boolean mTwoPane;

    private MovieSelectionType[] listTypes =  new MovieSelectionType[] {MovieSelectionType.Popular,
            MovieSelectionType.TopRated, MovieSelectionType.Favorite} ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTwoPane = ( findViewById(R.id.detail_container) != null );
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        for ( MovieSelectionType type :  listTypes) {
            MovieMainFragment mmf = new MovieMainFragment();
            Bundle args = new Bundle();
            args.putParcelable(PAGE_DATA_URI, MovieEntry.getTypeUri(type));
            mmf.setArguments(args);
            adapter.addFragment(mmf, type.toString());
        }
        mViewPager.setAdapter(adapter);
        if (savedInstanceState == null) {
            if (mTwoPane) {
              getSupportFragmentManager().beginTransaction()
                        .add(R.id.detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }  else {
                getSupportActionBar().setElevation(0f);
            }
        }
        registerReceiver(mBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "++ onReceive received intent");
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork =cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;  // not in use
                if (activeNetwork.isConnectedOrConnecting()) {
// TODO: ??                   callSync();
                }
            }
        }
    };

    void callSync() {
        Log.v(LOG_TAG, "++++ calling syncImmediately ");
        MovieSyncAdapter.syncImmediately(this);
        // TODO: do I need restartLoader ?
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

    }

    @Override
    public void onItemClicked(Uri uri) {
        if (mTwoPane) {
            DetailFragment df = new DetailFragment();
            Bundle args = new Bundle();
            args.putParcelable(Constants.DETAIL_URI, uri);
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();
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
        private List<Fragment> mFragments;
        private List<String> mFragmentTitles;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new ArrayList<>();
            mFragmentTitles = new ArrayList<>();
        }
        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }
        @Override
        public String getPageTitle(int pos) {
            return (pos < mFragmentTitles.size()) ? mFragmentTitles.get(pos) : null;
        }
        @Override
        public int getCount() {
//            Log.v(LOG_TAG, "----- getCount, size=" + Integer.toString(mFragments.size()));
            return mFragments.size();
        }
        /**
         * Return the Fragment associated with a specified position.
         *
         * @param position
         */
        @Override
        public Fragment getItem(int position) {
            if (position < mFragments.size()) {
//                Log.v(LOG_TAG, "----- getItem called, pos=" + Integer.toString(position));
                return mFragments.get(position);
            } else {
                Log.v(LOG_TAG, "invalid position " + Integer.toString(position));
                return null;
            }
        }
    }
}
