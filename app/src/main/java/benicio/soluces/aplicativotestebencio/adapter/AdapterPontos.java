package benicio.soluces.aplicativotestebencio.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import benicio.soluces.aplicativotestebencio.model.PontoModel;
import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;

public class AdapterPontos extends RecyclerView.Adapter<AdapterPontos.MyViewHolder>{
    List<PontoModel> lista;
    Context c;

    public AdapterPontos(List<PontoModel> lista, Context c) {
        this.lista = lista;
        this.c = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pontos, parent, false));
    }
    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PontoModel ponto = lista.get(position);

        holder.infos.setText(String.format("%s\nLat: %f Long: %f", ponto.getObs(), ponto.getLatitude(), ponto.getLongitude()));
        Picasso.get().load(ImageUtils.getLinkIconeDoPonto(ponto.getCategoria(), c)).into(holder.icone);

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView icone;
        TextView infos;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            icone = itemView.findViewById(R.id.icone_ponto);
            infos = itemView.findViewById(R.id.text_infos);
        }
    }
}
