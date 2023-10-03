package benicio.soluces.aplicativotestebencio.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMapaBinding;
import benicio.soluces.aplicativotestebencio.model.PontoModel;
import benicio.soluces.aplicativotestebencio.model.ProjetoModel;
import benicio.soluces.aplicativotestebencio.service.ServiceNotificacoes;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;
import benicio.soluces.aplicativotestebencio.util.ProjetoUtils;
import benicio.soluces.aplicativotestebencio.util.RetrofitUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapaActivity extends AppCompatActivity {
    private ItemizedOverlayWithFocus<OverlayItem> mOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int PERMISSIONS_REQUEST_LOCATION = 101;
    private ActivityMapaBinding vbinding;
    private MapView map = null;
    private MapController mapController;
    double latitude, longitude;
    private FloatingActionButton fab_onde_estou;
    private Dialog dialogCarregamento;
    private Bundle bundle;
    private SharedPreferences sharedPreferencesNovidade;
    private SharedPreferences.Editor editorNovidade;

    private SharedPreferences preferecesFirts;
    private SharedPreferences.Editor editorFirts;
    private int qtdNovidadeNova = 0 ;
    private Retrofit retrofit;
    private ServiceNotificacoes serviceNotificacao;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vbinding = ActivityMapaBinding.inflate(getLayoutInflater());
        setContentView(vbinding.getRoot());

        vbinding.menuOpcoes.open(true);

        preferecesFirts = getSharedPreferences("preferecesFirts", MODE_PRIVATE);
        editorFirts = preferecesFirts.edit();

        if ( preferecesFirts.getBoolean("first", true) ){
            AlertDialog.Builder d = new AlertDialog.Builder(MapaActivity.this);
            d.setTitle("Bem-vindo!");
            d.setCancelable(false);
            d.setMessage("Crie um projeto para inserir pontos no mapa com fotos e observações, se quiser pode exportar o projeto para pdf ou kmz/kml!");
            d.setPositiveButton("OK", (dialogInterface, i) -> editorFirts.putBoolean("first", false).apply());
            d.create().show();

        }

        configurarRetrofit();
        bundle = getIntent().getExtras();

        sharedPreferencesNovidade = getSharedPreferences("novidades", MODE_PRIVATE);
        editorNovidade = sharedPreferencesNovidade.edit();

        vbinding.fabNoficacoes.setOnClickListener( view -> {
            editorNovidade.putInt("qtdNovidades", qtdNovidadeNova);
            editorNovidade.apply();
            Intent i = new Intent(getApplicationContext(), NotificacoesActivity.class);
            startActivity(i);
        });

        dialogCarregamento = RetrofitUtils.criarDialogCarregando(this, this);
        dialogCarregamento.show();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // Intervalo de atualização da localização em milissegundos (10 segundos)
        locationRequest.setFastestInterval(1000); // Intervalo mais rápido de atualização em milissegundos (5 segundos)

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    mapController.setZoom(18.5);
                    mapController.animateTo( new GeoPoint(latitude, longitude) );
                    dialogCarregamento.dismiss();
                    stopLocationUpdates();
                    createPointer();
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        configurarMap();

        fab_onde_estou.setOnClickListener(btn -> {
            startLocationUpdates();
        });
        vbinding.fotoFab.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), AdicionarPontoActivity.class);
            if ( bundle != null){
                i.putExtra("idProjeto", bundle.getString("idProjeto"));
            }
            startActivity(i);
            finish();
        });
        vbinding.fabConfigs.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(getApplicationContext(), ConfigutacoesActivity.class));
        });

        // se o bundle não for null ele vem de outro activity
        if(bundle != null){

            vbinding.fabMeusPontos.setVisibility(View.VISIBLE);
            vbinding.fotoFab.setVisibility(View.VISIBLE);
            vbinding.fabMeusPontos.setOnClickListener(view -> {
                Intent i = new Intent(getApplicationContext(), MeusPontosActivity.class);
                i.putExtra("idProjeto", bundle.getString("idProjeto"));
                startActivity(i);
                finish();
            });

            criarPontosDoProejeto(bundle.getString("idProjeto"));

        }

        vbinding.fabMeusProjetos.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(getApplicationContext(), MeusProjetosActivity.class));
        });


    }
    private  void criarPontosDoProejeto(String id){
        List<PontoModel> listaPontosDoProjeto = new ArrayList<>();
        for (ProjetoModel p : ProjetoUtils.loadList(getApplicationContext())){
            if ( p.getIdProjeto().equals( id)){
                listaPontosDoProjeto.addAll(p.getListaDePontos());
                break;
            }
        }

        if ( !listaPontosDoProjeto.isEmpty() ){
            ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

            for (PontoModel ponto : listaPontosDoProjeto){
                items.add(new OverlayItem(ponto.getCategoria(), ponto.getObs(), new GeoPoint(ponto.getLatitude(), ponto.getLongitude())));
            }

            for (OverlayItem item : items) {
                if ( !item.getTitle().equals("Você")){
                    item.setMarker(ImageUtils.getIconeDoPonto(item.getTitle(), 32 , 32, getApplicationContext()));
                }
            }
            ItemizedOverlayWithFocus<OverlayItem> overlayItensProjeto = new ItemizedOverlayWithFocus<>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index, final OverlayItem item) {
                            return false;
                        }
                    }, this);

            overlayItensProjeto.setFocusItemsOnTap(true);
            map.getOverlays().add(overlayItensProjeto);
            map.getOverlays().add(overlayItensProjeto);
        }
    }
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void startLocationUpdates() {
        dialogCarregamento.show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void configurarMap(){
        map = vbinding.map;
        fab_onde_estou = findViewById(R.id.fab_onde_estou);

        map.setTileSource(TileSourceFactory.MAPNIK);

        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // add compass
        CompassOverlay compass = new CompassOverlay(this, new InternalCompassOrientationProvider(this), map);
        compass.enableCompass();
        map.getOverlays().add(compass);

        // add rotation
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);

        // config mapController
        mapController = (MapController) map.getController();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        verificarNovidades();
    }

    public void configurarRetrofit(){

        retrofit = new Retrofit.Builder()
                .baseUrl("https://comunicao-clientes-kaizen.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        serviceNotificacao = retrofit.create(ServiceNotificacoes.class);

    }
    public void verificarNovidades(){
        serviceNotificacao.recuperaQtdPostagem().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if ( response.isSuccessful() ){
                    int qtdNovidadesAtual = sharedPreferencesNovidade.getInt("qtdNovidades", 0 );
                    qtdNovidadeNova = Integer.parseInt(response.body());

                    int count = qtdNovidadeNova - qtdNovidadesAtual;
                    if ( count < 0 ){
                        editorNovidade.putInt("qtdNovidades", qtdNovidadeNova );
                        editorNovidade.apply();
                        count = 0;
                    }
                    vbinding.fabNoficacoes.setCount(
                            count
                    );

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MapaActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    private void createPointer() {

        // Verifique se o overlay anterior existe e remova-o
        if (mOverlay != null) {
            map.getOverlays().remove(mOverlay);
        }

        // Crie um novo overlay com os novos pontos
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Você", "Você esta aqui!", new GeoPoint(latitude, longitude)));




        mOverlay = new ItemizedOverlayWithFocus<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, this);

        mOverlay.setFocusItemsOnTap(true);
        map.getOverlays().add(mOverlay);
    }
}