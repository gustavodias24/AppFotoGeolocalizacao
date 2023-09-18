package benicio.soluces.aplicativotestebencio.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.FileProvider;


import com.andremion.counterfab.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import benicio.soluces.aplicativotestebencio.util.ImageUtils;


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
        kmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        kmlBuilder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        kmlBuilder.append("<Document>\n");

        kmlBuilder.append("<name>"+ nomeProjeto.replace(" ", "_") +"</name>");
        kmlBuilder.append("\t<description>Projeto criado no dia "+ dataProjeto +"</description>\n");

        // Para cada ponto do projeto
        for ( PontoModel ponto : listaDePontos){
            // icone do ponto

            String placeMarkId = UUID.randomUUID().toString().replace("-", "");
            String styleMapId = UUID.randomUUID().toString().replace("-", "");
            String highlightId = UUID.randomUUID().toString().replace("-", "");
            String normalId = UUID.randomUUID().toString().replace("-", "");

            kmlBuilder.append(returnCascadingStyle(highlightId, normalId, ponto.getCategoria(), context));
            kmlBuilder.append(returnStyleMap(normalId, highlightId, styleMapId));

            kmlBuilder.append(returnPlacemark(
                    placeMarkId,
                    styleMapId,
                    ponto.getCategoria(),
                    ponto.getObs(),
                    ponto.getLongitude().toString(),
                    ponto.getLatitude().toString(),
                    ponto.getImages(),
                    context
            ));
        }

        kmlBuilder.append("</Document>\n");
        kmlBuilder.append("</kml>\n");

        // Obtém o diretório "Documentos" no armazenamento externo
        File documentosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        // Crie a pasta "KaizenProjetos" dentro do diretório "Documentos"
        File kaizenProjetosDir = new File(documentosDir, "KaizenWayPointProjetos");
        if (!kaizenProjetosDir.exists()) {
            kaizenProjetosDir.mkdirs();
        }



        // Salvar o arquivo KML no armazenamento externo
        File kmlFile = new File(kaizenProjetosDir, this.nomeProjeto + ".kml");
        try {
            FileOutputStream fos = new FileOutputStream(kmlFile);
            fos.write(kmlBuilder.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Comprimir o arquivo KML em um arquivo KMZ
        File kmzFile = new File(kaizenProjetosDir, this.nomeProjeto + ".kmz");
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

            Log.d("ProjetoModel", "Arquivo KMZ gerado e salvo em " + kmzFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ProjetoModel", "Erro ao gerar e salvar o arquivo KMZ" + e.getMessage());
        }

        // Abra o arquivo KMZ no aplicativo Google Earth
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(context),
                "benicio.soluces.aplicativotestebencio.provider", kmzFile);

        intent.setDataAndType(uri, "application/vnd.google-earth.kmz");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    public String getIconeBase64(Context c, String categoria){
        return ImageUtils.convertDrawableToBase64(
                c.getResources(),
                ImageUtils.getIconeDoPonto(categoria, 32, 32, c)
                );
    }

    public String returnCascadingStyle(String highlightId,String normalId, String categoria, Context c){
        StringBuilder xmlStringBuilder = new StringBuilder();

        xmlStringBuilder.append("<gx:CascadingStyle kml:id=\"__managed_style_"+highlightId+"\">\n");
        xmlStringBuilder.append("    <styleUrl>https://earth.google.com/balloon_components/base/1.0.26.0/card_template.kml#main</styleUrl>\n");
        xmlStringBuilder.append("    <Style>\n");
        xmlStringBuilder.append("        <IconStyle>\n");
        xmlStringBuilder.append("            <scale>1.2</scale>\n");
        xmlStringBuilder.append("            <Icon>\n");
        xmlStringBuilder.append("                <href>"+ getIconeBase64(c, categoria) +"</href>\n");
        xmlStringBuilder.append("            </Icon>\n");
        xmlStringBuilder.append("        </IconStyle>\n");
        xmlStringBuilder.append("        <LabelStyle>\n");
        xmlStringBuilder.append("        </LabelStyle>\n");
        xmlStringBuilder.append("        <LineStyle>\n");
        xmlStringBuilder.append("            <color>ff2dc0fb</color>\n");
        xmlStringBuilder.append("            <width>6</width>\n");
        xmlStringBuilder.append("        </LineStyle>\n");
        xmlStringBuilder.append("        <PolyStyle>\n");
        xmlStringBuilder.append("            <color>40ffffff</color>\n");
        xmlStringBuilder.append("        </PolyStyle>\n");
        xmlStringBuilder.append("        <BalloonStyle>\n");
        xmlStringBuilder.append("        </BalloonStyle>\n");
        xmlStringBuilder.append("    </Style>\n");
        xmlStringBuilder.append("</gx:CascadingStyle>");

        xmlStringBuilder.append("<gx:CascadingStyle kml:id=\"__managed_style_"+normalId+"\">\n");
        xmlStringBuilder.append("    <styleUrl>https://earth.google.com/balloon_components/base/1.0.26.0/card_template.kml#main</styleUrl>\n");
        xmlStringBuilder.append("    <Style>\n");
        xmlStringBuilder.append("        <IconStyle>\n");
        xmlStringBuilder.append("            <Icon>\n");
        xmlStringBuilder.append("                <href>"+ getIconeBase64(c, categoria) +"</href>\n");
        xmlStringBuilder.append("            </Icon>\n");
        xmlStringBuilder.append("        </IconStyle>\n");
        xmlStringBuilder.append("        <LabelStyle>\n");
        xmlStringBuilder.append("        </LabelStyle>\n");
        xmlStringBuilder.append("        <LineStyle>\n");
        xmlStringBuilder.append("            <color>ff2dc0fb</color>\n");
        xmlStringBuilder.append("            <width>4</width>\n");
        xmlStringBuilder.append("        </LineStyle>\n");
        xmlStringBuilder.append("        <PolyStyle>\n");
        xmlStringBuilder.append("            <color>40ffffff</color>\n");
        xmlStringBuilder.append("        </PolyStyle>\n");
        xmlStringBuilder.append("        <BalloonStyle>\n");
        xmlStringBuilder.append("        </BalloonStyle>\n");
        xmlStringBuilder.append("    </Style>\n");
        xmlStringBuilder.append("</gx:CascadingStyle>\n");


        return xmlStringBuilder.toString();
    }
    public String returnStyleMap(String normal, String highlight, String stylemap){
        StringBuilder xmlStringBuilder = new StringBuilder();

        xmlStringBuilder.append("<StyleMap id=\"__managed_style_"+stylemap+"\">\n");
        xmlStringBuilder.append("    <Pair>\n");
        xmlStringBuilder.append("        <key>normal</key>\n");
        xmlStringBuilder.append("        <styleUrl>#__managed_style_"+ normal +"</styleUrl>\n");
        xmlStringBuilder.append("    </Pair>\n");
        xmlStringBuilder.append("    <Pair>\n");
        xmlStringBuilder.append("        <key>highlight</key>\n");
        xmlStringBuilder.append("        <styleUrl>#__managed_style_"+ highlight +"</styleUrl>\n");
        xmlStringBuilder.append("    </Pair>\n");
        xmlStringBuilder.append("</StyleMap>");

        return xmlStringBuilder.toString();
    }
    public String returnPlacemark(String id, String stylemapId, String titulo, String descri, String longi, String lat, List<String> images, Context c){
        StringBuilder xmlStringBuilder = new StringBuilder();

        xmlStringBuilder.append("<Placemark id=\""+id+"\">")
                .append("\n\t<name>"+titulo+"</name>")
                .append("\n\t<description><![CDATA[<div>"+descri+"</div>]]></description>")
                .append("\n\t<LookAt>")
                .append("\n\t\t<longitude>"+longi+"</longitude>")
                .append("\n\t\t<latitude>"+lat+"</latitude>")
                .append("\n\t\t<altitude>431.4313290066361</altitude>")
                .append("\n\t\t<heading>0</heading>")
                .append("\n\t\t<tilt>0</tilt>")
                .append("\n\t\t<gx:fovy>30</gx:fovy>")
                .append("\n\t\t<range>169.5750582342589</range>")
                .append("\n\t\t<altitudeMode>absolute</altitudeMode>")
                .append("\n\t</LookAt>")
                .append("\n\t<styleUrl>#__managed_style_"+stylemapId+"</styleUrl>")
                .append("\n\t<gx:Carousel>");

                for (String imageUri : images){
                    xmlStringBuilder.append("\n\t\t<gx:Image kml:id=\"embedded_image_03AE9FBE172BDD5C899D_0\">")
                    .append("\n\t\t\t<gx:ImageUrl>"+
                            "data:image/png;base64,"+ ImageUtils.imageToBase64(Uri.parse(imageUri), c)
                            +"</gx:ImageUrl>")
                    .append("\n\t\t</gx:Image>");
                }


                xmlStringBuilder.append("\n\t</gx:Carousel>")
                .append("\n\t<Point>")
                .append("\n\t\t<coordinates>"+longi+","+lat+",430.6608048569743</coordinates>")
                .append("\n\t</Point>")
                .append("\n</Placemark>");

        return xmlStringBuilder.toString();
    }
}