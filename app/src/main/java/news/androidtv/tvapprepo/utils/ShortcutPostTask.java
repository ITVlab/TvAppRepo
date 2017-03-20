package news.androidtv.tvapprepo.utils;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.modules.VolleyMultipartRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import news.androidtv.tvapprepo.R;
import news.androidtv.tvapprepo.model.AdvancedOptions;

/**
 * Created by Nick on 1/12/2017.
 *
 * This activity interfaces with a server and sends metadata to turn into a shortcut.
 *
 * Sample response:
 *
 * <code>
 *     {
 *     "build_ok":true,
 *     "app":{
 *          "download_link":"http:\/\/atvlauncher.trekgonewild.de\/download.php?v=5878266f0c5ef456227195",
 *          "name":"Cybercritters",
 *          "package":"com.felkertech.n.virtualpets",
 *          "category":"apps",
 *          "logo":"http:\/\/atvlauncher.trekgonewild.de\/showimage.php?v=5878266f0c5ef456227195&t=l"
*       }
 *     }
 * </code>
 */

public class ShortcutPostTask {
    private static final String TAG = ShortcutPostTask.class.getSimpleName();

    private static final String SUBMISSION_URL =
            "http://atvlauncher.trekgonewild.de/index_test.php";
    private static final String FORM_APP_NAME = "app_name";
    private static final String FORM_APP_PACKAGE = "app_package";
    private static final String FORM_APP_CATEGORY = "app_category";
    private static final String FORM_APP_LOGO = "app_logo";
    private static final String FORM_APP_BANNER = "app_banner";
    private static final String FORM_JSON = "json";
    public static final String CATEGORY_GAMES = "games";
    public static final String CATEGORY_APPS = "apps";

    private ShortcutPostTask() {
    }

    public static void generateShortcut(final Context context, final ResolveInfo app,
            final AdvancedOptions options, final Callback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        VolleyMultipartRequest sr = new VolleyMultipartRequest(Request.Method.POST,
                SUBMISSION_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d(TAG, "Response: " + new String(response.data));
                        callback.onResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if  (error.networkResponse != null) {
//                    Log.e(TAG, "Error: " + error.networkResponse.headers);
//                    Log.e(TAG, "Error: " + new String(error.networkResponse.data));
                }
//                Log.e(TAG, "Error: " + error.getMessage());
//                Log.d(TAG, error.toString());
                callback.onError(error);
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put(FORM_APP_NAME, app.activityInfo.loadLabel(context.getPackageManager()).toString());
                params.put(FORM_APP_PACKAGE, app.activityInfo.applicationInfo.packageName);
                params.put(FORM_APP_CATEGORY, CATEGORY_APPS);
                if (!options.getCategory().isEmpty()) {
                    params.put(FORM_APP_CATEGORY, options.getCategory());
                }
                params.put(FORM_JSON, "true");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put(FORM_APP_LOGO, new DataPart("file_avatar.png",
                        VolleyMultipartRequest.getFileDataFromDrawable(context,
                                app.activityInfo.loadIcon(context.getPackageManager())),
                                "image/png"));
                if (options.getBanner() != null) {
                    params.put(FORM_APP_LOGO, new DataPart("file_avatar.png", options.getBanner(),
                            "image/png"));
                }
                return params;
            }
        };
        try {
            Log.d(TAG, sr.getHeaders().toString());
            Log.d(TAG, new String(sr.getBody()));
            Log.d(TAG, sr.getBodyContentType());
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        sr.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 1, 0));
        queue.add(sr);
    }

    public interface Callback {
        void onResponse(NetworkResponse response);
        void onError(VolleyError error);
    }
}
