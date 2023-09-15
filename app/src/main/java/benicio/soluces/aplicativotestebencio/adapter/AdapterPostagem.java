package benicio.soluces.aplicativotestebencio.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.service.ServiceNotificacoes;
import benicio.soluces.aplicativotestebencio.util.MsgModel;
import benicio.soluces.aplicativotestebencio.util.PostagemModel;
import benicio.soluces.aplicativotestebencio.util.RetrofitUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdapterPostagem extends RecyclerView.Adapter<AdapterPostagem.MyViewHolder>{
    List<PostagemModel> lista;
    Context c;

    private Retrofit retrofit = RetrofitUtils.createRetrofit();
    private ServiceNotificacoes service = RetrofitUtils.createServiceNotificaceos(retrofit);

    public AdapterPostagem(List<PostagemModel> lista, Context c) {
        this.lista = lista;
        this.c = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.postagem_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PostagemModel postagem  = lista.get(position);

        holder.conteudo.setText(postagem.getDescricao());
        holder.title.setText(postagem.getTitulo());

        if ( postagem.getData() != null){
            holder.data.setText("Postado no dia " + postagem.getData());
        }
        if ( postagem.getTem_imagem() ){
            service.pegarImage(postagem.get_id()).enqueue(new Callback<MsgModel>() {
                @Override
                public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                    if ( response.isSuccessful() ){
                        byte[] imageBytes = Base64.decode(response.body().getMsg(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        holder.image.setImageBitmap(bitmap);
                    }
                }
                @Override
                public void onFailure(Call<MsgModel> call, Throwable t) {
                }
            });

        }else{
            holder.image.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title, conteudo, data;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.postagem_img);
            title = itemView.findViewById(R.id.tittle_postagem);
            conteudo = itemView.findViewById(R.id.conteudo);
            data = itemView.findViewById(R.id.data_text);

        }
    }
}
