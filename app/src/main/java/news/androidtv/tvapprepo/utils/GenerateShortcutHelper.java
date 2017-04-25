package news.androidtv.tvapprepo.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.activities.AdvancedShortcutActivity;
import news.androidtv.tvapprepo.download.ApkDownloadHelper;
import news.androidtv.tvapprepo.iconography.IconsTask;
import news.androidtv.tvapprepo.iconography.PackedIcon;
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
            editorPanel.putExtra(AdvancedShortcutActivity.EXTRA_ADVANCED_OPTIONS, options.serialize());
        }
        activity.startActivity(editorPanel);
    }
    private static void openAdvancedOptions2(final Activity activity, final ResolveInfo resolveInfo, AdvancedOptions options) {
        final AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.dialog_theme))
                .setTitle(R.string.advanced_options)
                .setView(R.layout.dialog_app_shortcut_editor)
                .setNegativeButton(R.string.cancel, null)
                .create();

        if (options == null) {
            options = new AdvancedOptions(activity);
        }
        final AdvancedOptions[] finalOptions = {options};
        dialog.setButton(Dialog.BUTTON_POSITIVE,
                activity.getString(R.string.create_shortcut),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                View editor = dialog.getWindow().getDecorView();
                boolean isGame = ((Switch) editor.findViewById(R.id.switch_isgame)).isChecked();
                finalOptions[0].setIsGame(isGame);
                generateShortcut(activity, resolveInfo, finalOptions[0]);
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View layout = dialog.getWindow().getDecorView();
                layout.findViewById(R.id.change_banner).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Lookup for the app icon
                        if (resolveInfo != null) {
                            IconsTask.getIconsForComponentName(activity,
                                    new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name),
                                    new IconsTask.IconsReceivedCallback() {
                                @Override
                                public void onIcons(PackedIcon[] icons) {
                                    Log.d(TAG, icons.length + "<<<");
                                    // Show dialog of all icons for the user to select (or let them do their own)
                                    final AlertDialog iconDialog = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.dialog_theme))
                                            .setTitle("Select Custom Iconography")
                                            .setView(R.layout.dialog_custom_iconography)
                                            .create();
                                    iconDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String bannerUrl =
                                                    ((EditText) iconDialog.getWindow().getDecorView().findViewById(R.id.edit_banner)).getText().toString();
                                            finalOptions[0].setBannerUrl(bannerUrl);
                                        }
                                    });
                                    iconDialog.show();
                                    LinearLayout iconDialogLayout = (LinearLayout) iconDialog.getWindow().getDecorView().findViewById(R.id.icon_list);
                                    for (final PackedIcon icon : icons) {
                                        ImageButton imageButton = new ImageButton(activity);
                                        imageButton.setImageDrawable(icon.icon);
                                        imageButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (icon.isBanner) {
                                                    finalOptions[0].setBannerBitmap(icon.getBitmap());
                                                } else {
                                                    finalOptions[0].setIconBitmap(icon.getBitmap());
                                                }
                                                Log.d(TAG, finalOptions[0].toString());
                                                iconDialog.dismiss();
                                            }
                                        });
                                        iconDialogLayout.addView(imageButton);
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(activity, "Cannot set banner of non-app yet", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
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
        if (!options.isReady()) {
            // Delay until we complete all web operations
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    generateShortcut(activity, resolveInfo, options);
                }
            }, 200);
            return;
        }
        Toast.makeText(activity,
                R.string.msg_pls_wait,
                Toast.LENGTH_SHORT).show();

        final InterstitialAd video = showVisualAd(activity);

        ShortcutPostTask.generateShortcut(activity,
                resolveInfo,
                options,
                new ShortcutPostTask.Callback() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        downloadShortcutApk(activity, response, resolveInfo);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Toast.makeText(activity,
                                activity.getString(R.string.err_build_failed_reason, error.getMessage()),
                                Toast.LENGTH_SHORT).show();
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
}
