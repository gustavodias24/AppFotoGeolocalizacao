package benicio.soluces.aplicativotestebencio.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.model.ProjetoModel;
import benicio.soluces.aplicativotestebencio.model.ResponseIngurModel;
import benicio.soluces.aplicativotestebencio.service.ServiceIngur;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;
import benicio.soluces.aplicativotestebencio.util.RetrofitUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdapterProjetos extends RecyclerView.Adapter<AdapterProjetos.MyViewHolder>{

    List<ProjetoModel> lista;
    Context c;
    Activity a;
    Dialog d;
    Boolean exibirBtn;
    Retrofit retrofitIngur = RetrofitUtils.createRetrofitIngur();
    ServiceIngur serviceIngur = RetrofitUtils.createServiceIngur(retrofitIngur);

    private static final String CLIENT_ID = "c3585d73dc8693b4d1ea33beb0449c704b54dac7";

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

            File imageFile = ImageUtils.uriToFile(c, Uri.parse(projetoModel.getListaDePontos().get(0).getImages().get(0)));
            RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "Image Description");
            RequestBody image = RequestBody.create(MediaType.parse("image/png"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), image);

            serviceIngur.postarImage("Bearer " + CLIENT_ID, description, imagePart).enqueue(new Callback<ResponseIngurModel>() {
                @Override
                public void onResponse(Call<ResponseIngurModel> call, Response<ResponseIngurModel> response) {
                    if ( response.isSuccessful() ){
                        Log.d("uploadImage", response.body().getData().getLink());
                    }
                    Log.d("uploadImage", response.message());
                }

                @Override
                public void onFailure(Call<ResponseIngurModel> call, Throwable t) {
                    Log.d("uploadImage", t.getMessage());
                }
            });
//            projetoModel.gerarArquivoKML(a);
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
