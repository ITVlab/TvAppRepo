package news.androidtv.tvapprepo.model;

import com.google.firebase.database.ThrowOnExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an app file.
 *
 * @author Nick
 * @version 2016.09.08
 */
public class Apk {
    private String banner;
    private String downloadUrl;
    private String icon;
    private boolean isLeanback;
    private String leanbackShortcut;
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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getIcon() {
        return icon;
    }

    public boolean hasLeanback() {
        return isLeanback;
    }

    public String getLeanbackShortcut() {
        return leanbackShortcut;
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

    public void setIsLeanback(Boolean leanback) {
        isLeanback = leanback;
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
            jsonObject.put("isLeanback", isLeanback);
            jsonObject.put("downloadUrl", downloadUrl);
            jsonObject.put("leanbackShortcut", leanbackShortcut);
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
            try {
                JSONObject jsonObject = new JSONObject(serial);
                mApk = new Apk();
                mApk.name = jsonObject.getString("name");
                mApk.banner = jsonObject.getString("banner");
                mApk.icon = jsonObject.getString("icon");
                mApk.isLeanback = jsonObject.getBoolean("isLeanback");
                mApk.downloadUrl = jsonObject.getString("downloadUrl");
                if (jsonObject.has("leanbackShortcut")) {
                    mApk.leanbackShortcut = jsonObject.getString("leanbackShortcut");
                }
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
