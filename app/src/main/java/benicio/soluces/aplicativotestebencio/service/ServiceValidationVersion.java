package benicio.soluces.aplicativotestebencio.service;

import benicio.soluces.aplicativotestebencio.model.BodyModelValidarion;
import benicio.soluces.aplicativotestebencio.model.ValidationModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ServiceValidationVersion {

    @POST("version_lasted")
    Call<ValidationModel> validarVersion(@Body BodyModelValidarion version);

}
