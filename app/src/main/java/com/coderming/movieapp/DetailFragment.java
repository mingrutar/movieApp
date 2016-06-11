package com.coderming.movieapp;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    ListView mTrailers;
    ListView mReviews;
    ImageView mMyStar;

    public static final String[] MOVIE_COLUMNS = {
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_VOTE_COUNT,
            MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_OVERVIEW      };
    public static final int COL_TITLE = 0;
    public static final int COL_RELEASE_DATE = 1;
    public static final int COL_VOTE_COUNT = 2;
    public static final int COL_VOTE_AVERAGE = 3;
    public static final int COL_POSTER_PATH = 4;
    public static final int COL_OVERVIEW = 5;

    public static final String[] EXTRA_DETAIL_COLUMNS = {
            MovieContract.DetailEntry.COLUMN_TYPE,
            MovieContract.DetailEntry.COLUMN_DETAIL_DATA };
    public static final int COL_TYPE = 0;
    public static final int COL_DETAIL_DATA = 1;

    public DetailFragment() {
        // Required empty public constructor
        mMovieLoaderId = Constants.nextId();
        mDetailLoaderId = Constants.nextId();
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
        String imagePath = cursor.getString(COL_POSTER_PATH);
        String url = String.format(Constants.FORMATTER_PICASSO_IMAGE_LOADER
                , String.valueOf(getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_342))
                , imagePath);
        Picasso.with(getContext()).load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mPoster.setImageBitmap(bitmap);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.w(LOG_TAG, "Fail to load backdrop image");
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }
    private void fillVideo(List<Details.Video> videos) {
        ArrayAdapter ad = ArrayAdapter. .createFromResource( getContext(), videos,
                getResources().getLayout(R.layout.trailer_list_item) );
        mTrailers.setAdapter( );
    }
    private void fillReiew(List<Details.Review> reviews)  {
        //        mReviews;
    }
    private void fillExtraData(Cursor cursor) {
        do {
            try {
                String type = cursor.getString(COL_TYPE);
                if ("videos".equals(type)) {
                    List<Details.Video> videos =  Details.parseVideos(cursor.getString(COL_DETAIL_DATA));
                    fillVideo(videos) ;
                } else if ("reviews".equals(type)) {
                    List<Details.Review> reviews =  Details.parseReviews(cursor.getString(COL_DETAIL_DATA));
                    fillReiew(reviews);
                } else {
                    List<Details.Image> reviews = Details.parseImages(cursor.getString(COL_DETAIL_DATA));
                    //TODO: use later;
                }
            }catch (JSONException jex) {
                Log.w(LOG_TAG, "fillExtraData exception "+jex.getMessage(), jex);
            }
        } while (cursor.moveToNext());
    }
    private void setupTrailerList(ListView trailers) {
       ArrayAdapter<String> adapter = new ArrayAdapter<String>( )
    }
    private void setupReviewList(ListView reviews) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), null, null  ) {

        };
        reviews.setAdapter(adapter);
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
        mMyStar = (ImageView) root.findViewById(R.id.favority_imageView);
        mPoster = (ImageView) root.findViewById(R.id.poster_imageView);
        mOverview = (TextView) root.findViewById(R.id.overview_textView);

        setupTrailerList ((ListView) root.findViewById(R.id.trailer_listView));
        setupReviewList((ListView) root.findViewById(R.id.review_listView));
        return root;
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

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
