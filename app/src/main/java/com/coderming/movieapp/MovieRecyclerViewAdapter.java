package com.coderming.movieapp;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.utils.Constants;
import com.coderming.movieapp.utils.Utilities;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linna on 6/10/2016.
 */
public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.RecyclerViewHolders>
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieRecyclerViewAdapter.class.getSimpleName();

    private static final String[] MAIN_MOVIE_COLUMNS = {
            BaseColumns._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH };
    private static final int COL_ID = 0;
    private static final int COL_POSTER_PATH = 1;

    public int mLoadId = -1;         // use page number

    private Uri mUri;
    private Cursor mCursor;
    private Context mContext;
    private Fragment mFragment;

//    private List<OnLoadFinishListener> mLoaderSubscriber;    TODO: not used for now
    private List<ItemClickedCallback> mItemClickedCallbacks;

    public interface ItemClickedCallback {
        void onItemClicked(Uri uri);
    }

//    public interface OnLoadFinishListener {
//        void onLoadFinish(Uri pageUri, int size);
//    }

    public MovieRecyclerViewAdapter(Fragment fragment) {
        mFragment = fragment;
        mContext = fragment.getContext();
//        mLoaderSubscriber = new ArrayList<>();
//        if (fragment instanceof OnLoadFinishListener) {
//            mLoaderSubscriber.add((OnLoadFinishListener)fragment);
//        }

        mItemClickedCallbacks = new ArrayList<>();
        Activity activity = fragment.getActivity();
        if (activity instanceof ItemClickedCallback) {
            mItemClickedCallbacks.add((ItemClickedCallback)activity);
        }
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new RecyclerViewHolders(view);
    }
    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            Long mid = mCursor.getLong(COL_ID);
            holder.mView.setTag(mid);
            final String url = String.format(Constants.FORMATTER_PICASSO_IMAGE_LOADER
                    , String.valueOf(mContext.getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_185)),
                    mCursor.getString(COL_POSTER_PATH));
            if (position == 0) {
                if (mFragment.isVisible()) {
                    Uri uri = MovieContract.MovieEntry.buildUri( mid );
                    for (ItemClickedCallback callback : mItemClickedCallbacks ) {
                        callback.onItemClicked(uri);
                    }
                }
                Log.v(LOG_TAG, String.format("+++RA+++ onBindViewHolder, position=%d, url=%s", position, url));
            }
            Picasso.with(mContext).load(url).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    holder.mImageView.setImageBitmap(bitmap);
                }
                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.w(LOG_TAG, "Fail to load poster image at "+url);
                }
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = MovieContract.MovieEntry.buildUri( (long) v.getTag() );
                    for (ItemClickedCallback callback : mItemClickedCallbacks ) {
                        callback.onItemClicked(uri);
                    }
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public void setLoaderId(int loaderId) {
        mLoadId = loaderId;
    }
    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> ret = null;

        if ((mLoadId == id) && args.containsKey(MainActivity.PAGE_DATA_URI)) {
            mUri = args.getParcelable(MainActivity.PAGE_DATA_URI);
            ret = new CursorLoader(mContext, mUri, MAIN_MOVIE_COLUMNS, null, null, MovieContract.MovieEntry._ID + " asc");
        } else {
            Log.w(LOG_TAG, "onCreateLoader need to contain URI in bundle, mLoadId="+ Integer.toString(mLoadId));
        }
        return ret;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "+++RA+++ onLoadFinished, cursor count=" + ((data==null)?"null" : Integer.toString(data.getCount())));
        int size = 0;
        if (data != null) {
            if (mCursor != null) {
                mCursor.close();
            }
            mCursor = data;
            size = mCursor.getCount();

            if ( Utilities.isFavoritePage(mUri)) {
                if (data.moveToFirst()) {
                    do {
                        Utilities.addFavoriteMovie(data.getLong(COL_ID));
                    } while (data.moveToNext());
                    data.moveToFirst();
                    notifyDataSetChanged();
                }
            } else {
                if (size > 0) {
                    notifyDataSetChanged();
                } else {
                    try {
                        Thread.sleep(100);              // sleep 100 ms
                    } catch (InterruptedException iex) {
                        Log.w(LOG_TAG, "+++++Thread.sleep: cannot sleep!!!");
                    }
                    Bundle args = new Bundle();
                    args.putParcelable(MainActivity.PAGE_DATA_URI, mUri);
                    mFragment.getLoaderManager().restartLoader(loader.getId(), args, this);
                }
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
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    /**
     * class RecyclerViewHolders
     */
    public static class RecyclerViewHolders extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        ImageView mImageView;
        View mView;

        public RecyclerViewHolders(View itemView) {
            super(itemView);
            mView = itemView;
            mImageView = (ImageView) itemView.findViewById(R.id.movie_poster);
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
        }
    }
}
