package benicio.soluces.aplicativotestebencio.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.databinding.ExibirImageLayoutBinding;

public class AdapterFotos extends RecyclerView.Adapter<AdapterFotos.MyViewHolder>{
    Dialog d;

    List<Uri> lista;
    Context c;

    Activity activity;

    public AdapterFotos(List<Uri> lista, Context c, Activity activity) {
        this.lista = lista;
        this.c = c;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_fotos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.fotoExibir.setImageURI(lista.get(position));
        holder.fotoExibir.setOnClickListener( view -> {
            AlertDialog.Builder b = new AlertDialog.Builder(activity);
            ExibirImageLayoutBinding bindingImageView = ExibirImageLayoutBinding.inflate(activity.getLayoutInflater());
            b.setPositiveButton("Fechar", null);
            bindingImageView.screenShortImage.setImageURI(lista.get(position));
            b.setView(bindingImageView.getRoot());
            d = b.create();
            d.show();
        });

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView fotoExibir;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fotoExibir = itemView.findViewById(R.id.foto_exibir);
        }
    }
}
