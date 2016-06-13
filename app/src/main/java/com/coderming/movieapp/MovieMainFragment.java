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

import com.coderming.movieapp.data.MovieContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment   {
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();

    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private float mDesityRatio;
    private GridLayoutManager mGridLayoutManager;
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

        mAdapter = new MovieRecyclerViewAdapter( this );
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (!args.containsKey(MainActivity.PAGE_DATA_URI))
            args.putParcelable(MainActivity.PAGE_DATA_URI, MovieContract.MovieEntry.CONTENT_POPULAR_URI);
        getLoaderManager().initLoader(mAdapter.mLoadId, args, mAdapter);
    }
}
