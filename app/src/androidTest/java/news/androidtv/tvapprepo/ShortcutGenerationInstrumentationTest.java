package news.androidtv.tvapprepo;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import news.androidtv.tvapprepo.activities.MainActivity;
import news.androidtv.tvapprepo.intents.IntentUriGenerator;
import news.androidtv.tvapprepo.model.AdvancedOptions;
import news.androidtv.tvapprepo.utils.GenerateShortcutHelper;

/**
 * A series of tests relating to generating shortcut apks.
 * Right now all tests must be verified manually.
 */
@RunWith(AndroidJUnit4.class)
public class ShortcutGenerationInstrumentationTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private static final String TAG = ShortcutGenerationInstrumentationTest.class.getSimpleName();
    private static final int TIMEOUT_LENGTH = 90;
    private static final TimeUnit TIMEOUT_UNITS = TimeUnit.SECONDS;
    private static final ResolveInfo SAMPLE_APK;
    private static final String SAMPLE_BANNER = "http://via.placeholder.com/320x180";

    static {
        SAMPLE_APK = new ResolveInfo();
        SAMPLE_APK.activityInfo = new ActivityInfo();
        SAMPLE_APK.activityInfo.applicationInfo = new ApplicationInfo();
        SAMPLE_APK.activityInfo.applicationInfo.packageName = "com.test";
        SAMPLE_APK.activityInfo.nonLocalizedLabel = "Test Activity Label";
        SAMPLE_APK.activityInfo.name = "Test Activity Name";
        SAMPLE_APK.icon = R.drawable.ic_launcher;
    }

    private void doOnMainThread(Runnable r) {
        mActivityRule.getActivity().runOnUiThread(r);
    }

    /**
     * Generates an APK shortcut from an on-device APK without advanced options.
     */
    @Test
    public void testSimpleApkGeneration() throws InterruptedException {
        // Set up a CountdownLatch to accommodate for network events
        final CountDownLatch latch = new CountDownLatch(1);

        doOnMainThread(new Runnable() {
            @Override
            public void run() {
                GenerateShortcutHelper.generateShortcut(mActivityRule.getActivity(), SAMPLE_APK,
                        new AdvancedOptions(mActivityRule.getActivity()), new GenerateShortcutHelper.Callback() {
                            @Override
                            public void onResponseComplete(String response) {
                                latch.countDown();
                            }

                            @Override
                            public void onResponseFailed(VolleyError error) {
                                throw new RuntimeException(error.getMessage());
                            }
                        });
            }
        });

        Assert.assertTrue(latch.await(TIMEOUT_LENGTH, TIMEOUT_UNITS));
    }

    @Test
    public void testCustomBannerApkGeneration() throws InterruptedException {
        // Set up a CountdownLatch to accommodate for network events
        final CountDownLatch latch = new CountDownLatch(1);
        final AdvancedOptions advancedOptions = new AdvancedOptions(mActivityRule.getActivity());
        advancedOptions.setBannerUrl(SAMPLE_BANNER);

        doOnMainThread(new Runnable() {
            @Override
            public void run() {
                GenerateShortcutHelper.generateShortcut(mActivityRule.getActivity(), SAMPLE_APK,
                        advancedOptions, new GenerateShortcutHelper.Callback() {
                            @Override
                            public void onResponseComplete(String response) {
                                Log.d(TAG, response);
                                latch.countDown();
                            }

                            @Override
                            public void onResponseFailed(VolleyError error) {
                                throw new RuntimeException(error.getMessage());
                            }
                        });
            }
        });

        Assert.assertTrue(latch.await(TIMEOUT_LENGTH, TIMEOUT_UNITS));
    }

    @Test
    public void testGameApkGeneration() throws InterruptedException {
        // Set up a CountdownLatch to accommodate for network events
        final CountDownLatch latch = new CountDownLatch(1);
        final AdvancedOptions advancedOptions = new AdvancedOptions(mActivityRule.getActivity());
        advancedOptions.setIsGame(true);

        doOnMainThread(new Runnable() {
            @Override
            public void run() {
                GenerateShortcutHelper.generateShortcut(mActivityRule.getActivity(), SAMPLE_APK,
                        advancedOptions, new GenerateShortcutHelper.Callback() {
                            @Override
                            public void onResponseComplete(String response) {
                                latch.countDown();
                            }

                            @Override
                            public void onResponseFailed(VolleyError error) {
                                throw new RuntimeException(error.getMessage());
                            }
                        });
            }
        });

        Assert.assertTrue(latch.await(TIMEOUT_LENGTH, TIMEOUT_UNITS));
    }

    @Test
    public void testBannerBitmapApk() throws InterruptedException, ExecutionException {
        // Set up a CountdownLatch to accommodate for network events
        final CountDownLatch latch = new CountDownLatch(1);
        final AdvancedOptions advancedOptions = new AdvancedOptions(mActivityRule.getActivity());
        Bitmap bitmap = Glide.with(mActivityRule.getActivity()).load(SAMPLE_BANNER).asBitmap()
                .into(320, 180).get();
        advancedOptions.setBannerBitmap(bitmap);

        doOnMainThread(new Runnable() {
            @Override
            public void run() {
                GenerateShortcutHelper.generateShortcut(mActivityRule.getActivity(), SAMPLE_APK,
                        advancedOptions, new GenerateShortcutHelper.Callback() {
                            @Override
                            public void onResponseComplete(String response) {
                                latch.countDown();
                            }

                            @Override
                            public void onResponseFailed(VolleyError error) {
                                throw new RuntimeException(error.getMessage());
                            }
                        });
            }
        });

        Assert.assertTrue(latch.await(TIMEOUT_LENGTH, TIMEOUT_UNITS));
    }

    @Test
    public void testWebShortcut() throws InterruptedException, ExecutionException {
        // Set up a CountdownLatch to accommodate for network events
        final CountDownLatch latch = new CountDownLatch(1);
        String url = "http://example.com";
        String label = "Example.Com";
        final AdvancedOptions advancedOptions = new AdvancedOptions(mActivityRule.getActivity())
                .setIntentUri(IntentUriGenerator.generateWebBookmark(url))
                .setIconUrl("https://raw.githubusercontent.com/ITVlab/TvAppRepo/master/promo/graphics/icon.png")
                .setCustomLabel(label);

        doOnMainThread(new Runnable() {
            @Override
            public void run() {
                GenerateShortcutHelper.generateShortcut(mActivityRule.getActivity(), SAMPLE_APK,
                        advancedOptions, new GenerateShortcutHelper.Callback() {
                            @Override
                            public void onResponseComplete(String response) {
                                latch.countDown();
                            }

                            @Override
                            public void onResponseFailed(VolleyError error) {
                                throw new RuntimeException(error.getMessage());
                            }
                        });
            }
        });

        Assert.assertTrue(latch.await(TIMEOUT_LENGTH, TIMEOUT_UNITS));
    }
}
