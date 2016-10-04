/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package news.androidtv.tvapprepo.fragments;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.DatabaseError;

import java.io.File;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.Utils;
import news.androidtv.tvapprepo.activities.DetailsActivity;
import news.androidtv.tvapprepo.activities.MainActivity;
import news.androidtv.tvapprepo.model.Apk;
import news.androidtv.tvapprepo.model.LeanbackShortcut;
import news.androidtv.tvapprepo.model.RepoDatabase;
import news.androidtv.tvapprepo.presenters.DetailsDescriptionPresenter;
import news.androidtv.tvapprepo.utils.PackageInstallerUtils;
import tv.puppetmaster.tinydl.PackageInstaller;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = VideoDetailsFragment.class.getSimpleName();

    private static final int ACTION_INSTALL = 11;
    private static final int ACTION_UPDATE = 12;
    private static final int ACTION_UNINSTALL = 13;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private Apk mSelectedApk;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private PackageInstaller mPackageInstaller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();
        mSelectedApk = new Apk.Builder(getActivity().getIntent()
                .getStringExtra(DetailsActivity.APPLICATION)).build();
        if (mSelectedApk != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setupMovieListRow();
            setupMovieListRowPresenter();
            updateBackground(mSelectedApk.getBanner());
            setOnItemViewClickedListener(new ItemViewClickedListener());
            mPackageInstaller = PackageInstaller.initialize(getActivity());
            RepoDatabase.getInstance().incrementApkViews(mSelectedApk);
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            Toast.makeText(getActivity(), "No app selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mPackageInstaller.destroy();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void updateBackground(String uri) {
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
    }

    private void setupAdapter() {
        mPresenterSelector = new ClassPresenterSelector();
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedApk.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedApk);
        row.setImageDrawable(getResources().getDrawable(R.drawable.default_background));
        int width = Utils.convertDpToPixel(getActivity()
                .getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = Utils.convertDpToPixel(getActivity()
                .getApplicationContext(), DETAIL_THUMB_HEIGHT);
        Glide.with(getActivity())
                .load(mSelectedApk.getIcon())
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        Log.d(TAG, "details overview card image url ready: " + resource);
                        row.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });

        // See if this is already installed. If so, check if it can be updated.
        // Offer an uninstall option.
        // Add an install button.
        SparseArrayObjectAdapter possibleActions = new SparseArrayObjectAdapter();
        PackageManager packageManager = getActivity().getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(mSelectedApk.getPackageName(), 0);
            if (info.versionCode < mSelectedApk.getVersionCode()) {
                possibleActions.set(ACTION_UPDATE,
                        new Action(ACTION_UPDATE, getString(R.string.update)));
            }
            possibleActions.set(ACTION_UNINSTALL,
                    new Action(ACTION_UNINSTALL, getString(R.string.uninstall)));
        } catch (PackageManager.NameNotFoundException e) {
            // App is not installed
            possibleActions.set(ACTION_INSTALL,
                    new Action(ACTION_INSTALL, getString(R.string.install)));
        }
        row.setActionsAdapter(possibleActions);

        mAdapter.add(row);
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background and style.
        DetailsOverviewRowPresenter detailsPresenter =
                new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter(getActivity()));
        detailsPresenter.setBackgroundColor(getResources().getColor(R.color.selected_background));
        detailsPresenter.setStyleLarge(true);

        // Hook up transition element.
        detailsPresenter.setSharedElementEnterTransition(getActivity(),
                DetailsActivity.SHARED_ELEMENT_NAME);
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void setupMovieListRow() {

    }

    private void setupMovieListRowPresenter() {
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Apk) {
                Apk application = (Apk) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(getResources().getString(R.string.movie), mSelectedApk.toString());
                intent.putExtra(getResources().getString(R.string.should_start), true);
                startActivity(intent);


                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            } else if (item instanceof Action) {
                Log.d(TAG, "Item: " + item.toString());
                Action action = (Action) item;
                if (action.getId() == ACTION_INSTALL ||
                        action.getId() == ACTION_UPDATE) {

                    RepoDatabase.getInstance().incrementApkDownloads(mSelectedApk);
                    mPackageInstaller.wget(mSelectedApk.getDownloadUrl());
                    RepoDatabase.getLeanbackShortcut(mSelectedApk.getPackageName(),
                            new RepoDatabase.LeanbackShortcutCallback() {
                        @Override
                        public void onNoLeanbackShortcut() {

                        }

                        @Override
                        public void onLeanbackShortcut(LeanbackShortcut leanbackShortcut) {
                            mPackageInstaller.wget(leanbackShortcut.getDownload());
                        }

                        @Override
                        public void onDatabaseError(DatabaseError error) {

                        }
                    });
                } else if (action.getId() == ACTION_UNINSTALL) {
                    PackageInstallerUtils.uninstallApp(getActivity(), mSelectedApk.getPackageName());
                }
            }
        }
    }
}
