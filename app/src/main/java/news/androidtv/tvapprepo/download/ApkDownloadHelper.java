package news.androidtv.tvapprepo.download;

import android.app.Activity;

import java.io.File;

import news.androidtv.tvapprepo.PrivateUtils;
import tv.puppetmaster.tinydl.PackageInstaller;

/**
 * Created by Nick on 9/23/2016.
 */
public class ApkDownloadHelper extends AbstractDownloadHelper {
    private PackageInstaller mPackageInstaller;

    public ApkDownloadHelper(Activity activity) {
        initialize(activity, null);
    }

    public ApkDownloadHelper initialize(Activity activity, PackageInstaller.DownloadListener listener) {
        super.initialize(activity);
        mPackageInstaller = PackageInstaller.initialize(activity);
        if (listener != null) {
            mPackageInstaller.addListener(listener);
        }
        return this;
    }

    @Override
    public void destroy() {
        super.destroy();
        mPackageInstaller.destroy();
    }

    @Override
    public AbstractDownloadHelper startDownload(String url) {
        mPackageInstaller.wget(url);
        return super.startDownload(url);
    }

    @Override
    public AbstractDownloadHelper startDownload(String url, String adType) {
        mPackageInstaller.wget(url);
        return super.startDownload(url, adType);
    }

    public PackageInstaller getPackageInstaller() {
        return mPackageInstaller;
    }

    @Override
    public String getAppId() {
        return PrivateUtils.COLOR_TV_KEY;
    }

    // Wrapper methods
    public void install(File file) {
        mPackageInstaller.install(file);
    }

    public void addListener(PackageInstaller.DownloadListener listener) {
        mPackageInstaller.addListener(listener);
    }

    public void removeListener(PackageInstaller.DownloadListener listener) {
        mPackageInstaller.removeListener(listener);
    }
}
