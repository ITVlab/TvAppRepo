package news.androidtv.tvapprepo.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.support.v14.preference.PreferenceFragment;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import news.androidtv.tvapprepo.R;

/**
 * Created by Nick on 1/14/2017.
 */

public class SettingsFragment extends LeanbackSettingsFragment
        implements DialogPreference.TargetFragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private static final String PREFERENCE_RESOURCE_ID = "preferenceResource";
    private static final String PREFERENCE_ROOT = "root";
    private PreferenceFragment mPreferenceFragment;

    @Override
    public void onPreferenceStartInitialScreen() {
        mPreferenceFragment = buildPreferenceFragment(R.xml.preferences, null);
        startPreferenceFragment(mPreferenceFragment);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment,
                                             Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment,
                                           PreferenceScreen preferenceScreen) {
        PreferenceFragment frag = buildPreferenceFragment(R.xml.preferences,
                preferenceScreen.getKey());
        startPreferenceFragment(frag);
        return true;
    }

    @Override
    public Preference findPreference(CharSequence charSequence) {
        return mPreferenceFragment.findPreference(charSequence);
    }

    private PreferenceFragment buildPreferenceFragment(int preferenceResId, String root) {
        PreferenceFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt(PREFERENCE_RESOURCE_ID, preferenceResId);
        args.putString(PREFERENCE_ROOT, root);
        fragment.setArguments(args);
        return fragment;
    }

    public static class PrefFragment extends LeanbackPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            String root = getArguments().getString(PREFERENCE_ROOT, null);
            int prefResId = getArguments().getInt(PREFERENCE_RESOURCE_ID);
            if (root == null) {
                addPreferencesFromResource(prefResId);
            } else {
                setPreferencesFromResource(prefResId, root);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("delete_all")) {
                File myDownloads = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                // Add them to a list first before we sort them.
                deleteApks(myDownloads);
                Toast.makeText(getActivity(), R.string.downloaded_files_deleted, Toast.LENGTH_SHORT).show();
            } else if (preference.getKey().equals("build_variant")) {
                new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.dialog_theme))
                        .setTitle(R.string.title_app_variants)
                        .setMessage(R.string.build_variant_explanation)
                        .show();
            }
            return super.onPreferenceTreeClick(preference);
        }

        private void deleteApks(File directory) {
            File[] downloadedFilesList = directory.listFiles();
            if (downloadedFilesList == null) {
                return; // There is nothing to delete!
            }
            for (File download : downloadedFilesList) {
                if (download.getAbsolutePath().toLowerCase().endsWith("apk")) {
                    boolean delete = download.delete();
                    if (delete) {
                        Log.d(TAG, "Deleted " + download.getAbsolutePath());
                    } else {
                        Log.d(TAG, "Cannot delete " + download.getAbsolutePath());
                    }
                } else if (download.isDirectory()) {
                    deleteApks(download);
                } else {
                    Log.d(TAG, download.getAbsolutePath() + " is not an APK");
                }
            }
        }
    }
}