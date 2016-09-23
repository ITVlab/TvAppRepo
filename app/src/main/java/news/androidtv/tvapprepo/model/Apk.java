package news.androidtv.tvapprepo.model;

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
    private long submissionDate;
    private int versionCode;
    private String versionName;

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

    public boolean isLeanback() {
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
        return submissionDate;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
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
            jsonObject.put("submissionDate", submissionDate);
            jsonObject.put("versionCode", versionCode);
            jsonObject.put("versionName", versionName);
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
                mApk.leanbackShortcut = jsonObject.getString("leanbackShortcut");
                mApk.packageName = jsonObject.getString("packageName");
                mApk.submissionDate = jsonObject.getLong("submissionDate");
                mApk.versionCode = jsonObject.getInt("versionCode");
                mApk.versionName = jsonObject.getString("versionName");
            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        public Apk build() {
            return mApk;
        }
    }
}
