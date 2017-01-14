package news.androidtv.tvapprepo.fragments;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;

import news.androidtv.tvapprepo.R;

/**
 * Created by Nick on 1/14/2017.
 */

public class PrefsFragment extends LeanbackPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}