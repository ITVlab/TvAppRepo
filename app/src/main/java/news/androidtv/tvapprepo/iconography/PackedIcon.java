package news.androidtv.tvapprepo.iconography;

import android.graphics.drawable.Drawable;

/**
 * Created by Nick on 4/23/2017.
 *
 * A simple model abstraction that contains a few custom parameters.
 */
public class PackedIcon {
    public final Drawable icon;
    public final boolean isBanner; // For 'banner-packs'

    public PackedIcon(Drawable icon, boolean isBanner) {
        this.icon = icon;
        this.isBanner = isBanner;
    }
}
