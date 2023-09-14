package benicio.soluces.aplicativotestebencio.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import benicio.soluces.aplicativotestebencio.model.ProjetoModel;

public class ProjetoUtils {
    // Chave para armazenar a lista na SharedPreferences
    private static final String PREFS_PROJETO = "pontosPrefs";
    private static final String PROJETO_KEY = "pontos";


    public static List<ProjetoModel> loadList(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS_PROJETO, Context.MODE_PRIVATE);
        String carrosJson = sharedPreferences.getString(PROJETO_KEY, "");

        if (!carrosJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ProjetoModel>>(){}.getType();
            return gson.fromJson(carrosJson, type);
        }

        return new ArrayList<>();
    }
    public static void saveList(List<ProjetoModel> projetos, Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(PREFS_PROJETO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String carrosJson = gson.toJson(projetos);

        editor.putString(PROJETO_KEY, carrosJson);
        editor.apply();
    }

    public static ProjetoModel getProjetoModel(String id, Context c){
        ProjetoModel projeto = null;
        for ( ProjetoModel p : loadList(c)){
            if ( p.getIdProjeto().equals(id)){
                projeto = p ;
            }
        }
        return projeto;
    }
}
