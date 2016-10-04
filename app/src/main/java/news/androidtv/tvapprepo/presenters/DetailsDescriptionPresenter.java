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

package news.androidtv.tvapprepo.presenters;

import android.content.Context;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.model.Apk;
import news.androidtv.tvapprepo.model.LeanbackShortcut;
import news.androidtv.tvapprepo.model.RepoDatabase;

/**
 * This presenter provides a detailed
 */
public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
    private static final String TAG = DetailsDescriptionPresenter.class.getSimpleName();

    private Apk mApplication;
    private Context mContext;

    public DetailsDescriptionPresenter(Context context) {
        mContext = context;
    }

    @Override
    protected void onBindDescription(final ViewHolder viewHolder, final Object item) {
        mApplication = (Apk) item;

        if (mApplication != null) {
            viewHolder.getTitle().setText(mApplication.getName());
            viewHolder.getSubtitle().setText("Version " + mApplication.getVersionName() + "  (" +
                    mApplication.getVersionCode() + ")");
            RepoDatabase.getLeanbackShortcut(mApplication.getPackageName(),
                    new RepoDatabase.LeanbackShortcutCallback() {
                @Override
                public void onNoLeanbackShortcut() {
                    Log.d(TAG, "This app does not have a LeanbackShortcut");
                    viewHolder.getBody().setText(R.string.app_leanback);
                }

                @Override
                public void onLeanbackShortcut(LeanbackShortcut leanbackShortcut) {
                    Log.d(TAG, "This app has a LeanbackShortcut");
                    viewHolder.getBody().setText(R.string.app_not_leanback);
                }

                @Override
                public void onDatabaseError(DatabaseError error) {
                    Log.e(TAG, error.getMessage());
                }
            });
        }
    }
}
