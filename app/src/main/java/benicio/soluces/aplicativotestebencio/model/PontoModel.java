package benicio.soluces.aplicativotestebencio.model;

import java.util.List;

public class PontoModel {

    // essa lista é uma lista de Uri que convertir para String
    List<String> images;
    String categoria, obs, operador;
    Double latitude, longitude;

    public PontoModel(List<String> images, String categoria, String obs, String operador, Double latitude, Double longitude) {
        this.images = images;
        this.categoria = categoria;
        this.obs = obs;
        this.operador = operador;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PontoModel() {
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
