package news.androidtv.tvapprepo.download;

import android.app.Activity;

import com.colortv.android.AdPlacement;
import com.colortv.android.ColorTvAdListener;
import com.colortv.android.ColorTvError;
import com.colortv.android.ColorTvSdk;

/**
 * Created by Nick on 9/23/2016.
 */
public abstract class AbstractDownloadHelper {
    private static final double AD_RATE = 1/4;
    private static final boolean mDisableAds = false;

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
