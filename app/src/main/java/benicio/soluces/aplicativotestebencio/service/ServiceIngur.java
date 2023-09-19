package benicio.soluces.aplicativotestebencio.service;

import benicio.soluces.aplicativotestebencio.model.ResponseIngurModel;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ServiceIngur {
    @Multipart
    @POST("3/image")
    Call<ResponseIngurModel> postarImage(
            @Header("Authorization") String authorization,
            @Part("description") RequestBody description,
            @Part MultipartBody.Part image
    );
}
