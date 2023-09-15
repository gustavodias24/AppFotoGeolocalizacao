package benicio.soluces.aplicativotestebencio.service;

import java.util.List;

import benicio.soluces.aplicativotestebencio.util.MsgModel;
import benicio.soluces.aplicativotestebencio.util.PostagemModel;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;
public interface ServiceNotificacoes {
    @POST("get_postagens")
    Call<List<PostagemModel>> recuperarPostagens();

    @POST("get_qtd_postagnes")
    Call<String> recuperaQtdPostagem();

    @POST("{id}/get_imagem")
    Call<MsgModel> pegarImage(@Path("id") String id);
}
