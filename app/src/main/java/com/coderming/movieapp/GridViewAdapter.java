package com.coderming.movieapp;

import android.app.Activity;
import android.content.Context;
<<<<<<< HEAD
import android.support.v7.widget.RecyclerView;
import android.util.Log;
=======
>>>>>>> spinner
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
<<<<<<< HEAD
=======
import android.widget.LinearLayout;
>>>>>>> spinner

import com.coderming.movieapp.model.MovieItem;
import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

/**
 * Created by linna on 5/4/2016.
 */
public class GridViewAdapter extends ArrayAdapter<MovieItem> {
    private static final String LOG_TAG = GridViewAdapter.class.getSimpleName();

    public static final String FORMATTER_PICASSO_IMAGE_LOADER = "http://image.tmdb.org/t/p/w%s/%s";

    LinearLayout.LayoutParams mParms;

    public GridViewAdapter(Context context, int layoutResourceId ) {
        super(context, layoutResourceId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        Context context = getContext();
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.grid_item, parent, false);
        } else {
            rowView = (View) convertView;
        }
        ImageView imageView = (ImageView) rowView.findViewById(R.id.movie_poster);
        MovieItem item = this.getItem(position);
        String url = String.format(FORMATTER_PICASSO_IMAGE_LOADER
                , String.valueOf(context.getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_185)), item.getPosterPath());
        Picasso.with(getContext()).load(url).into(imageView);
        return rowView;
    }
    public void resetList(List<MovieItem> list) {
        this.clear();
        this.addAll(list);
    }
    @Override
    public void addAll(Collection<? extends MovieItem> collection) {
        super.addAll(collection);
    }
}
