package benicio.soluces.aplicativotestebencio.adapter;

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

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.model.CategoriaModel;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;

public class AdapterCategorias extends RecyclerView.Adapter<AdapterCategorias.MyViewHolder>{

    List<CategoriaModel> lista;
    Context context;

    public AdapterCategorias(List<CategoriaModel> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pontos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            CategoriaModel categoriaModel = lista.get(position);

            holder.categoriaNome.setText(categoriaModel.getNome());
            Picasso.get().load(ImageUtils.getLinkIconeDoPonto(categoriaModel.getCategoria(), context)).into(holder.categoriaImg);

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder {
        ImageView categoriaImg;
        TextView categoriaNome;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            categoriaImg = itemView.findViewById(R.id.icone_ponto);
            categoriaNome = itemView.findViewById(R.id.text_infos);
        }
    }
}
