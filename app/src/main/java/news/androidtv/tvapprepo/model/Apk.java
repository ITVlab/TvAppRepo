package news.androidtv.tvapprepo.model;

import androidx.annotation.Keep;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an app file.
 *
 * @author Nick
 * @version 2016.09.08
 */
@Keep
public class Apk {
    private static final String TAG = Apk.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final String KEY_NAME = "name";
    private static final String KEY_BANNER = "banner";
    private static final String KEY_ICON = "icon";
    private static final String KEY_DOWNLOAD_URL = "downloadUrl";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_SUBMISSION_DATE = "submissionDate";
    private static final String KEY_VERSION_CODE = "versionCode";
    private static final String KEY_VERSION_NAME = "versionName";
    private static final String KEY = "key";
    private static final String KEY_DOWNLOADS = "downloads";
    private static final String KEY_VIEWS = "views";

    private String banner;
    private Map<String, String> downloadUrl;
//    private FirebaseMap downloadMap;
    private String icon;
    private String name;
    private String packageName;
    private long submitted;
    private int versionCode;
    private String versionName;
    private String key;
    private long downloads;
    private long views;

    private Apk() {
    }

    public String getBanner() {
        return banner;
    }

    public Map<String, String> getDownloadUrl() {
        return downloadUrl;
    }

    public HashMap<String, String> getDownloadMap() {
        /*if (downloadMap == null) {
            downloadMap = new FirebaseMap(downloadUrl.toString());
        }
        return downloadMap.getMap();*/
        return (HashMap<String, String>) downloadUrl;
    }

    public int getDownloadCount() {
        return getDownloadMap().size();
    }

    public String getDefaultDownloadUrl() {
        return getDownloadMap().get(getDownloadMap().keySet().toArray()[0]);
    }

    public CharSequence[] getDownloadTitleArray() {
        return getDownloadMap().keySet().toArray(new String[getDownloadMap().keySet().size()]);
    }

    public String[] getDownloadUrlArray() {
        return getDownloadMap().values().toArray(new String[getDownloadMap().keySet().size()]);
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getSubmissionDate() {
        return submitted;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public long getDownloads() {
        return downloads;
    }

    public long getViews() {
        return views;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setSubmitted(long submitted) {
        this.submitted = submitted;
    }

    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_NAME, name);
            jsonObject.put(KEY_BANNER, banner);
            jsonObject.put(KEY_ICON, icon);
            jsonObject.put(KEY_DOWNLOAD_URL, downloadUrl);
            jsonObject.put(KEY_PACKAGE_NAME, packageName);
            jsonObject.put(KEY_SUBMISSION_DATE, submitted);
            jsonObject.put(KEY_VERSION_CODE, versionCode);
            jsonObject.put(KEY_VERSION_NAME, versionName);
            jsonObject.put(KEY, key);
            jsonObject.put(KEY_DOWNLOADS, downloads);
            jsonObject.put(KEY_VIEWS, views);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static class Builder {
        Apk mApk;

        public Builder() {
            mApk = new Apk();
        }

        public Builder(String serial) {
            Log.d(TAG, serial);
            try {
                JSONObject jsonObject = new JSONObject(serial);
                Log.d(TAG, jsonObject.toString());
                mApk = new Apk();
                mApk.name = jsonObject.getString(KEY_NAME);
                mApk.banner = jsonObject.getString(KEY_BANNER);
                mApk.icon = jsonObject.getString(KEY_ICON);
                String downloadUrl = jsonObject.getString(KEY_DOWNLOAD_URL);
                Log.d(TAG, downloadUrl);
                mApk.downloadUrl = new FirebaseMap(downloadUrl).getMap();
                mApk.packageName = jsonObject.getString(KEY_PACKAGE_NAME);
                mApk.submitted = jsonObject.getLong(KEY_SUBMISSION_DATE);
                mApk.versionCode = jsonObject.getInt(KEY_VERSION_CODE);
                mApk.versionName = jsonObject.getString(KEY_VERSION_NAME);
                mApk.key = jsonObject.getString(KEY);
                mApk.downloads = jsonObject.getLong(KEY_DOWNLOADS);
                mApk.views = jsonObject.getLong(KEY_VIEWS);
            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        public Apk build() {
            return mApk;
        }
    }
}
