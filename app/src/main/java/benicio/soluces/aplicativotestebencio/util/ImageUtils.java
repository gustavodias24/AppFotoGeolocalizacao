package benicio.soluces.aplicativotestebencio.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {
    private static  final String PREF_NAME_PREFERENCES_PARSER = "imageDataParse";
    private static  final String LIST_KEY = "imageListData";


    public static String imageToBase64(Uri imageUri, Context c) {
        try {
            InputStream inputStream = c.getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void saveList(Context context, List<String> myList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME_PREFERENCES_PARSER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(myList);

        editor.putString(LIST_KEY, json);
        editor.apply();
    }
    public static List<String> loadList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME_PREFERENCES_PARSER, Context.MODE_PRIVATE);

        String json = sharedPreferences.getString(LIST_KEY, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }
}
