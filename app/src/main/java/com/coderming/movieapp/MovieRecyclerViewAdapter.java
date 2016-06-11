package com.coderming.movieapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linna on 6/10/2016.
 */
public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.RecyclerViewHolders>
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieRecyclerViewAdapter.class.getSimpleName();

    public int mLoadId = 0;         // use page number

    private Cursor mCursor;
    private Context mContext;

    private List<OnLoadFinishListener> mLoaderSubscriber;
    public interface OnLoadFinishListener {
        void onLoadFinish(int page, int size);
    }

    public MovieRecyclerViewAdapter(Context context, int pageN) {
        mLoadId = pageN;
        mContext = context;
        mLoaderSubscriber = new ArrayList<>();
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new RecyclerViewHolders(view);
    }
    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
//        Log.v(LOG_TAG, String.format("++++ onBindViewHolder, cursor?=%s, position=%d", (mCursor!=null) ,position ));
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            int idx = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);

            String url = String.format(Constants.FORMATTER_PICASSO_IMAGE_LOADER
                    , String.valueOf(mContext.getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_185)),
                    mCursor.getString(idx));
            Picasso.with(mContext).load(url).into(holder.mImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DetailActivity.class);
                    Uri uri = MovieContract.MovieEntry.buildUri(mCursor.getLong(0));
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
            ret = new CursorLoader(mContext, uri, null, null, null, MovieContract.MovieEntry._ID + " asc");
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
