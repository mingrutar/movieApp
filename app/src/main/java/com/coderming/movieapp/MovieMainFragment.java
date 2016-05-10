package com.coderming.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
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

import com.coderming.movieapp.model.MovieDb;
import com.coderming.movieapp.model.MovieItem;

//TODO: add menu forsort
/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment {
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    static final String UrlBase = "https://api.themoviedb.org/3/movie/";
    //TODO: replace the api_key
    static final String myApiKey = "replace-this-api_key";

    private GridViewAdapter mAdapter;
    private MovieDb mMovieDb;

    public MovieMainFragment() {
    }

    private void updateMovieInfo(String sortby) {
        String url = UrlBase + sortby;
        Uri buildUri = Uri.parse(url).buildUpon()
                .appendQueryParameter(getString(R.string.tag_api_key), myApiKey).build();
        new FetchMovieTask(this).execute(buildUri.toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    // TODO how to deal with 4K screen?
    private void calcNumColumes( GridView gridView) {
        Resources res = getActivity().getResources();
        Configuration configuration = res.getConfiguration();
        int smallScreenWidthDp = configuration.smallestScreenWidthDp;

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float colWidth = res.getDimension(R.dimen.moviedb_image_width_185) + res.getDimensionPixelSize(R.dimen.grid_hspacing) ;
        int posterWidthDp = Math.round( colWidth / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        int numCol = (int) (smallScreenWidthDp  / (posterWidthDp));
        int numCol =  Math.round(smallScreenWidthDp/colWidth);
        gridView.setNumColumns(numCol);
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
    public void onStart() {
        super.onStart();
        updateMovieInfo(getString(R.string.tag_sortby_popular) );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_main_fragment, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        android.support.v7.app.ActionBar actionBar=((AppCompatActivity)getActivity() ).getSupportActionBar();
        final Context themedContext = actionBar.getThemedContext();
        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(
                themedContext, R.array.movieListOrderValue, android.R.layout.simple_spinner_dropdown_item); //  create the adapter from a StringArray
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(spinnerAdapter); // set the adapter to provide layout of rows and content
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                if (item != null) {
                    if ( item.toString().equals( getString(R.string.sortby_popular))) {
                        updateMovieInfo(getString(R.string.tag_sortby_popular));
                    } else {
                        updateMovieInfo(getString(R.string.tag_sortby_top_rated));
                    }
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
        return super.onOptionsItemSelected(item);
//        if (item.getItemId() == R.id.action_setting) {
//            Intent settingIntent  = new Intent(getActivity(), SettingsActivity.class);
//            startActivity(settingIntent);
//            return true;               // stop here
//        } else {
//            return super.onOptionsItemSelected(item);
//        }
    }
    public void updateAdapter(MovieDb movieDb) {
        mMovieDb = movieDb;
        mAdapter.resetList(movieDb.getItemList());
    }
}
