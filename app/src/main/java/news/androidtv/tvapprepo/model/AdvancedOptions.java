package news.androidtv.tvapprepo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import news.androidtv.tvapprepo.utils.ShortcutPostTask;

/**
 * Created by Nick on 3/20/2017. A model for storing advanced options in generating shortcuts.
 */
public class AdvancedOptions {
    private volatile int mReady = 0;
    private String mCategory = "";
    private String mBannerUrl = "";
    private byte[] mBannerData;
    private Context mContext;

    public AdvancedOptions(Context context) {
        mContext = context;
    }

    public AdvancedOptions setBannerUrl(String bannerUrl) {
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

    public boolean isReady() {
        return mReady == 0;
    }

    public byte[] getBanner() {
        return mBannerData;
    }

    public String getCategory() {
        return mCategory;
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

    private interface GlideCallback {
        void onDone(byte[] binaryData);
    }
}
