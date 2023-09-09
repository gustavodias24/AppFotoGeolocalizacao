package benicio.soluces.aplicativotestebencio.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import benicio.soluces.aplicativotestebencio.model.PontoModel;

public class PontosUtils {
    // Chave para armazenar a lista na SharedPreferences
    private static final String PREFS_PONTO = "pontosPrefs";
    private static final String PONTOS_KEY = "pontos";


    public static List<PontoModel> loadList(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS_PONTO, Context.MODE_PRIVATE);
        String carrosJson = sharedPreferences.getString(PONTOS_KEY, "");

        if (!carrosJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<PontoModel>>(){}.getType();
            return gson.fromJson(carrosJson, type);
        }

        return new ArrayList<>();
    }
    public static void saveList(List<PontoModel> carros, Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS_PONTO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String carrosJson = gson.toJson(carros);

        editor.putString(PONTOS_KEY, carrosJson);
        editor.apply();
    }
}

