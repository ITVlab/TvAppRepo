package news.androidtv.tvapprepo.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import news.androidtv.tvapprepo.utils.ShortcutPostTask;

/**
 * Created by Nick on 3/20/2017. A model for storing advanced options in generating shortcuts.
 */
public class AdvancedOptions implements Parcelable {
    private volatile int mReady = 0;
    private String mCategory = "";
    private String mIconUrl = "";
    private String mBannerUrl = "";
    private String mIntentUri = "";
    private String mCustomLabel = "";
    private boolean mUnique = false;
    private byte[] mIconData = null;
    private byte[] mBannerData = null;
    private Context mContext = null;

    public AdvancedOptions(Context context) {
        mContext = context;
    }

    public AdvancedOptions setBannerUrl(String bannerUrl) {
        if (bannerUrl == null || bannerUrl.isEmpty()) {
            // Exit early.
            return this;
        }
        mReady++;
        mBannerUrl = bannerUrl;
        // Download from Glide.
        downloadBanner(mContext, bannerUrl, new GlideCallback() {
            @Override
            public void onDone(byte[] binaryData) {
                mBannerData = binaryData;
                mReady--;
            }
        });
        return this;
    }

    public AdvancedOptions setIsGame(boolean isGame) {
        mCategory = (isGame) ? ShortcutPostTask.CATEGORY_GAMES : ShortcutPostTask.CATEGORY_APPS;
        return this;
    }

    public AdvancedOptions setIntentUri(String intentUri) {
        if (intentUri.length() < 20 || intentUri.length() > 300) {
            throw new StringLengthException(intentUri);
        }
        mIntentUri = intentUri;
        return this;
    }

    public AdvancedOptions setUniquePackageName(boolean isUnique) {
        mUnique = isUnique;
        return this;
    }

    public AdvancedOptions setCustomLabel(String label) {
        mCustomLabel = label;
        return this;
    }

    public AdvancedOptions setIconUrl(String iconUrl) {
        if (iconUrl == null || iconUrl.isEmpty()) {
            // Exit early.
            return this;
        }
        mReady++;
        mIconUrl = iconUrl;
        // Download from Glide.
        downloadBanner(mContext, iconUrl, new GlideCallback() {
            @Override
            public void onDone(byte[] binaryData) {
                mIconData = binaryData;
                mReady--;
            }
        });
        return this;
    }

    public AdvancedOptions setBannerBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] results = stream.toByteArray();
        mBannerData = results;
        return this;
    }

    public AdvancedOptions setIconBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] results = stream.toByteArray();
        mIconData = results;
        return this;
    }

    public boolean isReady() {
        return mReady == 0;
    }

    public byte[] getIcon() {
        return mIconData;
    }

    public byte[] getBanner() {
        return mBannerData;
    }

    public String getBannerUrl() {
        return mBannerUrl;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getIntentUri() {
        return mIntentUri;
    }

    public String getCustomLabel() {
        return mCustomLabel;
    }

    public boolean isUnique() {
        return mUnique;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    private void downloadBanner(final Context context, final String url, final GlideCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = Glide.with(context).load(url).asBitmap().into(320, 180).get();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    final byte[] results = stream.toByteArray();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onDone(results);
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public String toString() {
        return "Name=" + mCustomLabel + ", category=" + mCategory + ", iconUrl=" + mIconUrl +
                ", bannerUrl=" + mBannerUrl + ", iconData=" + (mIconData != null) +
                ", bannerData=" + (mBannerData != null);
    }

    public String serialize() {
        JSONObject object = new JSONObject();
        try {
            object.put("customLabel", mCustomLabel);
            object.put("category", mCategory);
            object.put("iconUrl", mIconUrl);
            object.put("bannerUrl", mBannerUrl);
            object.put("isUnique", mUnique);
            object.put("intentUri", mIntentUri);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static AdvancedOptions deserialize(Activity activity, String serialization) {
        AdvancedOptions options = new AdvancedOptions(activity);
        try {
            JSONObject object = new JSONObject(serialization);
            options.setCustomLabel(object.optString("customLabel"));
            options.mCategory = object.optString("category");
            options.setIconUrl(object.optString("iconUrl"));
            options.setBannerUrl(object.optString("bannerUrl"));
            options.setUniquePackageName(object.optBoolean("isUnique"));
            options.setIntentUri(object.optString("intentUri"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return options;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCustomLabel);
        dest.writeString(mCategory);
        dest.writeString(mIconUrl);
        dest.writeString(mBannerUrl);
        dest.writeByte((byte) (mUnique ? 1 : 0));
        dest.writeString(mIntentUri);
        if (mIconData == null) {
            mIconData = new byte[0];
        }
        if (mBannerData == null) {
            mBannerData = new byte[0];
        }
        dest.writeInt(mIconData.length);
        dest.writeByteArray(mIconData);
        dest.writeInt(mBannerData.length);
        dest.writeByteArray(mBannerData);
    }

    public static final Parcelable.Creator<AdvancedOptions> CREATOR
            = new Parcelable.Creator<AdvancedOptions>() {
        public AdvancedOptions createFromParcel(Parcel in) {
            return new AdvancedOptions(in);
        }

        public AdvancedOptions[] newArray(int size) {
            return new AdvancedOptions[size];
        }
    };

    private AdvancedOptions(Parcel in) {
        mCustomLabel = in.readString();
        mCategory = in.readString();
        mIconUrl = in.readString();
        mBannerUrl = in.readString();
        mUnique = in.readByte() == 1;
        mIntentUri = in.readString();
        mIconData = new byte[in.readInt()];
        in.readByteArray(mIconData);
        mBannerData = new byte[in.readInt()];
        in.readByteArray(mBannerData);
    }

    private interface GlideCallback {
        void onDone(byte[] binaryData);
    }

    public class StringLengthException extends RuntimeException {
        public StringLengthException(String string) {
            super("Intent URI length must be between 20 and 300 characters: " + string);
        }
    }
}
