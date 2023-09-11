package benicio.soluces.aplicativotestebencio.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import benicio.soluces.aplicativotestebencio.R;

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

    public static Drawable getIconeDoPonto(String categoria, int larguraEmPixels, int alturaEmPixels, Context c) {


        @SuppressLint("ResourceType") Drawable arvore = ContextCompat.getDrawable(c, R.raw.arvore);
        @SuppressLint("ResourceType") Drawable erosao = ContextCompat.getDrawable(c, R.raw.erosao);
        @SuppressLint("ResourceType") Drawable estacaoenergia = ContextCompat.getDrawable(c, R.raw.estacaoenergia);
        @SuppressLint("ResourceType") Drawable outros = ContextCompat.getDrawable(c, R.raw.outros);
        @SuppressLint("ResourceType") Drawable poste = ContextCompat.getDrawable(c, R.raw.poste);
        @SuppressLint("ResourceType") Drawable rocada = ContextCompat.getDrawable(c, R.raw.rocada);
        Drawable drawable = null;

        switch (categoria) {
            case "árvore":
                drawable = arvore;
                break;
            case "poste":
                drawable = poste;
                break;
            case "área de roçada":
                drawable = rocada;
                break;
            case "subestação de energia":
                drawable = estacaoenergia;
                break;
            case "erosão":
                drawable = erosao;
                break;
            case "outros":
                drawable = outros;
                break;
        }

        assert drawable != null;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, larguraEmPixels, alturaEmPixels, false);
        return new BitmapDrawable(c.getResources(), bitmapRedimensionado);
    }
}