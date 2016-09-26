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

import java.io.File;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.Utils;
import news.androidtv.tvapprepo.activities.DetailsActivity;
import news.androidtv.tvapprepo.model.Apk;
import news.androidtv.tvapprepo.model.RepoDatabase;
import news.androidtv.tvapprepo.presenters.ApkPresenter;
import news.androidtv.tvapprepo.utils.PackageInstallerUtils;
import tv.puppetmaster.tinydl.PackageInstaller;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 15;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        prepareBackgroundManager();
        setupUIElements();
        loadRows();
        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        ApkPresenter cardPresenter = new ApkPresenter();
        final ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        listRowAdapter.addAll(0, RepoDatabase.getInstance(RepoDatabase.DATABASE_TYPE_TESTING).getAppList());
        RepoDatabase.getInstance(RepoDatabase.DATABASE_TYPE_TESTING).addListener((apk, index) -> {
            if (apk.getPackageName().equals(Utils.class.getPackage().getName())) {
                if (PackageInstallerUtils.isUpdateAvailable(getActivity(), apk)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("There is an update for the Tv App Repo")
                            .setPositiveButton("Update", (dialog, which) -> {
                                PackageInstaller packageInstaller =
                                        PackageInstaller.initialize(getActivity());
                                packageInstaller.wget(apk.getDownloadUrl());
                                packageInstaller.addListener(new PackageInstaller.DownloadListener() {
                                    @Override
                                    public void onApkDownloaded(File downloadedApkFile) {
                                        packageInstaller.install(downloadedApkFile);
                                    }

                                    @Override
                                    public void onApkDownloadedNougat(File downloadedApkFile) {
                                        new Handler(Looper.getMainLooper()).postDelayed(() ->
                                                packageInstaller.install(downloadedApkFile),
                                                1000 * 5);
                                    }

                                    @Override
                                    public void onFileDeleted(File deletedApkFile, boolean wasSuccessful) {
                                    }

                                    @Override
                                    public void onProgressStarted() {
                                    }

                                    @Override
                                    public void onProgressEnded() {
                                    }
                                });
                            })
                            .show();
                }
            } else {
                listRowAdapter.add(apk);
                listRowAdapter.notifyArrayItemRangeChanged(index, 1);
            }
        });
        HeaderItem header = new HeaderItem(0, "Browse");
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
        // TODO Add credits for TinyDL
        // TODO Add a TinyDL downloader
        setAdapter(mRowsAdapter);
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
//        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
/*        setOnSearchClickedListener(view ->
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                .show());*/

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    protected void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Apk) {
                Apk application = (Apk) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.APPLICATION, application.toString());

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Apk) {
                mBackgroundURI = URI.create(((Apk) item).getBanner());
                startBackgroundTimer();
            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI.toString());
                    }
                }
            });
        }
    }
}
