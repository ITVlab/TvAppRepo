package news.androidtv.tvapprepo.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;
import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.Utils;
import news.androidtv.tvapprepo.activities.DetailsActivity;
import news.androidtv.tvapprepo.activities.SettingsActivity;
import news.androidtv.tvapprepo.download.ApkDownloadHelper;
import news.androidtv.tvapprepo.model.Apk;
import news.androidtv.tvapprepo.model.RepoDatabase;
import news.androidtv.tvapprepo.model.SettingOption;
import news.androidtv.tvapprepo.presenters.ApkPresenter;
import news.androidtv.tvapprepo.presenters.DownloadedFilesPresenter;
import news.androidtv.tvapprepo.presenters.LauncherActivitiesPresenter;
import news.androidtv.tvapprepo.presenters.OptionsCardPresenter;
import news.androidtv.tvapprepo.ui.ShortcutGeneratorDialogs;
import news.androidtv.tvapprepo.utils.GenerateShortcutHelper;
import news.androidtv.tvapprepo.utils.PackageInstallerUtils;
import tv.puppetmaster.tinydl.PackageInstaller;

public class MainFragment extends BrowseFragment {
    private static final String TAG = MainFragment.class.getSimpleName();

    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private boolean checkedForUpdates = true;
    private Activity mMainActivity;
    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;
    private ApkDownloadHelper mApkDownloadHelper;
    private Set<String> downloadedApkFiles = new HashSet<>();
    private PackageInstaller.DownloadListener mDownloadListener = new PackageInstaller.DownloadListener() {
        @Override
        public void onApkDownloaded(File downloadedApkFile) {
            Log.d(TAG, downloadedApkFiles.toString());
            if (!downloadedApkFiles.contains(downloadedApkFile.getAbsolutePath())) {
                downloadedApkFiles.add(downloadedApkFile.getAbsolutePath());
                mApkDownloadHelper.install(downloadedApkFile);
            }
        }

        @Override
        public void onApkDownloadedNougat(final File downloadedApkFile) {
            Log.d(TAG, downloadedApkFiles.toString());
            if (!downloadedApkFiles.contains(downloadedApkFile.getAbsolutePath())) {
                downloadedApkFiles.add(downloadedApkFile.getAbsolutePath());
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, downloadedApkFiles.toString());
                        mApkDownloadHelper.install(downloadedApkFile);
                    }
                }, 1000 * 4);
            }

        }

        @Override
        public void onFileDeleted(File deletedApkFile, boolean wasSuccessful) {

        }

        @Override
        public void onProgressStarted() {
            // Show a video ad
        }

        @Override
        public void onProgressEnded() {

        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onCreate");
        prepareBackgroundManager();
        setupUIElements();
        loadRows();
        setupEventListeners();
        mMainActivity = getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
        mApkDownloadHelper.removeListener(mDownloadListener);
        mApkDownloadHelper.destroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRows();
    }

    private List<File> getApkDownloads(List<File> files, File[] startingPoint) {
        // Recursively searches for APKs in your Downloads
        if (startingPoint == null) {
            // Either we don't have permission or otherwise can't obtain files
            return new ArrayList<>(); // Return empty list
        }
        for (File file : startingPoint) {
            if (file.isDirectory()) {
                files = getApkDownloads(files, file.listFiles());
            } else if (file.getName().endsWith(".apk")) {
                files.add(file);
            }
        }
        return files;
    }

    private void loadRows() {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        mApkDownloadHelper = new ApkDownloadHelper(getActivity());
        // Setup the package installer for the session
        mApkDownloadHelper.addListener(mDownloadListener);
        HeaderItem header = null;

        if (getResources().getBoolean(R.bool.ENABLE_APP_REPO)) {
            createRowApkDownloads();
        }

        if (getResources().getBoolean(R.bool.ENABLE_DOWNLOADS_ROW)) {
            createRowDownloadedApks();
        }

        createRowShortcutGenerator();

        createRowCustomShortcuts();

        createRowMisc();

        setAdapter(mRowsAdapter);
    }

    private void createRowApkDownloads() {
        // Add a presenter for APKs - only if allowed
        ApkPresenter cardPresenter = new ApkPresenter(getActivity());
        final ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
        Log.d(TAG, "Get RepoDatabase instance");
        listRowAdapter.addAll(0, RepoDatabase.getInstance().getAppList());
        for (Apk apk : RepoDatabase.getInstance().getAppList()) {
            Log.d(TAG, apk.getPackageName() + " " + Utils.class.getPackage().getName());
            if (apk.getPackageName().equals(Utils.class.getPackage().getName())) {
                checkForAppUpdates(apk);
            }
        }
        RepoDatabase.getInstance().addListener(new RepoDatabase.Listener() {
            @Override
            public void onApkAdded(Apk apk, int index) {
                Log.d(TAG, apk.getPackageName() + " " + Utils.class.getPackage().getName());
                if (apk.getPackageName().equals(Utils.class.getPackage().getName())) {
                    checkForAppUpdates(apk);
                } else {
                    listRowAdapter.add(apk);
                    listRowAdapter.notifyArrayItemRangeChanged(index, 1);
                }
            }
        });
        HeaderItem header = new HeaderItem(0, getString(R.string.header_browse));
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void createRowDownloadedApks() {
        // Add a row for downloaded APKs
        DownloadedFilesPresenter downloadedFilesPresenter = new DownloadedFilesPresenter();
        ArrayObjectAdapter downloadedFilesAdapter = new ArrayObjectAdapter(downloadedFilesPresenter);
        File myDownloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        // Add them to a list first before we sort them.
        List<File> downloadedFilesList = new ArrayList<>();
        downloadedFilesList = getApkDownloads(downloadedFilesList, myDownloads.listFiles());

        // Now sort
        try {
            Collections.sort(downloadedFilesList, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    try {
                        return (int) (file.lastModified() - t1.lastModified());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(e.getMessage() + " with " +
                                file.lastModified() + " & " + t1.lastModified());
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            // Sorting fail. Issue w/ contract?
            // We don't sort.
        }
        downloadedFilesAdapter.addAll(0, downloadedFilesList);
        HeaderItem downloadedFilesHeader = new HeaderItem(1, getString(R.string.header_downloaded_apks));
        mRowsAdapter.add(new ListRow(downloadedFilesHeader, downloadedFilesAdapter));
    }

    private void createRowShortcutGenerator() {
        // Add a row for Leanback shortcuts
        // First, let's map all Leanback Launcher apps
        Intent leanbacks = new Intent(Intent.ACTION_MAIN);
        leanbacks.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> leanbackActivities = getActivity().getPackageManager()
                .queryIntentActivities(leanbacks, PackageManager.MATCH_ALL);
        Set<String> leanbackPackageNames = new HashSet<>();
        for (ResolveInfo info : leanbackActivities) {
            leanbackPackageNames.add(info.activityInfo.applicationInfo.packageName);
        }

        Intent shortcutables = new Intent(Intent.ACTION_MAIN);
        shortcutables.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> launcherActivitiesTemp = getActivity().getPackageManager()
                .queryIntentActivities(shortcutables, PackageManager.MATCH_ALL);
        // Need to convert to an arraylist to iterate
        ArrayList<ResolveInfo> launcherActivities = new ArrayList<>();
        launcherActivities.addAll(launcherActivitiesTemp);

        Iterator<ResolveInfo> infoIterator = launcherActivities.iterator();
        while (infoIterator.hasNext()) {
            ResolveInfo info = infoIterator.next();

            // Filter out those where shortcuts already exist
            String adjustedName = info.activityInfo.applicationInfo.loadLabel(getActivity().getPackageManager()).toString();
            adjustedName = adjustedName.toLowerCase().trim();
            Intent shortcut = getActivity().getPackageManager().getLeanbackLaunchIntentForPackage(
                    "de.eye_interactive.atvl." + adjustedName
            );
            try {
                if (shortcut != null) {
                    infoIterator.remove();
                } else if (leanbackPackageNames.contains(info.activityInfo.applicationInfo.packageName)) {
                    // Filter out those activities that are both launcher types
                    infoIterator.remove();
                }
            } catch (Exception e) {
                // Hmm...
            }
        }
        Log.d(TAG, launcherActivities.toString());
        LauncherActivitiesPresenter launcherActivitiesPresenter = new LauncherActivitiesPresenter();
        ArrayObjectAdapter launcherActivitiesAdapter = new ArrayObjectAdapter(launcherActivitiesPresenter);
        launcherActivitiesAdapter.addAll(0, launcherActivities);
        HeaderItem launcherActivitiesHeader = new HeaderItem(2, getString(R.string.leanback_shortcuts));
        mRowsAdapter.add(new ListRow(launcherActivitiesHeader, launcherActivitiesAdapter));
    }

    private void createRowCustomShortcuts() {
        if (!getResources().getBoolean(R.bool.ENABLE_RAW_INTENTS)) {
            return; // Don't do this at all.
        }
        // Add a row for credits
        OptionsCardPresenter optionsCardPresenter = new OptionsCardPresenter();
        ArrayObjectAdapter optionsRowAdapter = new ArrayObjectAdapter(optionsCardPresenter);
        if (getResources().getBoolean(R.bool.ENABLE_WEB_BOOKMARKS)) {
            optionsRowAdapter.add(new SettingOption(
                    getResources().getDrawable(R.drawable.web_bookmark),
                    getString(R.string.generate_web_bookmark),
                    new SettingOption.OnClickListener() {
                        @Override
                        public void onClick() {
                            ShortcutGeneratorDialogs.initWebBookmarkDialog(getActivity());
                        }
                    }
            ));
        }
        if (getResources().getBoolean(R.bool.ENABLE_FOLDERS)) {
            optionsRowAdapter.add(new SettingOption(
                    getResources().getDrawable(R.drawable.folder),
                    getString(R.string.generate_folder),
                    new SettingOption.OnClickListener() {
                        @Override
                        public void onClick() {
                            new MaterialDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                                    .title(R.string.generate_folder)
                                    .customView(R.layout.dialog_folder, false)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String tag = ((EditText) dialog.getCustomView().findViewById(R.id.tag)).getText().toString();

                                            Toast.makeText(getActivity(), R.string.starting_download, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .positiveText(R.string.generate_shortcut)
                                    .show();
                        }
                    }
            ));
        }
        if (getResources().getBoolean(R.bool.ENABLE_SETTINGS)) {
            optionsRowAdapter.add(new SettingOption(
                    getResources().getDrawable(R.drawable.sys_settings),
                    getString(R.string.generate_settings),
                    new SettingOption.OnClickListener() {
                        @Override
                        public void onClick() {
                            new MaterialDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                                    .title(R.string.generate_settings)
                                    .customView(R.layout.dialog_folder, false)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String tag = ((EditText) dialog.getCustomView().findViewById(R.id.tag)).getText().toString();

                                            Toast.makeText(getActivity(), R.string.starting_download, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .positiveText(R.string.generate_shortcut)
                                    .show();
                        }
                    }
            ));
        }
        if (getResources().getBoolean(R.bool.ENABLE_APP_DEEPLINKS)) {
            optionsRowAdapter.add(new SettingOption(
                    getResources().getDrawable(R.drawable.deep_link),
                    getString(R.string.generate_deep_link),
                    new SettingOption.OnClickListener() {
                        @Override
                        public void onClick() {
                            new MaterialDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                                    .title(R.string.generate_deep_link)
                                    .customView(R.layout.dialog_folder, false)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String tag = ((EditText) dialog.getCustomView().findViewById(R.id.tag)).getText().toString();

                                            Toast.makeText(getActivity(), R.string.starting_download, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .positiveText(R.string.generate_shortcut)
                                    .show();
                        }
                    }
            ));
        }
        if (getResources().getBoolean(R.bool.ENABLE_FILE_URIS)) {
            optionsRowAdapter.add(new SettingOption(
                    getResources().getDrawable(R.drawable.file_location),
                    getString(R.string.generate_file_shortcut),
                    new SettingOption.OnClickListener() {
                        @Override
                        public void onClick() {
                            new MaterialDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                                    .title(R.string.generate_file_shortcut)
                                    .customView(R.layout.dialog_folder, false)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String tag = ((EditText) dialog.getCustomView().findViewById(R.id.tag)).getText().toString();

                                            Toast.makeText(getActivity(), R.string.starting_download, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .positiveText(R.string.generate_shortcut)
                                    .show();
                        }
                    }
            ));
        }

        HeaderItem header = new HeaderItem(2, getString(R.string.header_custom));
        mRowsAdapter.add(new ListRow(header, optionsRowAdapter));
    }

    private void createRowMisc() {
        // Add a row for credits
        OptionsCardPresenter optionsCardPresenter = new OptionsCardPresenter();
        ArrayObjectAdapter optionsRowAdapter = new ArrayObjectAdapter(optionsCardPresenter);
        if (getResources().getBoolean(R.bool.ENABLE_SIDELOADTAG)) {
            optionsRowAdapter.add(new SettingOption(
                    getResources().getDrawable(R.drawable.sideloadtag),
                    getString(R.string.install_through_sideloadtag),
                    new SettingOption.OnClickListener() {
                        @Override
                        public void onClick() {
                            new MaterialDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                                    .title(R.string.sideloadtag)
                                    .customView(R.layout.dialog_sideload_tag, false)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            String tag = ((EditText) dialog.getCustomView().findViewById(R.id.tag)).getText().toString();
                                            PackageInstaller.initialize(getActivity()).wget("http://tinyurl.com/" + tag);
                                            Toast.makeText(getActivity(), R.string.starting_download, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .positiveText(R.string.Download)
                                    .show();
                        }
                    }
            ));
        }
        optionsRowAdapter.add(new SettingOption(
                getResources().getDrawable(R.drawable.about_credits),
                getString(R.string.credits),
                new SettingOption.OnClickListener() {
                    @Override
                    public void onClick() {
                        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                                .setTitle(R.string.credits)
                                .setMessage(R.string.about_app)
                                .show();
                    }
                }
        ));
        optionsRowAdapter.add(new SettingOption(
                getResources().getDrawable(R.drawable.settings),
                getString(R.string.settings),
                new SettingOption.OnClickListener() {
                    @Override
                    public void onClick() {
                        startActivity(new Intent(getActivity(), SettingsActivity.class));
                    }
                }
        ));
        HeaderItem header = new HeaderItem(2, getString(R.string.header_more));
        mRowsAdapter.add(new ListRow(header, optionsRowAdapter));
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

    private void checkForAppUpdates(final Apk apk) {
        // Only tell the user once per session
        if (checkedForUpdates) {
            return;
        }
        if (PackageInstallerUtils.isUpdateAvailable(getActivity(), apk)) {
            checkedForUpdates = true;
            new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                    .setTitle(R.string.update_for_tv_app_repo)
                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mApkDownloadHelper.startDownload(apk.getDefaultDownloadUrl());
                        }
                    })
                    .show();
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, final Object item,
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
            } else if (item instanceof SettingOption) {
                ((SettingOption) item).getClickListener().onClick();
            } else if (item instanceof File) {
                Log.d(TAG, "Open file " + ((File) item).getAbsolutePath());
                mApkDownloadHelper.install((File) item);
            } else if (item instanceof ResolveInfo) {
                GenerateShortcutHelper.begin(mMainActivity, (ResolveInfo) item);
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
