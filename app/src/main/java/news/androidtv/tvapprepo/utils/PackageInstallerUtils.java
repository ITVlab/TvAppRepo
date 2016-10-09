package news.androidtv.tvapprepo.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;

import news.androidtv.tvapprepo.model.Apk;

/**
 * CA handful of utility methods related to the {@link PackageInstaller}.
 */
public class PackageInstallerUtils {
    /**
     * Compares a new APK to the current version of the app installed on the device, if available.
     *
     * @param apk The apk you want version comparison data for
     * @return true if the installed version is lower.
     */
    public static boolean isUpdateAvailable(Activity activity, Apk apk) {
        PackageManager packageManager = activity.getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(apk.getPackageName(), 0);
            if (info.versionCode < apk.getVersionCode()) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    // http://stackoverflow.com/questions/6813322/install-uninstall-apks-programmatically-packagemanager-vs-intents
    public static void uninstallApp(Activity activity, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.fromParts("package",
                activity.getPackageManager().getPackageArchiveInfo(packageName, 0).packageName,
                null));
        activity.startActivity(intent);
    }
}
