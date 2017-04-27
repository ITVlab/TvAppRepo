package news.androidtv.tvapprepo.iconography;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
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

    public Bitmap getBitmap() {
        if (icon instanceof BitmapDrawable) {
            return ((BitmapDrawable) icon).getBitmap();
        }

        int width = icon.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = icon.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);

        return bitmap;
    }
}
