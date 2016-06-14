package com.coderming.movieapp;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

    private static final String[] MAIN_MOVIE_COLUMNS = {
            BaseColumns._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH };
    public static final int COL_ID = 0;
    public static final int COL_POSTER_PATH = 1;

    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private float mDesityRatio;
    private GridLayoutManager mGridLayoutManager;
    private int mLoaderId = -1;
    private long mFirstMovieDbId = -1;
    private Uri mUri;
    private boolean mIsRefreshed;


    public MovieMainFragment() {  }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //not set in AndroidManifest.xml, register here,
    }

    private void init() {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        double screenWidthInch = displayMetrics.widthPixels / displayMetrics.xdpi;
        double screenHeightInch = displayMetrics.heightPixels / displayMetrics.ydpi;
        double diagonalInches = Math.sqrt(screenWidthInch * screenWidthInch + screenHeightInch * screenHeightInch);
        if (diagonalInches >= 6.5)
            mDesityRatio = displayMetrics.xdpi / DisplayMetrics.DENSITY_XXHIGH ;
        else
            mDesityRatio = displayMetrics.xdpi / DisplayMetrics.DENSITY_XHIGH ;
    }
    private int calcGridColumnNumber( int parenWidthPx ) {
        Resources res = this.getResources();
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float width = res.getDimension(R.dimen.moviedb_image_width_185);
        if (mDesityRatio > 1f) {           // if 4K kind
            width = width * mDesityRatio ;
        }
        int space = Math.round(res.getDimensionPixelSize(R.dimen.dimen_1dp) / 2);
        int maxWidth = Math.round(width) + (4 * space);
        int numCol = parenWidthPx / maxWidth;
        int extra =  parenWidthPx % maxWidth;
        if ((extra >= width/2) && (mDesityRatio<=1))
            numCol++;
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
            outRect.top = mSpace;
        }
    }
    private void setLayoutMgmgt(int col, int space) {
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 //       init();
        final View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        String key = getLoaderKey();
        if ((savedInstanceState != null) && savedInstanceState.containsKey(key)) {
            mLoaderId = savedInstanceState.getInt(key);
        } else {
            mLoaderId = -1;
        }
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        int colnum = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)?3:2;
        mGridLayoutManager = new GridLayoutManager(getContext(), colnum);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        float space = getResources().getDimension(R.dimen.dimen_1dp)/2;
        RecyclerView.ItemDecoration itemDecoration = new SpacesItemDecoration(2);
        mRecyclerView.addItemDecoration(itemDecoration);

        Bundle args = getArguments();
        mUri = args.getParcelable(MainActivity.PAGE_DATA_URI);

        mAdapter = new MovieRecyclerViewAdapter( this );
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }
    String getLoaderKey() {
        if (mUri != null) {
            return "LOADER_ID_" + mUri;
        }  else {
            Log.w(LOG_TAG, "getLoaderKey null mUri") ;
            return null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLoaderId != -1)
            outState.putLong(getLoaderKey(), mLoaderId);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed())
        {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }
        if (mFirstMovieDbId != -1) {
            mAdapter.notifyItemSelected(mFirstMovieDbId);
            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.v(LOG_TAG, "$*$*$*$* onGlobalLayout called");
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mAdapter.notifyDataSetChanged();
                }
            });
        } else {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (mAdapter != null) {
                        if (mAdapter.readyForLayout()) {
                            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    Log.v(LOG_TAG, "$*$*$*$* onGlobalLayout called");
                                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }, 5000);           // in milli
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (!args.containsKey(MainActivity.PAGE_DATA_URI))
            args.putParcelable(MainActivity.PAGE_DATA_URI, MovieContract.MovieEntry.CONTENT_POPULAR_URI);
        if (mLoaderId == -1) {
            mLoaderId = Constants.nextId();
        }
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
                mFirstMovieDbId = data.getLong(COL_ID);
            } else if (!isFav) {
                try {
                    Thread.sleep(100);              // sleep 100 ms
                } catch (InterruptedException iex) {
                    Log.w(LOG_TAG, "+++++Thread.sleep: cannot sleep!!!");
                }
                Bundle args = new Bundle();
                args.putParcelable(MainActivity.PAGE_DATA_URI, mUri);
                getLoaderManager().restartLoader(loader.getId(), args, this);
            }
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mLoaderId == loader.getId())
            mAdapter.resetCursor();
    }
}
