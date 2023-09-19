package benicio.soluces.aplicativotestebencio.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import benicio.soluces.aplicativotestebencio.databinding.LayoutCarregamentoBinding;
import benicio.soluces.aplicativotestebencio.service.ServiceIngur;
import benicio.soluces.aplicativotestebencio.service.ServiceNotificacoes;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils {

    public static Retrofit createRetrofit(){
        return new Retrofit.Builder()
                .baseUrl("https://comunicao-clientes-kaizen.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit createRetrofitIngur(){
        return new Retrofit.Builder()
                .baseUrl("https://api.imgur.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ServiceIngur createServiceIngur(Retrofit retrofit){
        return retrofit.create(ServiceIngur.class);
    }

    public static ServiceNotificacoes createServiceNotificaceos(Retrofit retrofit){
        return retrofit.create(ServiceNotificacoes.class);
    }

    public static Dialog criarDialogCarregando(Context c, Activity a){
        AlertDialog.Builder b = new AlertDialog.Builder(c);
        b.setCancelable(false);
        b.setView(LayoutCarregamentoBinding.inflate(a.getLayoutInflater()).getRoot());
        return b.create();
    }
}
