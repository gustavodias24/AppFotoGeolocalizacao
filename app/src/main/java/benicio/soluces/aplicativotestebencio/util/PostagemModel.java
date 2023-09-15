package benicio.soluces.aplicativotestebencio.util;

public class PostagemModel {
    String data;
    String titulo, descricao, _id;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    String imagem;
    Boolean tem_imagem;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Boolean getTem_imagem() {
        return tem_imagem;
    }

    public void setTem_imagem(Boolean tem_imagem) {
        this.tem_imagem = tem_imagem;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }
}

