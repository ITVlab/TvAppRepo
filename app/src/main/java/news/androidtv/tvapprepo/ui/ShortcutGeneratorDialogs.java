package news.androidtv.tvapprepo.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.intents.IntentUriGenerator;
import news.androidtv.tvapprepo.model.AdvancedOptions;
import news.androidtv.tvapprepo.utils.GenerateShortcutHelper;

/**
 * Created by Nick on 4/23/2017.
 */

public class ShortcutGeneratorDialogs {
    private static final String TAG = ShortcutGeneratorDialogs.class.getSimpleName();

    public static void initWebBookmarkDialog(final Activity activity) {
        new MaterialDialog.Builder(new ContextThemeWrapper(activity, R.style.dialog_theme))
                .title(R.string.generate_web_bookmark)
                .customView(R.layout.dialog_web_bookmark, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String tag = ((EditText) dialog.getCustomView().findViewById(R.id.tag)).getText().toString();
                        if (!tag.contains("http://") && !tag.contains("https://")) {
                            tag = "http://" + tag;
                            Log.w(TAG, "URL added without http...");
                        }
                        String label = tag.replaceAll("(http://)|(https://)", "");
                        Log.d(TAG, IntentUriGenerator.generateWebBookmark(tag));
                        AdvancedOptions options = new AdvancedOptions(activity)
                                .setIntentUri(IntentUriGenerator.generateWebBookmark(tag))
                                .setIconUrl("https://raw.githubusercontent.com/ITVlab/TvAppRepo/master/promo/graphics/icon.png") // TODO Replace icon url
                                .setCustomLabel(label);
                        GenerateShortcutHelper.begin(activity, label, options);
                    }
                })
                .positiveText(R.string.generate_shortcut)
                .show();
    }
}
