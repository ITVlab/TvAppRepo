package news.androidtv.tvapprepo.model;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a connection to the Firebase database for obtaining app data.
 *
 * @author Nick
 * @version 2016.09.08
 */
public class RepoDatabase {
    private static final String TAG = RepoDatabase.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static final String DATABASE_TYPE_TESTING = "test";
    public static final String DATABASE_TYPE_PROD = "production";
    public static final String DATABASE_TYPE_UNVERIFIED = "unverified";

    public static final String ACTION_APP_ADDED = "news.androidtv.tvapprepo.CHANGED_NEW_APP";

    private static RepoDatabase mRepoDatabase;
    private static HashMap<String, Apk> apps;
    private static List<Listener> listenerList;
    private static DatabaseReference databaseReference;

    private RepoDatabase() {
        listenerList = new ArrayList<>();
    }

    public static RepoDatabase getInstance() {
        return getInstance(DATABASE_TYPE_PROD);
    }

    public static RepoDatabase getInstance(String type) {
        if (apps == null) {
            apps = new HashMap<>();
        }
        // Read from the database
        if (mRepoDatabase == null) {
            mRepoDatabase = new RepoDatabase();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference(type);
            Log.d(TAG, "Create new instance");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Apk value = dataSnapshot1.getValue(Apk.class);
                        value.setKey(dataSnapshot1.getKey());
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
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
        return mRepoDatabase;
    }

    public void addListener(Listener listener) {
        listenerList.add(listener);
    }

    public void removeListener(Listener listener) {
        listenerList.remove(listener);
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

    public void incrementApkDownloads(Apk app) {
        databaseReference.child(app.getKey()).child("downloads").runTransaction(
                new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long updatedViews = mutableData.getValue(Long.class) + 1;
                mutableData.setValue(updatedViews);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
            }
        });
    }

    public void incrementApkViews(Apk app) {
        databaseReference.child(app.getKey()).child("views").runTransaction(
                new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long updatedViews = mutableData.getValue(Long.class) + 1;
                mutableData.setValue(updatedViews);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
            }
        });
    }

    public static void getLeanbackShortcut(String packageName, final LeanbackShortcutCallback callback) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("leanbackShortcuts");
        DatabaseReference shortcutReference = reference.child(packageName.replaceAll("[.]", "_"));
        if (DEBUG) {
            Log.d(TAG, "Looking at shortcut reference " + shortcutReference.toString());
            Log.d(TAG, "Looking for package " + packageName);
        }

        shortcutReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (DEBUG) {
                    Log.d(TAG, "Got value back " + dataSnapshot.toString());
                }
                try {
                    LeanbackShortcut leanbackShortcut =
                            dataSnapshot.getValue(LeanbackShortcut.class);
                    if (leanbackShortcut == null) {
                        if (DEBUG) {
                            Log.i(TAG, "No leanback shortcut");
                        }
                        callback.onNoLeanbackShortcut();
                        return;
                    }
                    if (DEBUG) {
                        Log.i(TAG, "This is a leanback shortcut");
                    }
                    callback.onLeanbackShortcut(leanbackShortcut);
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.i(TAG, "No leanback shortcut");
                        Log.e(TAG, e.getMessage());
                    }
                    callback.onNoLeanbackShortcut();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (DEBUG) {
                    Log.e(TAG, databaseError.toString());
                }
                callback.onDatabaseError(databaseError);
            }
        });
    }

    public interface Listener {
        void onApkAdded(Apk apk, int index);
    }

    public interface LeanbackShortcutCallback {
        void onNoLeanbackShortcut();
        void onLeanbackShortcut(LeanbackShortcut leanbackShortcut);
        void onDatabaseError(DatabaseError error);
    }
}
