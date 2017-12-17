package news.androidtv.tvapprepo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.io.File;
import java.net.URISyntaxException;

import news.androidtv.tvapprepo.intents.IntentUriGenerator;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class IntentsInstrumentationTest extends ApplicationTestCase<Application> {
    public static final String TAG = IntentsInstrumentationTest.class.getSimpleName();

    public IntentsInstrumentationTest() {
        super(Application.class);
    }

    public void testWebBookmarks() {
        final String expected = "intent://google.com#Intent;scheme=http;end";
        String actual = IntentUriGenerator.generateWebBookmark("http://google.com");
        Log.d(TAG, actual);
        assertEquals(expected, actual);
    }

    public void testActivityShortcut() {
        final String expected = "intent:#Intent;component=news.androidtv.tvapprepo/.activities.SettingsActivity;end";
        String actual = IntentUriGenerator.generateActivityShortcut(new ComponentName("news.androidtv.tvapprepo", ".activities.SettingsActivity"));
        Log.d(TAG, actual);
        assertEquals(expected, actual);
    }

    public void testFileOpening() {
        // Note: This can be flaky if your device doesn't have this file. Future versions of this
        // test should create and delete a temporary file.
        final String expected = "intent:///storage/emulated/0/Download/com.felkertech.n.cumulustv.test.apk#Intent;scheme=file;launchFlags=0x10000000;end";
        String actual = IntentUriGenerator.generateVideoPlayback(new File("/storage/emulated/0/Download/com.felkertech.n.cumulustv.test.apk"));
        Log.d(TAG, actual);
        assertEquals(expected, actual);
    }

    public void testOpenGoogle() throws URISyntaxException {
        String string = "intent:#Intent;component=news.androidtv.tvapprepo/.activities.SettingsActivity;end";
        getContext().startActivity(Intent.parseUri(string, Intent.URI_INTENT_SCHEME));
    }
}