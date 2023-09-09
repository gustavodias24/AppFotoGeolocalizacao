package benicio.soluces.aplicativotestebencio.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.location.FusedLocationProviderClient;
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

import benicio.soluces.aplicativotestebencio.model.PontoModel;
import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMapaBinding;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;
import benicio.soluces.aplicativotestebencio.util.PontosUtils;

public class MapaActivity extends AppCompatActivity {
    private ItemizedOverlayWithFocus<OverlayItem> mOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int PERMISSIONS_REQUEST_LOCATION = 101;
    private ActivityMapaBinding vbinding;
    private MapView map = null;
    private MapController mapController;
    double latitude, longitude;
    private FloatingActionButton fab_onde_estou;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vbinding = ActivityMapaBinding.inflate(getLayoutInflater());
        setContentView(vbinding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocAtt();

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        configurarMap();

        fab_onde_estou.setOnClickListener( btn ->{
            getLocAtt();
        });
        vbinding.fotoFab.setOnClickListener( view -> {
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        });
        vbinding.fabMeusPontos.setOnClickListener( view -> {
            finish();
            startActivity(new Intent(getApplicationContext(), MeusPontosActivity.class));
        });
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
    private void getLocAtt(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            requestLocation();
        }
    }
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        mapController.setZoom(18.5);
                        mapController.animateTo( new GeoPoint(latitude, longitude) );
                        createPointer();
                        Log.d("bucetinha", String.format("Lat: %f\nLong: %f", latitude, longitude));
                    }
                });
    }
    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
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

        for (PontoModel ponto : PontosUtils.loadList(getApplicationContext())){
            items.add(new OverlayItem(ponto.getCategoria(), ponto.getObs(), new GeoPoint(ponto.getLatitude(), ponto.getLongitude())));
        }

        for (OverlayItem item : items) {
            if ( !item.getTitle().equals("Você")){
                item.setMarker(ImageUtils.getIconeDoPonto(item.getTitle(), 32 , 32, getApplicationContext()));
            }
        }


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