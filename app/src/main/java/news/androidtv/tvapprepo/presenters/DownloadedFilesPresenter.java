package news.androidtv.tvapprepo.presenters;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

import news.androidtv.tvapprepo.R;

/**
 * A presenter which can be used to show downloaded APKs with an optional title along the bottom.
 *
 * @author Nick
 * @version 2016.09.04
 */
public class DownloadedFilesPresenter extends CardPresenter {
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
        cardView.setBackgroundColor(parent.getResources().getColor(R.color.primaryDark));
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        final File downloadedApk = (File) item;
        final ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setMainImage(contextThemeWrapper.getDrawable(R.drawable.download));
        cardView.setTitleText(downloadedApk.getName());
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        cardView.getMainImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}
