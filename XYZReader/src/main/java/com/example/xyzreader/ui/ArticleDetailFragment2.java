package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * Created by Tejas Sherdiwala on 10/29/2016.
 * &copy; Knoxpo
 */

public class ArticleDetailFragment2 extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String
            TAG = ArticleDetailFragment2.class.getSimpleName(),
            ARGS_ITEM_ID = TAG + ".ARGS_ITEM_ID";

    public static ArticleDetailFragment2 newInstance(long itemId) {

        Bundle args = new Bundle();
        args.putLong(ARGS_ITEM_ID, itemId);
        ArticleDetailFragment2 fragment = new ArticleDetailFragment2();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callback {
        void onPaletteFetched(long id, ArticleDetailFragment2 fragment);
    }

    private Callback mCallback;

    private long mItemId;
    private int mMutedColor = 0xFF333333;
    private ImageView mPhotoView;
    private TextView mTitleTV, mAuthorTV, mBodyTV;
    private Cursor mCursor;
    private View mRootView;
    private FloatingActionButton mShareFAB;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) getActivity();
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemId = getArguments().getLong(ARGS_ITEM_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_details2, container, false);
        init(mRootView);

        mShareFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        return mRootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    private void init(View v) {
        mShareFAB = (FloatingActionButton) v.findViewById(R.id.share_fab);
        mPhotoView = (ImageView) v.findViewById(R.id.backdrop);
        mTitleTV = (TextView) v.findViewById(R.id.article_title);
        mAuthorTV = (TextView) v.findViewById(R.id.article_byline);
        mBodyTV = (TextView) v.findViewById(R.id.article_body);
    }

    private void updateUI() {

        if (mRootView == null) {
            return;
        }

        mAuthorTV.setMovementMethod(new LinkMovementMethod());
        mBodyTV.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            mTitleTV.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            mAuthorTV.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

            mBodyTV.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);

                                mPhotoView.setImageBitmap(imageContainer.getBitmap());

                                mRootView
                                        .findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);

                                if (mCallback != null) {
                                    mCallback.onPaletteFetched(mItemId, ArticleDetailFragment2.this);
                                }

                                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    updateStatusBar();
                                }*/
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            mTitleTV.setText("N/A");
            mAuthorTV.setText("N/A");
            mBodyTV.setText("N/A");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public final void updateStatusBar(Window window) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getUserVisibleHint()) {

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            int color = Color.argb(255,
                    (int) (Color.red(mMutedColor) * 0.8),
                    (int) (Color.green(mMutedColor) * 0.8),
                    (int) (Color.blue(mMutedColor) * 0.8));

            window.setStatusBarColor(color);
        }
        //window.setStatusBarColor(getActivity().getResources().getColor(R.color.my_statusbar_color));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }
        updateUI();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        updateUI();
    }
}
