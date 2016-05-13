package com.coderming.movieapp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.coderming.movieapp.model.MovieItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {
    static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public DetailFragment() {
        // Required empty public constructor
    }

    private void fillPage(View parent, MovieItem item) {
        Resources resources = getContext().getResources();
        final ImageView imageView = (ImageView) parent.findViewById(R.id.imageView_backdrop);
        String url = String.format(GridViewAdapter.FORMATTER_PICASSO_IMAGE_LOADER
                , String.valueOf(resources.getDimensionPixelSize(R.dimen.moviedb_image_width_342))
                , item.getBackdropPath());
        Picasso.with(getContext()).load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.w(LOG_TAG, "Fail to load backdrop image");
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });

        ((TextView) parent.findViewById(R.id.title_textView)).setText(item.getOriginalTitle());
        ((TextView) parent.findViewById(R.id.overview_textView)).setText(item.getOverview());
        ((TextView) parent.findViewById(R.id.release_textView)).setText( item.getReleaseDate());
        // the full rate is 10 represented by 5 stars, so /2
        ((TextView) parent.findViewById(R.id.nVoters_textView)).setText(String.format("%.01f", item.getVoteAverage()/2.0f));
        float rating = (float) (item.getVoteAverage() * 5.0f) /10.0f;
        ((RatingBar) parent.findViewById(R.id.ratingBar)).setRating(rating);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            Object obj = intent.getSerializableExtra("MovieItem");
            if  (obj instanceof MovieItem) {
                MovieItem movieItem = (MovieItem) intent.getSerializableExtra("MovieItem");
                fillPage(root, movieItem);
            } else {
                Log.w(LOG_TAG, "Received an invalid intent, ignored ");
            }
        } else {
            Log.w(LOG_TAG, "An invalid is expeted");
        }
        return root;
    }

}
