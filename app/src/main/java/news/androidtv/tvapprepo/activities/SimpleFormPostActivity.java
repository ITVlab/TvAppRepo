package news.androidtv.tvapprepo.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sketchproject.infogue.modules.VolleyMultipartRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import news.androidtv.tvapprepo.R;

/**
 * Created by Nick on 1/11/2017.
 */

public class SimpleFormPostActivity extends Activity {
    private static final String TAG = SimpleFormPostActivity.class.getSimpleName();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_A) {
            submitForm();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void submitForm() {
        String postTo = "http://atvlauncher.trekgonewild.de/index_test.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        VolleyMultipartRequest sr = new VolleyMultipartRequest(Request.Method.POST,
                postTo,
                new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Log.d(TAG, "Response: " + new String(response.data));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if  (error.networkResponse != null) {
                    Log.e(TAG, "Error: " + error.networkResponse.headers);
                    Log.e(TAG, "Error: " + new String(error.networkResponse.data));
                }
                Log.e(TAG, "Error: " + error.getMessage());
                Log.d(TAG, error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("app_name", "Cybercritters");
                params.put("app_package", "com.felkertech.n.cybercritters");
                params.put("app_category", "games"); // Or apps
                Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon_quantum);
//                params.put("app_logo2", getStringImage(appIcon)); // Need to stringify bitmap
                params.put("json", "true");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("app_logo", new DataPart("file_avatar.png",
                        VolleyMultipartRequest.getFileDataFromDrawable(getBaseContext(),
                                getDrawable(R.drawable.ic_launcher)), "image/png"));
                return params;
            }

/*            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                return params;
            }*/
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

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
