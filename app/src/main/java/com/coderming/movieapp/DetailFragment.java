package com.coderming.movieapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

    private static final String MOVIE_FUN_SHARE_HASHTAG = "#MovieFunApp";
    private static final String DBLOADER_MOVIE_ID = "DBLOADERMOVIE_ID";
    private static final String DBLOADER_EXTRA_ID = "DBLOADEREXTRA_ID";

    private int mMovieLoaderId = -1;
    private int mDetailLoaderId = -1;
    private long mMovieId;

    private ShareActionProvider mShareActionProvider;
    private String mMyShareString;

    CardView mCardView;
    TextView mTitle;
    TextView mReleaseDate;
    TextView mNumVote;
    TextView mVoteAverage;
    ImageView mPoster;
    TextView mOverview;
    FloatingActionButton mFab;
    View mImage_Container;
    TextView mTailerTitle;
    ListView mTailerListView;
    TextView mReviewTitle;
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
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMovieLoaderId != -1)
            outState.putInt(DBLOADER_MOVIE_ID, mMovieLoaderId);
        if (mDetailLoaderId != -1)
            outState.putInt(DBLOADER_EXTRA_ID, mDetailLoaderId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(DBLOADER_MOVIE_ID))
                mMovieLoaderId = savedInstanceState.getInt(DBLOADER_MOVIE_ID);
            if (savedInstanceState.containsKey(DBLOADER_EXTRA_ID))
                mDetailLoaderId = savedInstanceState.getInt(DBLOADER_EXTRA_ID);
        }
        if (mMovieLoaderId == -1)
            mMovieLoaderId = Constants.nextId();
        if (mDetailLoaderId == -1)
            mDetailLoaderId = Constants.nextId();

        Bundle args = getArguments();
        getLoaderManager().initLoader(mMovieLoaderId, args, this);
        if (args != null) {
            Uri uri = args.getParcelable(Constants.DETAIL_URI);
            mMovieId = Long.parseLong(uri.getLastPathSegment());
            Uri uriDetail = MovieContract.DetailEntry.buildUri(mMovieId);
            Bundle dargs = getArguments();
            dargs.putParcelable(Constants.MORE_DETAIL_URI, uriDetail);
            getLoaderManager().initLoader(mDetailLoaderId, dargs, this);
        }

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitle = (TextView) root.findViewById(R.id.title_textView)  ;
        mCardView= (CardView) root.findViewById(R.id.overview_card_view);
        mReleaseDate = (TextView) root.findViewById(R.id.release_textView);
        mNumVote = (TextView) root.findViewById(R.id.nVoters_textView) ;
        mVoteAverage = (TextView) root.findViewById(R.id.nStar_textView);
        mMyFavorite = (ImageView) root.findViewById(R.id.favority_imageView);
        mPoster = (ImageView) root.findViewById(R.id.poster_imageView);
        mOverview = (TextView) root.findViewById(R.id.overview_textView);
        mImage_Container = root.findViewById(R.id.poster_container);
        mTailerTitle = (TextView) root.findViewById(R.id.trail_header);
        mTailerListView = (ListView) root.findViewById(R.id.trailer_listView);
        mReviewTitle = (TextView) root.findViewById(R.id.review_header);
        mReviewListView = (ListView) root.findViewById(R.id.review_listView);
        setupExtraListViews ();

        mFab = (FloatingActionButton) root.findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.addFavoriteMovie(getContext(), mMovieId);
                mMyFavorite.setVisibility(View.VISIBLE);
                v.setVisibility(View.INVISIBLE);
            }
        });

        return root;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // thisi different from http://developer.android.com/training/sharing/shareaction.html#set-share-intent
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider( menuItem);
        if (mMyShareString != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null");
        }
    }
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET prevent this activity be put in activity stack
        // so it will return back to this app.
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMyShareString+MOVIE_FUN_SHARE_HASHTAG);
        return shareIntent;
    }

    private void fillPage(Cursor cursor) {
        mTitle.setText( cursor.getString(COL_TITLE));
        mReleaseDate.setText(Utilities.releaseDate2Str(cursor.getLong(COL_RELEASE_DATE)));
        double vote_average = cursor.getDouble(COL_VOTE_AVERAGE);
        mVoteAverage.setText(String.format("%.01f/10", vote_average));
        mNumVote.setText(String.format("%d votes", cursor.getInt(COL_VOTE_COUNT)));
        mOverview.setText(cursor.getString(COL_OVERVIEW));
        mPoster.setImageResource(0);

        ViewTreeObserver vto = mPoster.getViewTreeObserver();
        vto.addOnPreDrawListener( new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mPoster.getViewTreeObserver().removeOnPreDrawListener(this);
                int w = mPoster.getWidth();
                mCardView.getLayoutParams().height = mPoster.getHeight();

//                int pw = mImage_Container.getWidth();
//                if (w > (pw/2))  {
//                    mPoster.getLayoutParams().width = pw/2;
//                }
                return true;
            }
        });

        if (Utilities.isFavorite(mMovieId)) {
            mMyFavorite.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.INVISIBLE);
        }

        String imagePath = cursor.getString(COL_POSTER_PATH);
        final String url = String.format(Constants.FORMATTER_PICASSO_IMAGE_LOADER
                , String.valueOf(getResources().getDimensionPixelSize(R.dimen.moviedb_image_width_185))
                , imagePath);

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

        mMyShareString = String.format("movie %s (%s), %s stars of %s votes",cursor.getString(COL_TITLE),
                mReleaseDate.getText(), mVoteAverage.getText(), mNumVote.getText());
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private static final int UNBOUNDED = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    private void fillExtraData(Cursor cursor) {
        do {
            try {
                String type = cursor.getString(COL_TYPE);
                if ("videos".equals(type)) {
                    List<Details.Video> videos =  Details.parseVideos(cursor.getString(COL_DETAIL_DATA));
                    int numVideo = videos.size();
                    if (numVideo > 0) {
                        mTailerListView.setVisibility(View.VISIBLE);
                        ((ArrayAdapter<Details.Video>) mTailerListView.getAdapter()).addAll(videos);
                        ViewGroup.LayoutParams params = mTailerListView.getLayoutParams();
                        int nv = (numVideo > 3) ? 3 :numVideo;
                        params.height = nv * getResources().getDimensionPixelSize(R.dimen.trailerListItemHeight) ;
                        mTailerListView.setLayoutParams(params);
                        mTailerListView.requestLayout();
                    } else {
                        mTailerListView.setVisibility(View.INVISIBLE);
                    }
                    mTailerTitle.setText(String.format("Trailers (%d)", numVideo));
                } else if ("reviews".equals(type)) {
                    List<Details.Review> reviews =  Details.parseReviews(cursor.getString(COL_DETAIL_DATA));
                    int numReview = reviews.size();
                    if (reviews.size() > 0) {
                        mReviewListView.setVisibility(View.VISIBLE);
                        ((ArrayAdapter<Details.Review>)mReviewListView.getAdapter()).addAll(reviews) ;
                        ViewGroup.LayoutParams params = mReviewListView.getLayoutParams();
                        mReviewListView.requestLayout();
                    } else {
                        mReviewListView.setVisibility(View.INVISIBLE);
                    }
                    mReviewTitle.setText(String.format("Revies (%d)", numReview) );
//                } else {
//                    List<Details.Image> images = Details.parseImages(cursor.getString(COL_DETAIL_DATA));
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
                    rowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utilities.playYouTube(v.getContext(), video.getVideoKey());
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
        } else if ((id == mDetailLoaderId) && (args != null))  {
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
