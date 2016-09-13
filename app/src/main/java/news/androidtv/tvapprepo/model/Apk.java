package news.androidtv.tvapprepo.model;

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

    public static class Builder {

        public Builder() {

        }

        public Builder(String serial) {

        }

        public Apk build() {
            return null;
        }
    }
}
