package com.coderming.movieapp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.coderming.movieapp.data.MovieContract;
import com.coderming.movieapp.data.MovieContract.MovieEntry;
import com.coderming.movieapp.model.Details;
import com.coderming.movieapp.sync.MovieSyncAdapter;
import com.coderming.movieapp.utils.Constants;
import com.coderming.movieapp.utils.Utilities;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private int mMovieLoaderId;
    private int mDetailLoaderId;
    private int mMovieId;

    TextView mTitle;
    TextView mReleaseDate;
    TextView mNumVote;
    TextView mVoteAverage;
    RatingBar mRatingBar;
    ImageView mPoster;
    TextView mOverview;

    ListView mTailerListView;
    ListView mReviewListView;
//    ArrayAdapter<Details.Video> mTrailerAdapter;
//    ArrayAdapter<Details.Review> mReviewAdapter;
//    ListView mTrailers;
    ListView mReviews;
    ImageView mMyFavorite;

    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_OVERVIEW      };
    private static final int COL_TITLE = 0;
    private static final int COL_RELEASE_DATE = 1;
    private static final int COL_VOTE_COUNT = 2;
    private static final int COL_VOTE_AVERAGE = 3;
    private static final int COL_POSTER_PATH = 4;
    private static final int COL_OVERVIEW = 5;

    private static final String[] EXTRA_DETAIL_COLUMNS = {
            MovieContract.DetailEntry.COLUMN_TYPE,
            MovieContract.DetailEntry.COLUMN_DETAIL_DATA };
    private static final int COL_TYPE = 0;
    private static final int COL_DETAIL_DATA = 1;

    public DetailFragment() {
        // Required empty public constructor
        mMovieLoaderId = Constants.nextId();
        mDetailLoaderId = Constants.nextId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        getLoaderManager().initLoader(mMovieLoaderId, args, this);
        Uri uri = args.getParcelable(Constants.DETAIL_URI);
        mMovieId = Integer.parseInt(uri.getLastPathSegment());

        Uri uriDetail = MovieContract.DetailEntry.buildUri(mMovieId);
        Bundle dargs = getArguments();
        dargs.putParcelable(Constants.MORE_DETAIL_URI, uriDetail);
        getLoaderManager().initLoader(mDetailLoaderId, dargs, this);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitle = (TextView) root.findViewById(R.id.title_textView)  ;
        mReleaseDate = (TextView) root.findViewById(R.id.release_textView);
        mNumVote = (TextView) root.findViewById(R.id.nStar_textView) ;
        mVoteAverage = (TextView) root.findViewById(R.id.nVoters_textView);
        mRatingBar = (RatingBar) root.findViewById(R.id.ratingBar);
        mMyFavorite = (ImageView) root.findViewById(R.id.favority_imageView);
        mPoster = (ImageView) root.findViewById(R.id.poster_imageView);
        mOverview = (TextView) root.findViewById(R.id.overview_textView);

        mTailerListView = (ListView) root.findViewById(R.id.trailer_listView);
        mReviewListView = (ListView) root.findViewById(R.id.review_listView);
        setupExtraListViews ();


        FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.addFavoriteMovie(getContext(), mMovieId);
                mMyFavorite.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    private void fillPage(Cursor cursor) {
        mTitle.setText( cursor.getString(COL_TITLE));
        mReleaseDate.setText(Utilities.releaseDate2Str(cursor.getLong(COL_RELEASE_DATE)));
        double vote_average = cursor.getDouble(COL_VOTE_AVERAGE);
        float rating = (float) (vote_average * 5.0f) /10.0f;
        mRatingBar.setRating(rating);
        mVoteAverage.setText(String.format("%.01f", rating));
        mNumVote.setText(Integer.toString(cursor.getInt(COL_VOTE_COUNT)));
        mOverview.setText(cursor.getString(COL_OVERVIEW));
        mPoster.setImageResource(0);
        String imagePath = cursor.getString(COL_POSTER_PATH);
        final String url = String.format(Constants.FORMATTER_PICASSO_IMAGE_LOADER
                , String.valueOf(getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_342))
                , imagePath);
        Log.v(LOG_TAG, String.format("++++ fillPage, title=%s, url=%s", mTitle.getText(), url ));

        Picasso.with(getContext()).load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mPoster.setImageBitmap(bitmap);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.w(LOG_TAG, "Fail to load backdrop image at "+url);
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }
    private void fillExtraData(Cursor cursor) {
        do {
            try {
                String type = cursor.getString(COL_TYPE);
                if ("videos".equals(type)) {
                    List<Details.Video> videos =  Details.parseVideos(cursor.getString(COL_DETAIL_DATA));
                    if (videos.size() > 0) {
                        ((ArrayAdapter<Details.Video>) mTailerListView.getAdapter()).addAll(videos);
                        mTailerListView.setVisibility(View.VISIBLE);
                    } else {
                        mTailerListView.setVisibility(View.INVISIBLE);
                    }
                } else if ("reviews".equals(type)) {
                    List<Details.Review> reviews =  Details.parseReviews(cursor.getString(COL_DETAIL_DATA));
                    if (reviews.size() > 0) {
                        mTailerListView.setVisibility(View.VISIBLE);
                        ((ArrayAdapter<Details.Review>)mTailerListView.getAdapter()).addAll(reviews) ;
                    } else {
                        mTailerListView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    List<Details.Image> images = Details.parseImages(cursor.getString(COL_DETAIL_DATA));
                    //TODO: use later;
                }
            }catch (JSONException jex) {
                Log.w(LOG_TAG, "fillExtraData exception "+jex.getMessage(), jex);
            }
        } while (cursor.moveToNext());
    }
    private void setupExtraListViews( ) {
        ArrayAdapter<Details.Video> videoAdapter = new ArrayAdapter<Details.Video>(getContext(), R.layout.trailer_list_item ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View rowView = convertView;
                final Details.Video video = super.getItem(position);
                if (rowView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    rowView = inflater.inflate(R.layout.trailer_list_item, parent, false);
                    ImageView playIcon = (ImageView)rowView.findViewById(R.id.trailer_video_control);
                    playIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utilities.watchYouTube(v.getContext(), video.getVideoKey());
                        }
                    });
                }
                ((TextView)rowView.findViewById(R.id.trailer_textView)).setText(video.getName());
                 return rowView;
            }
        };
        mTailerListView.setAdapter(videoAdapter);

        mReviewListView.setAdapter(new ArrayAdapter<Details.Review>(getContext(), R.layout.review_list_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View rowView = convertView;
                Details.Review review = super.getItem(position);
                if (rowView == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    rowView = inflater.inflate(R.layout.review_list_item, parent, false);
                }
                ((TextView) rowView.findViewById(R.id.review_item_textView)).setText(review.getContent());
                return rowView;
            }
        } );
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
        if ((id == mMovieLoaderId) && (args != null)) {
            Uri uri = args.getParcelable(Constants.DETAIL_URI);
            return new android.support.v4.content.CursorLoader(getActivity(), uri, MOVIE_COLUMNS,null,null,null );
        } else if (id == mDetailLoaderId) {
            Uri uri = args.getParcelable(Constants.MORE_DETAIL_URI);
            return new android.support.v4.content.CursorLoader(getActivity(),uri,EXTRA_DETAIL_COLUMNS,null,null,null );
        } else
            return null;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == mMovieLoaderId) {
            if (data.moveToFirst())
                fillPage(data);
        } else if (loader.getId() == mDetailLoaderId) {
            if (data.moveToFirst()) {
                fillExtraData(data);
            } else {
                MovieSyncAdapter.syncImmediately(getContext(), mMovieId);
            }
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
