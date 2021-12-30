package news.androidtv.tvapprepo.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.json.JSONException;
import org.json.JSONObject;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.activities.AdvancedShortcutActivity;
import news.androidtv.tvapprepo.download.ApkDownloadHelper;
import news.androidtv.tvapprepo.model.AdvancedOptions;

/**
 * Created by Nick Felker on 3/20/2017.
 */
public class GenerateShortcutHelper {
    private static final String TAG = GenerateShortcutHelper.class.getSimpleName();

    private static final String KEY_BUILD_STATUS = "build_ok";
    private static final String KEY_APP_OBJ = "app";
    private static final String KEY_DOWNLOAD_URL = "download_link";

    public static void begin(final Activity activity, final ResolveInfo resolveInfo) {
        new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.dialog_theme))
                .setTitle(activity.getString(R.string.title_shortcut_generator,
                        resolveInfo.activityInfo.applicationInfo.loadLabel(activity.getPackageManager())))
                .setMessage(R.string.shortcut_generator_info)
                .setPositiveButton(R.string.create_shortcut, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        generateShortcut(activity, resolveInfo);
                    }
                })
                .setNeutralButton(R.string.advanced, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open a new dialog
                        openAdvancedOptions(activity, resolveInfo);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void begin(final Activity activity, final String label, final AdvancedOptions options) {
        new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.dialog_theme))
                .setTitle(activity.getString(R.string.title_shortcut_generator, label))
                .setMessage(R.string.shortcut_generator_info)
                .setPositiveButton(R.string.create_shortcut, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        generateShortcut(activity, null, options);
                    }
                })
                .setNeutralButton(R.string.advanced, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open a new dialog
                        openAdvancedOptions(activity, null, options);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static void openAdvancedOptions(final Activity activity, final ResolveInfo resolveInfo) {
        openAdvancedOptions(activity, resolveInfo, null);
    }

    private static void openAdvancedOptions(final Activity activity, final ResolveInfo resolveInfo, AdvancedOptions options) {
        Intent editorPanel = new Intent(activity, AdvancedShortcutActivity.class);
        editorPanel.putExtra(AdvancedShortcutActivity.EXTRA_RESOLVE_INFO, resolveInfo);
        if (options != null) {
            editorPanel.putExtra(AdvancedShortcutActivity.EXTRA_ADVANCED_OPTIONS, options);
        }
        activity.startActivity(editorPanel);
    }

    private static void downloadShortcutApk(Activity activity, NetworkResponse response, Object item) {
        JSONObject data = null;
        try {
            data = new JSONObject(new String(response.data));
            Log.d(TAG, data.toString());
            if (data.getBoolean(KEY_BUILD_STATUS)) {
                String downloadLink = data.getJSONObject(KEY_APP_OBJ).getString(KEY_DOWNLOAD_URL);
                ApkDownloadHelper apkDownloadHelper = new ApkDownloadHelper(activity);

                if (activity == null) {
                    throw new NullPointerException("Activity variable doesn't exist");
                }
                apkDownloadHelper.startDownload(downloadLink);
            } else {
                Toast.makeText(activity, R.string.err_build_failed, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            if (item instanceof ResolveInfo) {
                throw new NullPointerException(e.getMessage() +
                        "\nSomething odd is happening for " +
                        ((ResolveInfo) item).activityInfo.packageName
                        + "\n" + data.toString());
            } else {
                throw new NullPointerException(e.getMessage() +
                        "\nSomething odd is happening"
                        + "\n" + data.toString());
            }
        }
    }

    public static void generateShortcut(final Activity activity, final ResolveInfo resolveInfo) {
        generateShortcut(activity, resolveInfo, new AdvancedOptions(activity));
    }

    public static void generateShortcut(final Activity activity, final ResolveInfo resolveInfo,
            final AdvancedOptions options) {
        generateShortcut(activity, resolveInfo, options, null);
    }

    @VisibleForTesting
    public static void generateShortcut(final Activity activity, final ResolveInfo resolveInfo,
                                        final AdvancedOptions options, final Callback callback) {
        if (!options.isReady()) {
            // Delay until we complete all web operations
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Delaying until web ops are complete");
                    generateShortcut(activity, resolveInfo, options, callback);
                }
            }, 200);
            return;
        }
        Toast.makeText(activity,
                R.string.msg_pls_wait,
                Toast.LENGTH_SHORT).show();

        showVisualAd(activity);


        ShortcutPostTask.generateShortcut(activity,
                resolveInfo,
                options,
                new ShortcutPostTask.Callback() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        if (callback != null) {
                            callback.onResponseComplete(new String(response.data));
                        } else {
                            downloadShortcutApk(activity, response, resolveInfo);
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(activity,
                                activity.getString(R.string.err_build_failed_reason, error.getMessage()),
                                Toast.LENGTH_SHORT).show();
                        Toast.makeText(activity,
                                new String(error.networkResponse.data),
                                Toast.LENGTH_LONG).show();
                        if (callback != null) {
                            callback.onResponseFailed(error);
                        }
                    }
                });
    }

    static InterstitialAd showVisualAd(Activity activity) {
        final InterstitialAd video =  new InterstitialAd(activity);
        video.setAdUnitId(activity.getString(R.string.interstitial_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        video.loadAd(adRequest);
        Log.d(TAG, "Loading ad");

        video.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "Ad loaded");
                // Show video as soon as possible
                video.show();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "Ad closed");
            }
        });

        return video;
    }

    static void showVideoAd(Activity activity) {
        final RewardedVideoAd ad = MobileAds.getRewardedVideoAdInstance(activity);
        ad.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                ad.show();
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {

            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }

            @Override
            public void onRewardedVideoCompleted() {
                
            }
        });
        ad.loadAd(activity.getString(R.string.reward_video_ad_unit_id), new AdRequest.Builder().build());
    }

    @VisibleForTesting
    public interface Callback {
        void onResponseComplete(String response);
        void onResponseFailed(VolleyError error);
    }
}
