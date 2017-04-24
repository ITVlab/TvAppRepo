package news.androidtv.tvapprepo.iconography;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

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
 */

public class IconsTask {
    private static final String INTENT_FILTER_ICON_PACKS = "org.adw.launcher.THEMES";

    public static void getIconsForComponentName(Activity activity, IconsReceivedCallback iconsReceivedCallback, ComponentName filter) {
        List<PackedIcon> iconList = new ArrayList<>();
        List<ResolveInfo> iconPacks =
                activity.getPackageManager().queryIntentActivities(new Intent(INTENT_FILTER_ICON_PACKS), PackageManager.GET_META_DATA);
        for (ResolveInfo app : iconPacks) {
            int xmlId = activity.getResources().getIdentifier("appfilter", "xml", app.activityInfo.applicationInfo.packageName);
            XmlResourceParser resourceParser = activity.getResources().getXml(xmlId);
            try {
                while (resourceParser.next() != XmlPullParser.END_DOCUMENT) {
                    if (resourceParser.getName().equals("item")) {
                        // Get properties
                        boolean validApp = false;
                        for (int i = 0; i < resourceParser.getAttributeCount(); i++) {
                            String attr = resourceParser.getAttributeName(i);
                            String value = resourceParser.getAttributeValue(i);

                            if (attr.equals("component") && value.substring(14, value.length() - 1).equals(filter.flattenToString())) {
                                validApp = true;
                            }
                        }
                    }
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface IconsReceivedCallback {
        void onIcons(PackedIcon[] icons);
    }
}
