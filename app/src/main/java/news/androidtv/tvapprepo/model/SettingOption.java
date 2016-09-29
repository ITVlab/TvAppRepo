package news.androidtv.tvapprepo.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.function.Consumer;

/**
 * A generic class for items in the {@link news.androidtv.tvapprepo.fragments.MainFragment} which
 * do a certain action on being selected.
 */
public class SettingOption {
    private Drawable mIcon;
    private String mTitle;
    private OnClickListener mOnClickListener;

    public SettingOption(Drawable drawable, String text, OnClickListener onClickListener) {
        mIcon = drawable;
        mTitle = text;
        mOnClickListener = onClickListener;
    }

    public Drawable getDrawable() {
        return mIcon;
    }

    public String getText() {
        return mTitle;
    }

    public OnClickListener getClickListener() {
        return mOnClickListener;
    }

    public interface OnClickListener {
        void onClick();
    }
}
