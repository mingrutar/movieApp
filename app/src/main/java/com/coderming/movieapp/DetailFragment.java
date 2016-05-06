package com.coderming.movieapp;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {
    public DetailFragment() {
        // Required empty public constructor
    }

    private void fillPage(View parent, MovieItem item) {
        Resources resources = getContext().getResources();
        ImageView imageView = (ImageView) parent.findViewById(R.id.imageView_backdrop);
        String url = String.format(GridViewAdapter.FORMATTER_PICASSO_IMAGE_LOADER
                , String.valueOf(resources.getDimension(R.dimen.moviedb_image_width_500))
                , item.mBackdropPath);
        Picasso.with(getContext()).load(url).into(imageView);

        ((TextView) parent.findViewById(R.id.title_textView)).setText(item.mTitle);
        ((TextView) parent.findViewById(R.id.overview_textView)).setText(item.mOverview);
        ((TextView) parent.findViewById(R.id.release_textView)).setText( item.mReleaseDate);
        ((TextView) parent.findViewById(R.id.nVoters_textView)).setText(String.valueOf(item.mVoteAverage));
        //TODO: mVoteAverage full range and calculation
        float rating = (float) item.mVoteAverage ;
        ((RatingBar) parent.findViewById(R.id.ratingBar)).setRating(rating);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            MovieItem movieItem = (MovieItem) intent.getSerializableExtra("MovieItem");

            fillPage(root, movieItem);
        } else {
            Toast.makeText(getContext(), "Invalid intent ", Toast.LENGTH_LONG).show();
        }
        return root;
    }

}
