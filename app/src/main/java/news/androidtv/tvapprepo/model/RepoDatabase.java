package news.androidtv.tvapprepo.model;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents a connection to the Firebase database for obtaining app data.
 *
 * @author Nick
 * @version 2016.09.08
 */
public class RepoDatabase {
    private static final String TAG = RepoDatabase.class.getSimpleName();

    public static final String DATABASE_TYPE_TESTING = "test";
    public static final String DATABASE_TYPE_PROD = "production";
    public static final String DATABASE_TYPE_UNVERIFIED = "unverified";

    public static final String ACTION_APP_ADDED = "news.androidtv.tvapprepo.CHANGED_NEW_APP";

    private static RepoDatabase mRepoDatabase;
    private static HashMap<String, Apk> apps;
    private static List<Listener> listenerList;

    public static RepoDatabase getInstance() {
        return getInstance(DATABASE_TYPE_TESTING);
    }

    public static RepoDatabase getInstance(String type) {
        // Read from the database
        if (mRepoDatabase == null) {
            mRepoDatabase = new RepoDatabase();
        }
        if (apps == null) {
            apps = new HashMap<>();
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(type);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Apk value = dataSnapshot.getValue(Apk.class);
                Log.d(TAG, "Value is: " + value);
                if (apps.containsKey(value.getPackageName()) &&
                        apps.get(value.getPackageName()).getVersionCode() < value.getVersionCode() ||
                        !apps.containsKey(value.getPackageName())) {
                    for (Listener listener : listenerList) {
                        listener.onApkAdded(value, apps.size());
                    }
                    apps.put(value.getPackageName(), value);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        return mRepoDatabase;
    }

    public void addListener(Listener listener) {
        listenerList.add(listener);
    }

    public void removeListener(Listener listener) {
        listenerList.remove(listener);
    }

    private RepoDatabase() {
    }

    public int getAppCount() {
        return apps.size();
    }

    public HashMap<String, Apk> getApps() {
        return apps;
    }

    public List<Apk> getAppList() {
        return new ArrayList<>(apps.values());
    }

    public interface Listener {
        void onApkAdded(Apk apk, int index);
    }
}
