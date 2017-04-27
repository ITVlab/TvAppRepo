package news.androidtv.tvapprepo.intents;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by Nick on 4/20/2017.
 *
 * Generates Intent URIs through static methods.
 */
public class IntentUriGenerator {
    public static String generateWebBookmark(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        return i.toUri(Intent.URI_INTENT_SCHEME);
    }

    public static String generateVideoPlayback(File myFile) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(fileExt(myFile.getAbsolutePath()).substring(1));
        newIntent.setDataAndType(Uri.fromFile(myFile),mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent.toUri(Intent.URI_INTENT_SCHEME);
    }

    public static String generateFileOpen(File myFile) {
        return generateVideoPlayback(myFile);
    }

    public static String generateActivityShortcut(ComponentName componentName) {
        Intent newIntent = new Intent();
        newIntent.setComponent(componentName);
        return newIntent.toUri(Intent.URI_INTENT_SCHEME);
    }

    private static String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }
}
