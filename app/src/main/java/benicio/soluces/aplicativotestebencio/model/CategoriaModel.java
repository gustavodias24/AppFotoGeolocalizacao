package benicio.soluces.aplicativotestebencio.model;

public class CategoriaModel {
    String categoria;
    String nome;

    public CategoriaModel(String categoria, String nome) {
        this.categoria = categoria;
        this.nome = nome;
    }

    public CategoriaModel() {
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
