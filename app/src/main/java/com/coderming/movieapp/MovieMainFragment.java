package com.coderming.movieapp;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.utils.Constants;
import com.coderming.movieapp.utils.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();
    private static final String LAST_SEL_ITEM = "LatsSelectedItem";

    private static final String[] MAIN_MOVIE_COLUMNS = {
            BaseColumns._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH };
    public static final int COL_ID = 0;
    public static final int COL_POSTER_PATH = 1;
    static int sHColNumber = -1;
    static int sVColNumber = -1;

    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private int mLoaderId = -1;
    private Uri mUri;

    public MovieMainFragment() {  }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    private boolean isTablet() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        double screenWidthInch = displayMetrics.widthPixels / displayMetrics.xdpi;
        double screenHeightInch = displayMetrics.heightPixels / displayMetrics.ydpi;
        double diagonalInches = Math.sqrt(screenWidthInch * screenWidthInch + screenHeightInch * screenHeightInch);
        return (diagonalInches >= 6.5);            // for tablet 3 or 2
    }
    private int calcColumnNumber (int parentSize, boolean isLongEdge) {
        Resources res = getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        float desityRatio = (isLongEdge?displayMetrics.ydpi:displayMetrics.xdpi) / DisplayMetrics.DENSITY_HIGH;
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float width = res.getDimension(R.dimen.moviedb_image_width_185);
        if (desityRatio > 1f) {           // if 4K kind
            width = width * desityRatio;
        }
        float space =res.getDimensionPixelSize(R.dimen.dimen_1dp);
        int numCol = parentSize /  Math.round(width+space);
//        int extra = parentSize % Math.round(width+space);
//        if ((extra >= width / 2) && (desityRatio <= 1))
//            numCol++;
        return numCol;
   }

    private int calcGridColumnNumber( int parentWidthPx, int parentHeightPx ) {
        boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (sHColNumber != -1)
            return isLandscape ? sHColNumber : sVColNumber;
        if (isTablet()) {           //  tablet 3 : 2;
            sHColNumber = 3;
            sVColNumber = 2;
        } else {
            int longEdge = Math.max(parentWidthPx, parentHeightPx);
            int shortEdge = Math.min(parentWidthPx, parentHeightPx);
            sHColNumber = calcColumnNumber(longEdge, true);
            sVColNumber = calcColumnNumber(shortEdge, false);
        }
        return isLandscape ? sHColNumber : sVColNumber;
    }
    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;
        public SpacesItemDecoration(float space) {
            this.mSpace = Math.round(space);
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(mSpace, mSpace, mSpace, mSpace);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        Bundle args = getArguments();
        if (!args.containsKey(MainActivity.PAGE_DATA_URI))
            args.putParcelable(MainActivity.PAGE_DATA_URI, MovieContract.MovieEntry.CONTENT_POPULAR_URI);
        mUri = args.getParcelable(MainActivity.PAGE_DATA_URI);

        String key = getLoaderKey();
        if ((savedInstanceState != null) && savedInstanceState.containsKey(key)) {
            mLoaderId = savedInstanceState.getInt(key);
        } else {
            mLoaderId = Constants.nextId();;
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        int colnum = 2;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            colnum = (sHColNumber == -1) ? 3 : sHColNumber;
        } else {
            colnum =  (sVColNumber == -1) ? 2 : sVColNumber;
        }
//        int colnum = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)?3:2;
        mGridLayoutManager = new GridLayoutManager(getContext(), colnum);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(getResources().getDimension(R.dimen.dimen_1dp)));
        if ((sHColNumber == -1) || (sVColNumber == -1)) {
            ViewTreeObserver vto = mRecyclerView.getViewTreeObserver();
            if ((vto != null) && vto.isAlive()) {
                vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int colNum = calcGridColumnNumber(mRecyclerView.getWidth(), mRecyclerView.getHeight());
                        mGridLayoutManager.setSpanCount(colNum);
                        return true;
                    }
                });
            }
        }
        mAdapter = new MovieRecyclerViewAdapter( this );
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }
    String getLoaderKey() {
        return (mUri != null)? "LOADER_ID_" + mUri.toString() : null;
    }
    String getSelectKey() {
        return (mUri != null)? "LAST_SEL_" + mUri.toString() : null;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLoaderId != -1)
            outState.putInt(getLoaderKey(), mLoaderId);
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed())
        {
            onResume();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Long sel = mAdapter.getSelMovieDbId();
        Log.v(LOG_TAG, String.format("+++BV+=> onPause: uri=%s, sel=%s", mUri, (sel==null)?"null":Long.toString(sel)));
        if ((sel != null) && (sel.longValue() != -1)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(getSelectKey(), sel);
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String key = getSelectKey();
        Long sel = prefs.getLong(key, -1l);
        Log.v(LOG_TAG, String.format("+++BV++<= onResume: uri=%s, sel=%s", mUri, (sel==null)?"null":Long.toString(sel)));
        mAdapter.setSelMovieDbId(sel);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (!args.containsKey(MainActivity.PAGE_DATA_URI))
            args.putParcelable(MainActivity.PAGE_DATA_URI, MovieContract.MovieEntry.CONTENT_POPULAR_URI);
        getLoaderManager().initLoader(mLoaderId, args, this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> ret = null;
        if ((mLoaderId == id) && args.containsKey(MainActivity.PAGE_DATA_URI)) {
            mUri = args.getParcelable(MainActivity.PAGE_DATA_URI);
            ret = new CursorLoader(getContext(), mUri, MAIN_MOVIE_COLUMNS, null, null, MovieContract.MovieEntry._ID + " asc");
        } else {
            Log.w(LOG_TAG, "onCreateLoader need to contain URI in bundle, mLoadId="+ Integer.toString(mLoaderId));
        }
        return ret;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "+++RA+++ onLoadFinished, cursor count=" + ((data==null)?"null" : Integer.toString(data.getCount())));
        if (mLoaderId == loader.getId()) {
            boolean isFav = Utilities.isFavoritePage(mUri);
            if (data.moveToFirst()) {
                if (isFav) {
                    do {
                        Utilities.addFavoriteMovie(data.getLong(COL_ID));
                    } while (data.moveToNext());
                    data.moveToFirst();
                }
                mAdapter.swapCursor(data);
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mLoaderId == loader.getId())
            mAdapter.resetCursor();
    }
}
