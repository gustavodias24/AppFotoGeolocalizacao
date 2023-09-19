package benicio.soluces.aplicativotestebencio.model;

public class ResponseIngurModel {
    private int status;
    private DataIngurModel data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataIngurModel getData() {
        return data;
    }

    public void setData(DataIngurModel data) {
        this.data = data;
    }
}
