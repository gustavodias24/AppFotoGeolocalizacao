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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.databinding.LayoutCarregamentoBinding;
import benicio.soluces.aplicativotestebencio.databinding.LayoutCarregandoImageBinding;
import benicio.soluces.aplicativotestebencio.model.PontoModel;
import benicio.soluces.aplicativotestebencio.model.ProjetoModel;
import benicio.soluces.aplicativotestebencio.model.ResponseIngurModel;
import benicio.soluces.aplicativotestebencio.service.ServiceIngur;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;
import benicio.soluces.aplicativotestebencio.util.ProjetoUtils;
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
    Dialog dialogCarregamento;
    ProgressBar progressImage;

    private static final String TOKEN = "c3585d73dc8693b4d1ea33beb0449c704b54dac7";
    int quantidadeDeLinkGerado = 0;

    public AdapterProjetos(List<ProjetoModel> lista, Context c, Activity a, Boolean exibirBtn) {
        this.lista = lista;
        this.c = c;
        this.a = a;
        this.exibirBtn = exibirBtn;
        this.dialogCarregamento = criarDialogCarregamento();
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
            dialogCarregamento.show();
            progressImage.setProgress(0);

            quantidadeDeLinkGerado = 0;
            int quantidadeTotalDeImages = 0;
            for(PontoModel ponto: projetoModel.getListaDePontos()){
                List<String> listaDeImageUri = ponto.getImages() == null ? new ArrayList<>() : ponto.getImages();
                for ( String imageString : listaDeImageUri){
                    quantidadeTotalDeImages++;
                }
            }

            progressImage.setMax(quantidadeTotalDeImages);

            int positionPonto = 0;
            if ( !projetoModel.getListaDePontos().isEmpty() ) {
                for (PontoModel ponto : projetoModel.getListaDePontos()) {

                    // isso evita criar as mesmas imagens
                    List<String> listaDeImageLink = ponto.getImagesLink() == null ? new ArrayList<>() : ponto.getImagesLink();
                    List<String> listaDeImageUri = ponto.getImages() == null ? new ArrayList<>() : ponto.getImages();
                    if (listaDeImageLink.size() != listaDeImageUri.size()) {

                        for (String uriImage : ponto.getImages()) {
                            returnImagemNoIngu(positionPonto, Uri.parse(uriImage), position, quantidadeTotalDeImages);
                        }

                    } else {
                        dialogCarregamento.dismiss();
                        projetoModel.gerarArquivoKML(a);
                    }

                    positionPonto++;
                }
            }else{
                dialogCarregamento.dismiss();
                Toast.makeText(c, "Esse projeto está vazio!", Toast.LENGTH_SHORT).show();
            }

        });

        d = b.create();

        holder.exportarBtn.setOnClickListener( view -> {
            d.show();
        });

        holder.infos.setText(
                String.format("%s %s", projetoModel.getDataProjeto(), projetoModel.getNomeProjeto())
        );

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView infos;
        ImageButton exportarBtn;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.icone_ponto);
            infos = itemView.findViewById(R.id.text_infos);
            exportarBtn = itemView.findViewById(R.id.exportart_btn);
        }
    }

    public void returnImagemNoIngu(int positionPonto,Uri uriImage, int position, int quantidadeTotalDeImages){

        File imageFile = ImageUtils.uriToFile(c, uriImage);
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "Image Description");
        RequestBody image = RequestBody.create(MediaType.parse("image/png"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), image);

        serviceIngur.postarImage("Bearer " + TOKEN, description, imagePart).enqueue(new Callback<ResponseIngurModel>() {
            @Override
            public void onResponse(Call<ResponseIngurModel> call, Response<ResponseIngurModel> response) {
                if ( response.isSuccessful() ){
                    progressImage.setProgress( progressImage.getProgress() + 1);
                    lista.get(position).getListaDePontos().get(positionPonto).getImagesLink().add(response.body().getData().getLink());
                    quantidadeDeLinkGerado++;
                }else{
                    quantidadeDeLinkGerado++;
                    progressImage.setProgress( progressImage.getProgress() + 1);
                    lista.get(position).getListaDePontos().get(positionPonto).getImagesLink().add("");
                }
                verificarTermino(quantidadeTotalDeImages, quantidadeDeLinkGerado, position);
            }

            @Override
            public void onFailure(Call<ResponseIngurModel> call, Throwable t) {
                Toast.makeText(c, t.getMessage(), Toast.LENGTH_SHORT).show();
                progressImage.setProgress( progressImage.getProgress() + 1);
                lista.get(position).getListaDePontos().get(positionPonto).getImagesLink().add("");
                quantidadeDeLinkGerado++;
                verificarTermino(quantidadeTotalDeImages, quantidadeDeLinkGerado, position);
            }
        });
    }
    public void verificarTermino(int quatidadeTotalDeImages, int quatidadeProcessada, int position){
        if (quatidadeTotalDeImages == quatidadeProcessada){
            dialogCarregamento.dismiss();
            // escreve por cima o novo projetoModel com as imagens link
            ProjetoUtils.saveList(lista, c);

            lista.get(position).gerarArquivoKML(a);
        }
    }

    private Dialog criarDialogCarregamento(){
        AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setCancelable(false);
        LayoutCarregandoImageBinding carregandoImageBinding = LayoutCarregandoImageBinding.inflate(a.getLayoutInflater());
        progressImage = carregandoImageBinding.carregamentoImageProgress;
        b.setView(carregandoImageBinding.getRoot());
        return b.create();
    }
}
