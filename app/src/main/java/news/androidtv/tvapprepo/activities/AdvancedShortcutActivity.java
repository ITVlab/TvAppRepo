package news.androidtv.tvapprepo.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.iconography.IconsTask;
import news.androidtv.tvapprepo.iconography.PackedIcon;
import news.androidtv.tvapprepo.model.AdvancedOptions;
import news.androidtv.tvapprepo.utils.GenerateShortcutHelper;

/**
 * Created by Nick on 4/24/2017.
 *
 * Dialogs are not a very good user interface if they start nesting. Instead, we will use a pull-out
 * panel that comes in from the right and shows a variety of settings. This will scale a lot better
 * as we can have more real-estate.
 */
public class AdvancedShortcutActivity extends Activity {
    private static final String TAG = AdvancedShortcutActivity.class.getSimpleName();

    public static final String EXTRA_RESOLVE_INFO = "resolveInfo";
    public static final String EXTRA_ADVANCED_OPTIONS = "advancedOptions";

    private AdvancedOptions advancedOptions;
    private ResolveInfo resolveInfo;
    private IconsTask.IconsReceivedCallback callback = new IconsTask.IconsReceivedCallback() {
        @Override
        public void onIcons(PackedIcon[] icons) {
            Log.d(TAG, icons.length + "<<<");
            // Show all icons for the user to select (or let them do their own)
            LinearLayout iconDialogLayout = (LinearLayout) findViewById(R.id.icon_list);
            iconDialogLayout.removeAllViews();
            for (final PackedIcon icon : icons) {
                ImageButton imageButton = new ImageButton(AdvancedShortcutActivity.this);
                imageButton.setImageDrawable(icon.icon);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (icon.isBanner) {
                            advancedOptions.setBannerBitmap(icon.getBitmap());
                        } else {
                            advancedOptions.setIconBitmap(icon.getBitmap());
                        }
                        Log.d(TAG, advancedOptions.toString());
                    }
                });
                iconDialogLayout.addView(imageButton);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        setContentView(R.layout.activity_advanced);

        if (getIntent().hasExtra(EXTRA_RESOLVE_INFO)) {
            resolveInfo = getIntent().getParcelableExtra(EXTRA_RESOLVE_INFO);
        }
        if (getIntent().hasExtra(EXTRA_ADVANCED_OPTIONS)) {
            advancedOptions = AdvancedOptions.deserialize(this,
                    getIntent().getStringExtra(EXTRA_ADVANCED_OPTIONS));
        }

        if (advancedOptions == null) {
            advancedOptions = new AdvancedOptions(this);
        }

        loadCustomIconography();

        findViewById(R.id.generate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
                finish();
            }
        });

        // Turn into side-panel
        // Sets the size and position of dialog activity.
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        layoutParams.width = getResources().getDimensionPixelSize(R.dimen.side_panel_width);
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(layoutParams);
    }

    private void publish() {
        boolean isGame = ((Switch) findViewById(R.id.switch_isgame)).isChecked();
        String bannerUrl =
                ((EditText) findViewById(R.id.edit_banner)).getText().toString();
        if (!bannerUrl.isEmpty()) {
            advancedOptions.setBannerUrl(bannerUrl);
        }
        advancedOptions.setIsGame(isGame);
        GenerateShortcutHelper.generateShortcut(this, resolveInfo, advancedOptions);
    }

    private void loadCustomIconography() {
        if (resolveInfo != null) {
            IconsTask.getIconsForComponentName(this,
                    new ComponentName(resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name), callback);

        } else {
            Toast.makeText(this, "Cannot set banner of non-app yet", Toast.LENGTH_SHORT).show();
        }
    }
}
