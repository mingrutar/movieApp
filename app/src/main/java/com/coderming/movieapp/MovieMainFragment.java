package com.coderming.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.coderming.movieapp.model.MovieItem;
import com.coderming.movieapp.model.MovieSource;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment {
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    static final String UrlBase = "https://api.themoviedb.org/3/movie/";

    private GridViewAdapter mAdapter;
    private MovieSource mMovieDb;
    private ArrayAdapter mSpinnerAdapter;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;
    private Spinner mSpinner;
    private String mSortby;

    public MovieMainFragment() {
    }

    private void updateMovieInfo() {
        int tagId=  mSortby.equals(getString(R.string.sortby_popular)) ?
                R.string.tag_sortby_popular : R.string.tag_sortby_top_rated;
        String url = UrlBase + getString(tagId);
        Uri buildUri = Uri.parse(url).buildUpon()
                .appendQueryParameter(getString(R.string.tag_api_key), getString(R.string.themoviedb_api_key)).build();
        new FetchMovieTask(this).execute(buildUri.toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "++++onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    // TODO how to deal with 4K screen?
    private void calcNumColumes( GridView gridView) {
        Resources res = getActivity().getResources();
        Configuration configuration = res.getConfiguration();
        int smallScreenWidthDp = configuration.smallestScreenWidthDp;

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float colWidth = res.getDimension(R.dimen.moviedb_image_width_185) + res.getDimensionPixelSize(R.dimen.dimen_4dp) ;
        int posterWidthDp = Math.round( colWidth / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
// TODO: use commented numCol all dp will get tiny imagess
//        int numCol = (int) (smallScreenWidthDp  / (posterWidthDp));
        int numCol =  Math.round(smallScreenWidthDp/colWidth);
        gridView.setNumColumns(numCol);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_main, container, false);
        Context context = getContext();
        mAdapter = new GridViewAdapter( context, R.layout.grid_item );
        GridView gridView = (GridView) rootView.findViewById(R.id.movie_grid);
        gridView.setAdapter(mAdapter);
        calcNumColumes(gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieItem item = mAdapter.getItem(position);
                Intent detailIntenet = new Intent(getContext(), DetailActivity.class);
                detailIntenet.putExtra("MovieItem", item);
                startActivity(detailIntenet);
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "++++onResume");
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mSortby = prefs.getString(getString(R.string.pref_sortby_key), getString(R.string.sortby_popular));
        if (mSpinner != null) {
            int pos = (mSortby.equals(getString(R.string.sortby_top_rated))) ? 1 : 0;
            mSpinner.setSelection(pos);
        } else {
            updateMovieInfo();
        }
    }

    @Override
    public void onPause() {
        Log.v(LOG_TAG, "++++onPause");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.pref_sortby_key), mSortby);
        editor.commit();
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_main_fragment, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        android.support.v7.app.ActionBar actionBar=((AppCompatActivity)getActivity() ).getSupportActionBar();
        final Context themedContext = actionBar.getThemedContext();
        mSpinnerAdapter = ArrayAdapter.createFromResource(
                themedContext, R.array.movieListOrderValue, android.R.layout.simple_spinner_dropdown_item); //  create the adapter from a StringArray
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner = (Spinner) MenuItemCompat.getActionView(item);
        mSpinner.setAdapter(mSpinnerAdapter); // set the adapter to provide layout of rows and content
        int pos = (mSortby.equals(getString(R.string.sortby_top_rated))) ? 1 : 0;
        mSpinner.setSelection(pos);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                if (item != null) {
                    mSortby = item.toString();
                    updateMovieInfo();
                } else {
                    Toast.makeText(themedContext, "Selected unknown", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {
            Intent settingIntent  = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingIntent);
            return true;               // stop here
        }
        return super.onOptionsItemSelected(item);
    }
    public void updateAdapter(MovieSource movieSource) {
        mMovieDb = movieSource;
        mAdapter.resetList(movieSource.getItemList());
    }
}
