package com.coderming.movieapp;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.model.MovieSource;
import com.coderming.movieapp.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment {
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    static final String UrlBase = "https://api.themoviedb.org/3/movie/";
    private MovieRecyclerViewAdapter mAdapter;
    private MovieSource mMovieDb;

    public MovieMainFragment() {  }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //not set in AndroidManifest.xml, register here,
    }

    // TODO how to deal with 4K screen?
//    private void calcNumColumes( GridView gridView) {
    private int calcNumColumes( ) {
        Resources res = getActivity().getResources();
        Configuration configuration = res.getConfiguration();
        int smallScreenWidthDp = configuration.smallestScreenWidthDp;

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float colWidth = res.getDimension(R.dimen.moviedb_image_width_185) + res.getDimensionPixelSize(R.dimen.dimen_4dp) ;
        int posterWidthDp = Math.round( colWidth / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
// TODO: use commented numCol all dp will get tiny imagess
//        int numCol = (int) (smallScreenWidthDp  / (posterWidthDp));
        int numCol =  Math.round(smallScreenWidthDp/colWidth);
        return numCol;
    }
    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;
        public SpacesItemDecoration(float space) {
            this.mSpace = Math.round(space);
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = mSpace;
            outRect.right = mSpace;
            outRect.bottom = mSpace;
            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 0)
                outRect.top = mSpace;
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), calcNumColumes()));
        RecyclerView.ItemDecoration itemDecoration = new SpacesItemDecoration(getResources().getDimension(R.dimen.dimen_4dp));
        recyclerView.addItemDecoration(itemDecoration);

        Bundle args = getArguments();
        if (!args.containsKey(MainActivity.PAGE_DATA_URI))
            args.putParcelable(MainActivity.PAGE_DATA_URI, MovieContract.MovieEntry.CONTENT_POPULAR_URI);
        mAdapter = new MovieRecyclerViewAdapter(getContext(), Constants.nextId());
        recyclerView.setAdapter(mAdapter);
        TextView textView = (TextView) rootView.findViewById(R.id.page_name);
        String pname = args.getParcelable(MainActivity.PAGE_DATA_URI).toString();
        textView.setText(pname);
        getLoaderManager().initLoader(mAdapter.mLoadId, args, mAdapter);
        return rootView;
    }

//    public void onResume() {
//        Log.v(LOG_TAG, "++++onResume");
//        super.onResume();
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//        mSortby = prefs.getString(getString(R.string.pref_sortby_key), getString(R.string.sortby_popular));
//        if (mSpinner != null) {
//            int pos = 0;
//            if (mSortby.equals(getString(R.string.sortby_top_rated))) {
//                pos = 1;
//            } else if (mSortby.equals(getString((R.string.sortby_faverites)))) {
//                pos = 2;
//            }
//            mSpinner.setSelection(pos);
//        } else {                      // resume could be called before spinner is created
//            updateMovieInfo();
//        }
//    }

    @Override
    public void onPause() {
//        Log.v(LOG_TAG, "++++onPause");
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(getString(R.string.pref_sortby_key), mSortby);
//        editor.commit();
        super.onPause();
    }

//    public void updateAdapter(MovieSource movieSource) {
//        mMovieDb = movieSource;
//        getLoaderManager().initLoader(mAdapter.LOADER_ID, args, mAdapter);
//    }
}
