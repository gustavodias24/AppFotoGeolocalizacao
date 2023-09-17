package benicio.soluces.aplicativotestebencio.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.FileProvider;


import com.andremion.counterfab.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ProjetoModel {
    String idProjeto;
    String nomeProjeto, dataProjeto;
    List<PontoModel> listaDePontos = new ArrayList<>();

    public ProjetoModel(String idProjeto, String nomeProjeto, String dataProjeto, List<PontoModel> listaDePontos) {
        this.idProjeto = idProjeto;
        this.nomeProjeto = nomeProjeto;
        this.dataProjeto = dataProjeto;
        this.listaDePontos = listaDePontos;
    }

    public String getIdProjeto() {
        return idProjeto;
    }


    public ProjetoModel() {
    }

    public String getNomeProjeto() {
        return nomeProjeto;
    }


    public String getDataProjeto() {
        return dataProjeto;
    }


    public List<PontoModel> getListaDePontos() {
        return listaDePontos;
    }


    public void gerarArquivoKML(Activity context) {
        StringBuilder kmlBuilder = new StringBuilder();
        kmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        kmlBuilder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        kmlBuilder.append("<Document>\n");

        // Adicione informações do projeto ao KML
        kmlBuilder.append("<Placemark>\n");
        kmlBuilder.append("<name>").append(nomeProjeto).append("</name>\n");
        kmlBuilder.append("<description>").append(dataProjeto).append("</description>\n");
        kmlBuilder.append("</Placemark>\n");

        // Adicione os pontos do projeto ao KML
        for (PontoModel ponto : listaDePontos) {
            kmlBuilder.append("<Placemark>\n");
            kmlBuilder.append("<name>").append(ponto.categoria).append("</name>\n");
            kmlBuilder.append("<description>").append(ponto.obs).append("</description>\n");
            kmlBuilder.append("<Point>\n");
            kmlBuilder.append("<coordinates>").append(ponto.longitude).append(",").append(ponto.latitude).append(",0</coordinates>\n");
            kmlBuilder.append("</Point>\n");

            // Adicione imagens aos pontos (se houver)
            for (String imagemUri : ponto.images) {
                Bitmap bitmap = decodeUriToBitmap(context, Uri.parse(imagemUri));
                if (bitmap != null) {
                    String base64Image = encodeBitmapToBase64(bitmap);

                    kmlBuilder.append("<PhotoOverlay>\n");
                    kmlBuilder.append("<name>Imagem</name>\n");
                    kmlBuilder.append("<Icon>\n");
                    kmlBuilder.append("<href>").append("data:image/png;base64,").append(base64Image).append("</href>\n");
                    kmlBuilder.append("</Icon>\n");

                    // Adicione a imagem ao Carousel
                    kmlBuilder.append("<View>\n");
                    kmlBuilder.append("<gx:Carousel>\n");
                    kmlBuilder.append("<gx:Image>\n");
                    kmlBuilder.append("<href>").append("data:image/png;base64,").append(base64Image).append("</href>\n");
                    kmlBuilder.append("</gx:Image>\n");
                    kmlBuilder.append("</gx:Carousel>\n");
                    kmlBuilder.append("</View>\n");

                    kmlBuilder.append("<ViewVolume>\n");
                    kmlBuilder.append("<leftFov>-60.0</leftFov>\n");
                    kmlBuilder.append("<rightFov>60.0</rightFov>\n");
                    kmlBuilder.append("<bottomFov>-45.0</bottomFov>\n");
                    kmlBuilder.append("<topFov>45.0</topFov>\n");
                    kmlBuilder.append("</ViewVolume>\n");
                    kmlBuilder.append("</PhotoOverlay>\n");
                }
            }

            kmlBuilder.append("</Placemark>\n");
        }

        kmlBuilder.append("</Document>\n");
        kmlBuilder.append("</kml>");

        // Salvar o arquivo KML no armazenamento externo
        File kmlFile = new File(context.getExternalFilesDir(null), "marker.kml");
        try {
            FileOutputStream fos = new FileOutputStream(kmlFile);
            fos.write(kmlBuilder.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Comprimir o arquivo KML em um arquivo KMZ
        File kmzFile = new File(context.getExternalFilesDir(null), "marker.kmz");
        try {
            FileOutputStream fos = new FileOutputStream(kmzFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // Adicionar o arquivo KML ao arquivo KMZ
            ZipEntry entry = new ZipEntry("marker.kml");
            zos.putNextEntry(entry);
            byte[] kmlBytes = kmlBuilder.toString().getBytes();
            zos.write(kmlBytes, 0, kmlBytes.length);
            zos.closeEntry();

            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Abra o arquivo KMZ no aplicativo Google Earth
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(context),
                "benicio.soluces.aplicativotestebencio.provider", kmzFile);

        intent.setDataAndType(uri, "application/vnd.google-earth.kmz");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    private Bitmap decodeUriToBitmap(Context context, Uri uri) {
        try {
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}
