package news.androidtv.tvapprepo.iconography;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 4/23/2017.
 *
 * This class has methods that allow it to traverse icon packs installed on your device. These
 * icon packs are ultimately XML files that contain certain properties in the `appfilter.xml`.
 *
 * <item component="ComponentInfo{com.android.browser/com.android.browser.BrowserActivity}" drawable="com_android_browser_browseractivity" />
 *
 * We are adding more properties to support TVs. Each XML file can have a `banner` boolean attribute.
 * Alternatively, one can them placed in `appfilterbanners.xml`
 *
 * We need to remove the "ComponentInfo" from that attribute.
 * ComponentInfo{com.android.browser/com.android.browser.BrowserActivity}
 *               ^ (14)                                                ^ (-1)
 *
 *
 * For some additional references:
 *      * https://github.com/iamareebjamal/scratch_icon_pack_source/blob/master/app/src/main/res/xml/appfilter.xml
 *      * http://stackoverflow.com/questions/24937890/using-icon-packs-in-my-app
 *      * https://github.com/CyanogenMod/android_packages_apps_Trebuchet/
 *      * https://github.com/googlesamples/androidtv-sample-inputs/blob/master/app/src/main/res/raw/rich_tv_input_xmltv_feed.xml
 */

public class IconsTask {
    private static final boolean DEBUG = true;
    private static final String TAG = IconsTask.class.getSimpleName();

    private static final String INTENT_FILTER_ICON_PACKS = "org.adw.launcher.THEMES";

    public static void getIconsForComponentName(Activity activity, ComponentName filter, IconsReceivedCallback iconsReceivedCallback) {
        List<PackedIcon> iconList = new ArrayList<>();
        List<ResolveInfo> iconPacks =
                activity.getPackageManager().queryIntentActivities(new Intent(INTENT_FILTER_ICON_PACKS), PackageManager.GET_META_DATA);
        if (DEBUG) {
            Log.d(TAG, "Found " + iconPacks.size() + " icon packs");
        }
        for (ResolveInfo app : iconPacks) {
            if (DEBUG) {
                Log.d(TAG, "Scan icon pack " + app.activityInfo.packageName + ", " + app.activityInfo.applicationInfo.packageName);
            }
            iconList.addAll(scanIconPack(activity, "appfilter", app.activityInfo.applicationInfo.packageName, filter));
            iconList.addAll(scanIconPack(activity, "appfilterbanner", app.activityInfo.applicationInfo.packageName, filter));
        }
        if (DEBUG) {
            Log.d(TAG, "Sending callback with " + iconList.size() + " items");
        }
        iconsReceivedCallback.onIcons(iconList.toArray(new PackedIcon[iconList.size()]));
    }

    private static List<PackedIcon> scanIconPack(Activity activity, String filename, String packageName, ComponentName filter) {
        List<PackedIcon> iconList = new ArrayList<>();
        try {
            Resources iconResources = activity.getPackageManager().getResourcesForApplication(packageName);
            int xmlId = iconResources.getIdentifier("appfilter", "xml", packageName);
            if (DEBUG) {
                Log.d(TAG, "Read XML file " + xmlId);
            }
            XmlResourceParser resourceParser = iconResources.getXml(xmlId);
            try {
                while (resourceParser.next() != XmlPullParser.END_DOCUMENT) {
                    if (resourceParser.getName() != null && resourceParser.getName().equals("item")) {
                        // Get properties
                        boolean validApp = false;
                        String drawableName = "";
                        boolean banner = false;
                        for (int i = 0; i < resourceParser.getAttributeCount(); i++) {
                            String attr = resourceParser.getAttributeName(i);
                            String value = resourceParser.getAttributeValue(i);

                            if (attr.equals("component") &&
                                    value.substring(14, value.length() - 1).equals(filter.flattenToString())) {
                                validApp = true;
                            } else if (attr.equals("drawable")) {
                                drawableName = value;
                            } else if (attr.equals("banner")) {
                                banner = value.toLowerCase().equals("true");
                            }

                            if (i + 1 == resourceParser.getAttributeCount()) {
                                // Last element
                                if (validApp) {
                                    int drawableId = iconResources.getIdentifier(drawableName, "drawable", packageName);
                                    Drawable icon = iconResources.getDrawable(drawableId);
                                    iconList.add(new PackedIcon(icon, banner));
                                    if (DEBUG) {
                                        Log.d(TAG, "Adding an icon for " + drawableName + " from " + packageName + ": " + drawableId);
                                    }
                                }
                                validApp = false;
                                drawableName = "";
                                banner = false;
                            }
                        }
                    }
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return iconList;
    }

    public interface IconsReceivedCallback {
        void onIcons(PackedIcon[] icons);
    }
}
