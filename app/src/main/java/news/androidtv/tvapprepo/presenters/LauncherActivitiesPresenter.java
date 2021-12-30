package news.androidtv.tvapprepo.presenters;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.palette.graphics.Palette;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.widget.ImageView;

import news.androidtv.tvapprepo.R;

/**
 * A presenter which can be used to show downloaded APKs with an optional title along the bottom.
 *
 * @author Nick
 * @version 2017.01.12
 */
public class LauncherActivitiesPresenter extends CardPresenter {
    private static final String TAG = LauncherActivitiesPresenter.class.getSimpleName();
    private ContextThemeWrapper contextThemeWrapper;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        if (contextThemeWrapper == null) {
            contextThemeWrapper = new ContextThemeWrapper(parent.getContext(),
                    R.style.OptionsImageCardViewStyle);
        }
        ImageCardView cardView = new ImageCardView(contextThemeWrapper);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        final ResolveInfo app = (ResolveInfo) item;
        final ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setMainImage(app.activityInfo.loadIcon(contextThemeWrapper.getPackageManager()));
        Log.d(TAG, app.toString());
        Log.d(TAG, app.activityInfo.name);
        Log.d(TAG, app.activityInfo.applicationInfo.loadLabel(contextThemeWrapper.getPackageManager()) + "");
        cardView.setTitleText(app.activityInfo.applicationInfo.loadLabel(contextThemeWrapper.getPackageManager()));
        cardView.setContentText(app.activityInfo.name);
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        cardView.getMainImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
        BitmapDrawable bitmapDrawable;
        try {
            bitmapDrawable = (BitmapDrawable) app.activityInfo
                    .loadIcon(contextThemeWrapper.getPackageManager());
        } catch (ClassCastException e) {
            // Probably some other sort of drawable (?)
            // Use a default.
            bitmapDrawable = (BitmapDrawable) contextThemeWrapper.getDrawable(R.drawable.banner);
        }
        Palette.generateAsync(bitmapDrawable.getBitmap(), new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                // Here's your generated palette
                if (palette.getDarkVibrantSwatch() != null) {
                    cardView.findViewById(R.id.info_field).setBackgroundColor(
                            palette.getDarkVibrantSwatch().getRgb());
                }
            }
        });
    }
}
