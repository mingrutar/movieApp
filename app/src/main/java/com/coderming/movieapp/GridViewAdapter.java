package com.coderming.movieapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;


/**
 * Created by linna on 5/4/2016.
 */
public class GridViewAdapter extends ArrayAdapter<MovieItem> {
    private static final String LOG_TAG = GridViewAdapter.class.getSimpleName();
    public static final int POSTER_WIDTH = 185;
    private int mLayoutResourceId;
    public GridViewAdapter(Context context, int layoutResourceId ) {
        super(context, layoutResourceId);
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v("LOG_TAG", String.format("getView position=%d, convertView=%s",position, convertView));
        View row = convertView;
        CustomerViewHolder holder = null;
        Context context = getContext();
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new CustomerViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (CustomerViewHolder) row.getTag();
        }

        MovieItem item = this.getItem(position);
//        holder.mImageView.setImageBitmap(item.mImage);
        String url = getImageUrl(item.mPosterPath, 185);
        Picasso.with(context).load(url).into(holder.mImageView);
        return row;
    }

    String getImageUrl(String imageId, int width) {
        return String.format("http://image.tmdb.org/t/p/w%d/%s", width, imageId);
    }
    public void resetList(List<MovieItem> list) {
        this.clear();
        this.addAll(list);
    }

    @Override
    public void addAll(Collection<? extends MovieItem> collection) {
        super.addAll(collection);
    }
    class CustomerViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public CustomerViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.movie_poster);
        }
    }

}
