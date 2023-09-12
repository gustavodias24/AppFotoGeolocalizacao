package benicio.soluces.aplicativotestebencio.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public AdapterProjetos(List<ProjetoModel> lista, Context c) {
        this.lista = lista;
        this.c = c;
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
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.icone_ponto);
            infos = itemView.findViewById(R.id.text_infos);
        }
    }
}
