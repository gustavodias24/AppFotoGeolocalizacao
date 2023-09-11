package benicio.soluces.aplicativotestebencio.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import benicio.soluces.aplicativotestebencio.databinding.LayoutCarregamentoBinding;

public class RetrofitUtils {

    public static Dialog criarDialogCarregando(Activity a){
        AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setCancelable(false);
        b.setView(LayoutCarregamentoBinding.inflate(a.getLayoutInflater()).getRoot());
        return b.create();
    }
}
