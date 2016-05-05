package com.coderming.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

//TODO: add menu forsort
/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment {
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();
    private static final boolean DEBUG = false;
    static final String PopularPath = "popular";
    static final String TopRatedPath = "top_rated";

    static final String UrlBase = "https://api.themoviedb.org/3/movie/";
    //TODO: remove when submit
    static final String myApiKey = "cdf5f229abf9f31735694c38c48a67ac";

    private GridViewAdapter mAdapter;
    private MovieDb mMovieDb;

    public MovieMainFragment() {
        // Required empty public constructor
    }

    /**
     * @return the subpath ame according to user setting
    */
    String sortby() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String popular_val = getString(R.string.sortby_popular);
        String sortBy = preferences.getString(getString(R.string.pref_sortby_key) ,popular_val );
        return (popular_val.equals(sortBy)) ? PopularPath : TopRatedPath;
    }
    private void updateMovieInfo() {
        String sortby = sortby();
        String url = UrlBase + sortby();
        Uri buildUri = Uri.parse(url).buildUpon()
            .appendQueryParameter(getString(R.string.tag_api_key), myApiKey).build();
        new FetchMovieTask(this).execute(buildUri.toString());
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private int calcNumColumn() {
        DisplayMetrics metrics = new  DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int columnwidth_px = 185 * metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        int colNum =  metrics.widthPixels / columnwidth_px;
        Log.v(LOG_TAG, String.format("calc #col: columnwidth_px=%d, colNum=%d", columnwidth_px,colNum));
        return colNum;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_main, container, false);
        Context context = getContext();
        mAdapter = new GridViewAdapter( context, R.layout.grid_item );
        GridView gridView = (GridView) rootView.findViewById(R.id.movie_grid);
        gridView.setAdapter(mAdapter);
        int gridNumCol = calcNumColumn();
        gridView.setNumColumns(gridNumCol);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieItem item = mAdapter.getItem(position);
                Intent detailIntenet = new Intent(getContext(), DetailActivity.class);
                detailIntenet.putExtra("MovieItem", item.mId);
                startActivity(detailIntenet);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieInfo();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_main_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_setting) {
            updateMovieInfo();
            return true;               // stop here
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    public void updateAdapter(MovieDb movieDb) {
        mMovieDb = movieDb;
        mAdapter.resetList(movieDb.mItemList);
    }
}
