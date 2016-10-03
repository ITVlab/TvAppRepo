package news.android.shortcut;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sample.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getString(R.string.activity_to_launch).equals("null")) {
            startActivity(new Intent(getString(R.string.activity_to_launch)));
        } else {
            startActivity(new Intent(getPackageManager()
                    .getLaunchIntentForPackage(getString(R.string.package_name))));
        }
    }
}
