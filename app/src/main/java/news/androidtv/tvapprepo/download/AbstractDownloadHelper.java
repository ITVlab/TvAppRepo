package news.androidtv.tvapprepo.download;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.colortv.android.AdPlacement;
import com.colortv.android.ColorTvAdListener;
import com.colortv.android.ColorTvError;
import com.colortv.android.ColorTvSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created by Nick on 9/23/2016.
 */
public abstract class AbstractDownloadHelper {
    private static double AD_RATE = 1/4;
    private static final boolean mDisableAds = false;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Activity mActivity;

    private ColorTvAdListener mListener = new ColorTvAdListener() {
        @Override
        public void onAdLoaded(String placement) {
            ColorTvSdk.showAd(placement);
        }

        @Override
        public void onAdError(String placement, ColorTvError colorTvError) {
        }

        @Override
        public void onAdClosed(String placement, boolean watched) {
        }

        @Override
        public void onAdExpired(String placement) {
        }
    };

    protected AbstractDownloadHelper() {

    }

    public AbstractDownloadHelper initialize(Activity activity) {
        mActivity = activity;
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mFirebaseRemoteConfig.activateFetched();
                AD_RATE = mFirebaseRemoteConfig.getLong("ad_rate");
            }
        });
        ColorTvSdk.init(activity, getAppId());
        ColorTvSdk.setRecordAudioEnabled(false);
        ColorTvSdk.onCreate();
        ColorTvSdk.registerAdListener(mListener);
        return this;
    }

    public void destroy() {
        ColorTvSdk.onDestroy();
    }

    public AbstractDownloadHelper startDownload(String url) {
        startDownload(url, AdPlacement.BETWEEN_LEVELS);
        return this;
    }

    public AbstractDownloadHelper startDownload(String url, String adType) {
        // Starts download
        // Also loads ad
        if (!mDisableAds && Math.random() < AD_RATE) {
            ColorTvSdk.loadAd(adType);
        }
        return this;
    }

    public abstract String getAppId();
}
