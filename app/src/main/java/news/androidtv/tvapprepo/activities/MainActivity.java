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

package news.androidtv.tvapprepo.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.DependsOn;
import news.androidtv.tvapprepo.PrivateUtils;
import news.androidtv.tvapprepo.R;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
//        checkSelfVersion();
    }

    @Deprecated
    public void checkSelfVersion() {
        // Make a call to Firebase
    }
}
