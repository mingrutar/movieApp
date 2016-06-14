package com.coderming.movieapp;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.utils.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment   {
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();

    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private float mDesityRatio;
    private GridLayoutManager mGridLayoutManager;
    private int mLoaderId = -1;
    private Uri mUri;
    private boolean mIsVisibleToUser;
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

//        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
//                int newSpanCOunt = calcGridColumnNumber(mRecyclerView.getWidth());
//                mGridLayoutManager.setSpanCount(newSpanCOunt);
//                mGridLayoutManager.requestLayout();
//                return true;
//            }
//        });
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
        mAdapter.setLoaderId(mLoaderId);
        getLoaderManager().initLoader(mLoaderId, args, mAdapter);
    }

    /**
     * Set a hint to the system about whether this fragment's UI is currently visible
     * to the user. This hint defaults to true and is persistent across fragment instance
     * state save and restore.
     * <p>
     * <p>An app may set this to false to indicate that the fragment's UI is
     * scrolled out of visibility or is otherwise not directly visible to the user.
     * This may be used by the system to prioritize operations such as fragment lifecycle updates
     * or loader ordering behavior.</p>
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
}
