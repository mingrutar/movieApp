package com.coderming.movieapp;

import android.content.Context;
import android.content.Intent;
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

    public int mLoadId;         // use page number

    private Cursor mCursor;
    private Context mContext;

    private List<OnLoadFinishListener> mLoaderSubscriber;
    public interface OnLoadFinishListener {
        void onLoadFinish(int page, int size);
    }

    public MovieRecyclerViewAdapter(Fragment fragment) {
        mLoadId = Constants.nextId();
        mContext = fragment.getContext();
        mLoaderSubscriber = new ArrayList<>();
        if (fragment instanceof OnLoadFinishListener) {
            mLoaderSubscriber.add((OnLoadFinishListener)fragment);
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
            holder.mView.setTag(mCursor.getLong(COL_ID));
            final String url = String.format(Constants.FORMATTER_PICASSO_IMAGE_LOADER
                    , String.valueOf(mContext.getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_185)),
                    mCursor.getString(COL_POSTER_PATH));
//            Log.v(LOG_TAG, String.format("++++ onBindViewHolder, position=%d, url=%s", position,url ));
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
            Picasso.with(mContext).load(url).into(holder.mImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DetailActivity.class);
                    Long movieDbId = (Long) v.getTag();
                    Uri uri = MovieContract.MovieEntry.buildUri( movieDbId.longValue() );
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
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
        if (mLoadId == id) {
            Uri uri = args.getParcelable(MainActivity.PAGE_DATA_URI);
            ret = new CursorLoader(mContext, uri, MAIN_MOVIE_COLUMNS, null, null, MovieContract.MovieEntry._ID + " asc");
        } else {
            Log.w(LOG_TAG, "LOADER_MOVIE_ID need to contain URI in bundle");
        }
        return ret;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        Log.v(LOG_TAG, "++++ onLoadFinished, cursor count=" + ((data==null)?"null" : Integer.toString(data.getCount())));
        int size = 0;
        if (data != null) {
            mCursor = data;
            size = mCursor.getCount();
            notifyDataSetChanged();
        }
        for (OnLoadFinishListener loaderSubscriber : mLoaderSubscriber) {
            loaderSubscriber.onLoadFinish(mLoadId, size);
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
        mCursor = null;
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
