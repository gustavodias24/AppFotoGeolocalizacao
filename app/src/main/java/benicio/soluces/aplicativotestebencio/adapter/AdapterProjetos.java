package benicio.soluces.aplicativotestebencio.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.model.ProjetoModel;

public class AdapterProjetos extends RecyclerView.Adapter<AdapterProjetos.MyViewHolder>{

    List<ProjetoModel> lista;
    Context c;
    Activity a;
    Dialog d;
    Boolean exibirBtn;

    public AdapterProjetos(List<ProjetoModel> lista, Context c, Activity a, Boolean exibirBtn) {
        this.lista = lista;
        this.c = c;
        this.a = a;
        this.exibirBtn = exibirBtn;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pontos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ProjetoModel projetoModel = lista.get(position);
        holder.img.setVisibility(View.GONE);
        if ( exibirBtn ){
            holder.exportarBtn.setVisibility(View.VISIBLE);
        }

        AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setMessage("Exportar para KMZ/KML ?");
        b.setNegativeButton("Não", null);
        b.setPositiveButton("Sim", (d, i) -> {
            projetoModel.gerarArquivoKML(a);
        });

        d = b.create();

        holder.exportarBtn.setOnClickListener( view -> {
            d.show();
        });

        holder.infos.setText(
                String.format("%s\n%s", projetoModel.getNomeProjeto(), projetoModel.getDataProjeto())
        );

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView infos;
        Button exportarBtn;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.icone_ponto);
            infos = itemView.findViewById(R.id.text_infos);
            exportarBtn = itemView.findViewById(R.id.exportart_btn);
        }
    }
}
