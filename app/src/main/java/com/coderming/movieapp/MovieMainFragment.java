package com.coderming.movieapp;

import android.content.res.Resources;
import android.graphics.Rect;
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
import android.view.ViewTreeObserver;

import com.coderming.movieapp.data.MovieContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieMainFragment extends Fragment  {
    private static final String LOG_TAG = MovieMainFragment.class.getSimpleName();

    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private float mDesityRatio;

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
    private void calcGridColumnNumber( int parenWidthPx ) {
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
        else if ((extra / numCol ) > 4)
            space *= 2;
        Log.v(" +=+=+=+= calcColnumber", String.format("#col=%d, space=%d, parentWidth=%d", numCol, space, parenWidthPx));
        setLayoutMgmgt(numCol, space);
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
        GridLayoutManager mglm = new GridLayoutManager(getContext(), col);
        mRecyclerView.setLayoutManager(mglm);
        RecyclerView.ItemDecoration itemDecoration = new SpacesItemDecoration(space);
        mRecyclerView.addItemDecoration(itemDecoration);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        init();
        final View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                rootView.getViewTreeObserver().removeOnPreDrawListener(this);
                calcGridColumnNumber(mRecyclerView.getWidth());
                return true;
            }
        });

        Bundle args = getArguments();
        if (!args.containsKey(MainActivity.PAGE_DATA_URI))
            args.putParcelable(MainActivity.PAGE_DATA_URI, MovieContract.MovieEntry.CONTENT_POPULAR_URI);
        mAdapter = new MovieRecyclerViewAdapter( this );
        mRecyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(mAdapter.mLoadId, args, mAdapter);

        return rootView;
    }
}
