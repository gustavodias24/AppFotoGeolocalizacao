package benicio.soluces.aplicativotestebencio.model;

import java.util.ArrayList;
import java.util.List;

public class ProjetoModel {
    String nomeProjeto, dataProjeto;
    List<PontoModel> listaDePontos = new ArrayList<>();

    public ProjetoModel(String nomeProjeto, String dataProjeto, List<PontoModel> listaDePontos) {
        this.nomeProjeto = nomeProjeto;
        this.dataProjeto = dataProjeto;
        this.listaDePontos = listaDePontos;
    }

    public ProjetoModel() {
    }

    public String getNomeProjeto() {
        return nomeProjeto;
    }

    public void setNomeProjeto(String nomeProjeto) {
        this.nomeProjeto = nomeProjeto;
    }

    public String getDataProjeto() {
        return dataProjeto;
    }

    public void setDataProjeto(String dataProjeto) {
        this.dataProjeto = dataProjeto;
    }

    public List<PontoModel> getListaDePontos() {
        return listaDePontos;
    }

    public void setListaDePontos(List<PontoModel> listaDePontos) {
        this.listaDePontos = listaDePontos;
    }
}
