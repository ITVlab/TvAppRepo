package news.androidtv.tvapprepo.model;

import android.support.annotation.Keep;
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
            jsonObject.put("name", name);
            jsonObject.put("banner", banner);
            jsonObject.put("icon", icon);
            jsonObject.put("downloadUrl", downloadUrl);
            jsonObject.put("packageName", packageName);
            jsonObject.put("submissionDate", submitted);
            jsonObject.put("versionCode", versionCode);
            jsonObject.put("versionName", versionName);
            jsonObject.put("key", key);
            jsonObject.put("downloads", downloads);
            jsonObject.put("views", views);
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
                mApk.name = jsonObject.getString("name");
                mApk.banner = jsonObject.getString("banner");
                mApk.icon = jsonObject.getString("icon");
                String downloadUrl = jsonObject.getString("downloadUrl");
                Log.d(TAG, downloadUrl);
                mApk.downloadUrl = new FirebaseMap(downloadUrl).getMap();
                mApk.packageName = jsonObject.getString("packageName");
                mApk.submitted = jsonObject.getLong("submissionDate");
                mApk.versionCode = jsonObject.getInt("versionCode");
                mApk.versionName = jsonObject.getString("versionName");
                mApk.key = jsonObject.getString("key");
                mApk.downloads = jsonObject.getLong("downloads");
                mApk.views = jsonObject.getLong("views");
            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        public Apk build() {
            return mApk;
        }
    }
}