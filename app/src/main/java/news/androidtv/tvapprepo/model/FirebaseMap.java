package news.androidtv.tvapprepo.model;

import java.util.HashMap;

/**
 * Firebase uses a unique serialization scheme for maps in Android, and it's not a standard
 * {@link org.json.JSONObject}. This class converts between this serialization and a map.
 */
public class FirebaseMap {
    private String mInputData;
    private HashMap<String, String> mMap;

    public FirebaseMap(String inputData) {
        mInputData = inputData;
        parse();
    }

    private void parse() {
        mMap = new HashMap<>();
        int index = mInputData.indexOf("{");
        String temp = new String(mInputData); // Make a copy that we can destroy;
        temp = temp.substring(index + 1);
        while (index >= 0) { // Continually parse until we run out of characters
            index = temp.indexOf("=");
            String key = temp.substring(0, index).trim();
            temp = temp.substring(index + 1);
            index = temp.indexOf("',");
            if (index == -1) {
                String value = temp.substring(0, temp.length() - 2).trim();
                mMap.put(key, value);
            } else {
                String value = temp.substring(0, index).trim();
                mMap.put(key, value);
                temp = temp.substring(index + 2).trim();
            }
        }
    }

    public HashMap<String, String> getMap() {
        return mMap;
    }

    public String getRawString() {
        return mInputData;
    }
}
