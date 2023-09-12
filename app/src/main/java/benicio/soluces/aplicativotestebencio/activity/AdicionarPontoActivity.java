package benicio.soluces.aplicativotestebencio.activity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import benicio.soluces.aplicativotestebencio.databinding.ActivityAdicionarPontoBinding;
import benicio.soluces.aplicativotestebencio.model.PontoModel;
import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.adapter.AdapterFotos;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;
import benicio.soluces.aplicativotestebencio.util.PontosUtils;

public class AdicionarPontoActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private Double latitude, longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<String> listaDeFotos = new ArrayList<>();
    private AdapterFotos adapterFotos;
    private RecyclerView recyclerFotos;

    private String operador;


    private ActivityAdicionarPontoBinding binding;

    private Bundle b;
    private Boolean modoExibicao = false;
    private int positionPonto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdicionarPontoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        operador = getSharedPreferences("configPreferences", MODE_PRIVATE).getString("operador", "");
        verificarPermissoes();
        criarRecyclerFotos();

        b = getIntent().getExtras();

        if ( b != null){
            modoExibicao = b.getBoolean("modoExibicao");
            positionPonto = b.getInt("position");
        }

        if ( modoExibicao ){
            PontoModel pontoModel = PontosUtils.loadList(getApplicationContext()).get(positionPonto);
            binding.criarPontoBtn.setVisibility(View.GONE);
            binding.cameraBtn.setVisibility(View.GONE);

            binding.obsField.getEditText().setEnabled(false);
            binding.menu.getEditText().setEnabled(false);

            binding.obsField.getEditText().setText(pontoModel.getObs());
            binding.menu.getEditText().setText(pontoModel.getCategoria());

            listaDeFotos.addAll(pontoModel.getImages());
            adapterFotos.notifyDataSetChanged();

        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Adicionar ponto");

        if ( !modoExibicao ){
            configurarMenuCategoria();
        }

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // Intervalo de atualização da localização em milissegundos (10 segundos)
        locationRequest.setFastestInterval(5000); // Intervalo mais rápido de atualização em milissegundos (5 segundos)

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }

        binding.cameraBtn.setOnClickListener( fotoView -> {
            Intent i  = new Intent(getApplicationContext(), ExibirActivity.class);
            i.putExtra("obs", binding.obsField.getEditText().getText().toString());
            i.putExtra("operador", operador);
            i.putExtra("tipo", 0);
            i.putExtra("lat", latitude);
            i.putExtra("long", longitude);
            startActivity(i);
        });

        binding.criarPontoBtn.setOnClickListener( view -> {
            String obs, categoria;

            obs = binding.obsField.getEditText().getText().toString();
            categoria = binding.menu.getEditText().getText().toString().isEmpty() ? "Não informado" : binding.menu.getEditText().getText().toString();

            if (!categoria.isEmpty()){
                List<PontoModel> listaAntiga = PontosUtils.loadList(getApplicationContext());
                listaAntiga.add(new PontoModel(listaDeFotos, categoria, obs, operador, latitude, longitude));
                PontosUtils.saveList(listaAntiga, getApplicationContext());
                Toast.makeText(this, "Ponto adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                
                irParaOmapaActivity();
            }else{
                Toast.makeText(this, "Preencha todas as informações", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void configurarMenuCategoria (){
        String[] items = {"poste", "árvore", "área de roçada", "subestação de energia", "erosão", "outros"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
        TextInputLayout categoriaMenu = binding.menu;

        AutoCompleteTextView autoCompleteTextView = categoriaMenu.findViewById(R.id.auto_complete);

        autoCompleteTextView.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        if ( !modoExibicao ){
            listaDeFotos.clear();
            for ( String imageString : ImageUtils.loadList(getApplicationContext())){
                listaDeFotos.add(imageString);
            }
            adapterFotos.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ( item.getItemId() == android.R.id.home){
            if ( !modoExibicao ){
                irParaOmapaActivity();
            }else{
                startActivity(new Intent(getApplicationContext(), MeusPontosActivity.class));
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void irParaOmapaActivity(){
        startActivity(new Intent(getApplicationContext(), MapaActivity.class));
        ImageUtils.saveList(getApplicationContext(), new ArrayList<>());
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSIONS_REQUEST_LOCATION  && resultCode == RESULT_OK) {
           startLocationUpdates();
        }
    }

    public void verificarPermissoes(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AdicionarPontoActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(AdicionarPontoActivity.this, new String[] {Manifest.permission.CAMERA}, 1);
            ActivityCompat.requestPermissions(AdicionarPontoActivity.this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(AdicionarPontoActivity.this, new String[] {Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            return;
        }

    }

    public void criarRecyclerFotos(){
        recyclerFotos = binding.recyclerFotos;
        recyclerFotos.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFotos.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.HORIZONTAL));
        recyclerFotos.setHasFixedSize(true);
        adapterFotos = new AdapterFotos(listaDeFotos, getApplicationContext(), AdicionarPontoActivity.this);
        recyclerFotos.setAdapter(adapterFotos);
    }

}