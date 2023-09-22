package benicio.soluces.aplicativotestebencio.model;

import java.util.ArrayList;
import java.util.List;

public class PontoModel {

    // essa lista é uma lista de Uri que convertir para String
    List<String> images = new ArrayList<>();
    List<String> imagesLink = new ArrayList<>();
    String categoria, obs, operador;
    String data = "";
    Double latitude, longitude;


    public PontoModel(String data,List<String> images, String categoria, String obs, String operador, Double latitude, Double longitude) {
        this.data = data;
        this.images = images;
        this.categoria = categoria;
        this.obs = obs;
        this.operador = operador;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PontoModel() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getImagesLink() {
        return imagesLink;
    }

    public void setImagesLink(List<String> imagesLink) {
        this.imagesLink = imagesLink;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }



    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
