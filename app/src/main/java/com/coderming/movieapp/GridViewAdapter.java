package com.coderming.movieapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by linna on 5/4/2016.
 */
public class GridViewAdapter extends ArrayAdapter<MovieItem> {
    private static final String LOG_TAG = GridViewAdapter.class.getSimpleName();
    //"w92", "w154", "w185", "w342", "w500", "w780", or "original"
    public static final int POSTER_WIDTH = 185;
    private static final String URLFormatter = "http://image.tmdb.org/t/p/w%d/%s";
    Lock mLocl;
    LinearLayout.LayoutParams mParms;

    public GridViewAdapter(Context context, int layoutResourceId ) {
        super(context, layoutResourceId);
        mLocl = new ReentrantLock();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.v(LOG_TAG, String.format("getView position=%d, convertView=%s", position, convertView));
        View rowView;
        if (convertView == null) {
            Context context = getContext();
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.grid_item, parent, false);
        } else {
            rowView = (View) convertView;
        }
        ImageView imageView = (ImageView) rowView.findViewById(R.id.movie_poster);
        MovieItem item = this.getItem(position);
        String url = String.format(URLFormatter, POSTER_WIDTH, item.mPosterPath);
        GridView gridView = (GridView) parent.findViewById(R.id.movie_grid);
        int ivw = imageView.getMeasuredHeight();
        imageView.getLayoutParams().width = gridView.getWidth();
        Picasso.with(getContext()).load(url).into(imageView);
//        try {
//            if (mLocl.tryLock(30L, TimeUnit.SECONDS)) {
//                imageView.setLayoutParams(mParms);
//                Picasso.with(getContext()).load(url).fit().centerInside().into(imageView);
//            }
//        } catch (InterruptedException iex) {
//            Log.d(LOG_TAG, String.format("getView waiting for lock is interrupted while process item %d", position));
//        }
        return rowView;
    }
    public void resetList(List<MovieItem> list) {
        this.clear();
        this.addAll(list);
    }
    public void getImageHeigh( MovieItem item, int gridWidth) {
        final boolean myLock = mLocl.tryLock();
        String url = String.format(URLFormatter, POSTER_WIDTH, item.mPosterPath);
        final int imageWidth = gridWidth;
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                int bmw = bitmap.getWidth();
                int bmh = bitmap.getHeight();
                int ivh = imageWidth * bmh / bmw;
                mParms = new LinearLayout.LayoutParams(imageWidth, ivh);
                if (myLock) {
                    mLocl.unlock();
                }
//                    Log.v(LOG_TAG, String.format("+++ image w=%d,h=%d, ivw=%d,ivh=%d", bmw, bmh, gridWidth, ivh));
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (myLock) {
                    mLocl.unlock();
                }
                Log.w(LOG_TAG, "load move poster error");
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.with(getContext()).load(url).into(target);
    }
    @Override
    public void addAll(Collection<? extends MovieItem> collection) {
        super.addAll(collection);
    }
}
