package com.coderming.movieapp;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
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
public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.RecyclerViewHolders>  {
    private static final String LOG_TAG = MovieRecyclerViewAdapter.class.getSimpleName();

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

    public void notifyItemSelected(Long movieDbId) {
        Uri uri = MovieContract.MovieEntry.buildUri( movieDbId.longValue() );
        for (ItemClickedCallback callback : mItemClickedCallbacks ) {
            callback.onItemClicked(uri);
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
            Long mid = mCursor.getLong(MovieMainFragment.COL_ID);
            holder.mView.setTag(mid);
            final String url = String.format(Constants.FORMATTER_PICASSO_IMAGE_LOADER
                    , String.valueOf(mContext.getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_185)),
                    mCursor.getString(MovieMainFragment.COL_POSTER_PATH));
            if (position == 0) {
                Log.v(LOG_TAG, String.format("+++RA+++ onBindViewHolder, position=%d, url=%s", position, url));
            }
            Picasso.with(mContext).load(url)
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mImageView);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemSelected((Long) v.getTag());
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public Cursor swapCursor(Cursor cursor) {
        Cursor ret = null;
        if (mCursor == null) {
            mCursor = cursor;
        } else if (!mCursor.equals(cursor)){
            mCursor.close();
            ret = mCursor;
            mCursor = cursor;
        }
        notifyDataSetChanged();
        return ret;
    }
    public void resetCursor() {
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
