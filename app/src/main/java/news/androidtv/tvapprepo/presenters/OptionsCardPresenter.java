package news.androidtv.tvapprepo.presenters;

import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.widget.ImageView;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.model.SettingOption;

/**
 * A presenter which can be used to show options with an optional title along the bottom.
 *
 * @author Nick
 * @version 2016.09.04
 */
public class OptionsCardPresenter extends CardPresenter {
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
        final SettingOption option = (SettingOption) item;
        final ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setMainImage(option.getDrawable());
        cardView.setTitleText(option.getText());
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        cardView.getMainImageView().setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}
