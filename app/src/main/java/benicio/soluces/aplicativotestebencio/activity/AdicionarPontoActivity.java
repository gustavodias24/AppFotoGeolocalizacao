package benicio.soluces.aplicativotestebencio.activity;

//salvar na pasta do programa

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import benicio.soluces.aplicativotestebencio.adapter.AdapterCategorias;
import benicio.soluces.aplicativotestebencio.adapter.AdapterFotos;
import benicio.soluces.aplicativotestebencio.adapter.AdapterProjetos;
import benicio.soluces.aplicativotestebencio.databinding.ActivityAdicionarPontoBinding;
import benicio.soluces.aplicativotestebencio.databinding.LayoutRecyclerBinding;
import benicio.soluces.aplicativotestebencio.model.CategoriaModel;
import benicio.soluces.aplicativotestebencio.model.PontoModel;
import benicio.soluces.aplicativotestebencio.model.ProjetoModel;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;
import benicio.soluces.aplicativotestebencio.util.ProjetoUtils;
import benicio.soluces.aplicativotestebencio.util.RecyclerItemClickListener;

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

    private Dialog verProjetosDialog;
    private ActivityAdicionarPontoBinding binding;

    private Bundle b;
    private Boolean modoExibicao = false;
    private int positionPonto;
    private String idProjeto;
    private String nomeProjeto = "";
    private String categoriaEscolhida = "";

    private ProjetoModel projetoModel;

    private RecyclerView recyclerCategoria;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdicionarPontoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        verProjetosDialog = criarDialogVerProjetos();

//        binding.escolherProjeto.setOnClickListener( view -> {
//            verProjetosDialog.show();
//        });

        binding.prosseguirBtn.setOnClickListener( view -> {
            binding.layoutFoto.setVisibility(View.GONE);
            binding.layoutCategoria.setVisibility(View.VISIBLE);
        });

        operador = getSharedPreferences("configPreferences", MODE_PRIVATE).getString("operador", "");
        verificarPermissoes();
        criarRecyclerFotos();

        b = getIntent().getExtras();

        if ( b != null){
            modoExibicao = b.getBoolean("modoExibicao");
            positionPonto = b.getInt("position");
            idProjeto = b.getString("idProjeto");
            projetoModel = ProjetoUtils.getProjetoModel(idProjeto, getApplicationContext());
            nomeProjeto = projetoModel.getNomeProjeto();
            binding.nomeProjetoText.setText(
                    "Categoria do Ponto: " + categoriaEscolhida
                    + "\n" +
                    "Projeto selecionado: " + nomeProjeto);
        }

        if ( modoExibicao ){
            PontoModel pontoModel = projetoModel.getListaDePontos().get(positionPonto);
            getSupportActionBar().setTitle(projetoModel.getNomeProjeto());

            binding.criarPontoBtn.setVisibility(View.GONE);
            binding.cameraBtn.setVisibility(View.GONE);
            binding.nomeProjetoText.setVisibility(View.GONE);
            binding.prosseguirBtn.setVisibility(View.GONE);
            binding.layoutCategoria.setVisibility(View.GONE);

            binding.layoutDados.setVisibility(View.VISIBLE);
            binding.layoutFoto.setVisibility(View.VISIBLE);
            binding.verNoMapaBtn.setVisibility(View.VISIBLE);

            binding.obsField.getEditText().setEnabled(false);
            binding.obsField.getEditText().setText(pontoModel.getObs());

            listaDeFotos.addAll(pontoModel.getImages());
            adapterFotos.notifyDataSetChanged();


            binding.verNoMapaBtn.setOnClickListener( view -> {
                Intent i = new Intent(getApplicationContext(), MapaActivity.class);
                i.putExtra("latPonto", pontoModel.getLatitude());
                i.putExtra("longPonto", pontoModel.getLongitude());
                i.putExtra("idProjeto", b.getString("idProjeto"));
                startActivity(i);
                finish();
            });

        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if ( !modoExibicao ){
//            configurarMenuCategoria();
            getSupportActionBar().setTitle("Adicionar ponto");
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
            String obs;

            obs = binding.obsField.getEditText().getText().toString().isEmpty() ? "Sem observações" : binding.obsField.getEditText().getText().toString();

            if ( !nomeProjeto.isEmpty() ){

                ProjetoModel projetoModel = null;
                List<ProjetoModel> listaAntiga = ProjetoUtils.loadList(getApplicationContext());

                int pos = 0;
                for ( ProjetoModel p : listaAntiga){
                    if (p.getNomeProjeto().equals(nomeProjeto)) {
                        projetoModel = p;
                        break;
                    }
                    pos++;
                }

                String data = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                assert projetoModel != null;
                projetoModel.getListaDePontos().add(
                        new PontoModel( data,listaDeFotos, categoriaEscolhida, obs, operador, latitude, longitude)
                );

                listaAntiga.remove(pos);
                listaAntiga.add(pos, projetoModel);

                if ( listaAntiga.size() >= 60){
                    AlertDialog.Builder bAvido = new AlertDialog.Builder(AdicionarPontoActivity.this);
                    bAvido.setTitle("AVISO!");
                    bAvido.setMessage("Você atingiu a quantidade máxima de pontos na versão grátis, atualize para versão PRO!");
                    bAvido.setPositiveButton("OK", null);
                    bAvido.create().show();
                }else{
                    ProjetoUtils.saveList(listaAntiga, getApplicationContext());

                    Toast.makeText(this, "Ponto adicionado com sucesso!", Toast.LENGTH_SHORT).show();

                    irParaOmapaActivity();
                }

            }else{
                Toast.makeText(this, "Escolha um projeto para esse ponto!", Toast.LENGTH_SHORT).show();
            }

        });

        configurarRecyclerCategoria();
    }

    public void configurarRecyclerCategoria(){
        recyclerCategoria = binding.recyclerCategoria;
        recyclerCategoria.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerCategoria.setHasFixedSize(true);
        recyclerCategoria.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        List<CategoriaModel> lista = new ArrayList<>();
        lista.add(new CategoriaModel("árvore", "Árvore"));
        lista.add(new CategoriaModel("ferrugem", "Ferrugem"));
        lista.add(new CategoriaModel("poste", "Poste"));
        lista.add(new CategoriaModel("área de roçada", "Área de roçada"));
        lista.add(new CategoriaModel("subestação de energia", "Subestação de energia"));
        lista.add(new CategoriaModel("erosão", "Erosão"));
        lista.add(new CategoriaModel("outros", "Outros"));
        recyclerCategoria.setAdapter( new AdapterCategorias(lista, getApplicationContext()));

        recyclerCategoria.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerCategoria,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemClick(View view, int position) {
                        categoriaEscolhida = lista.get(position).getCategoria();
                        binding.layoutCategoria.setVisibility(View.GONE);
                        binding.layoutDados.setVisibility(View.VISIBLE);

                        binding.nomeProjetoText.setText(
                                "Categoria do Ponto: " + categoriaEscolhida
                                        + "\n" +
                                        "Projeto selecionado: " + nomeProjeto);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

    }
    public Dialog criarDialogVerProjetos(){
        AlertDialog.Builder b = new AlertDialog.Builder(AdicionarPontoActivity.this);
        b.setMessage("Projetos disponíveis:");
        b.setNegativeButton("Fechar", null);
        LayoutRecyclerBinding projetosBinding = LayoutRecyclerBinding.inflate(getLayoutInflater());

        if ( !ProjetoUtils.loadList(getApplicationContext()).isEmpty() ) {
            projetosBinding.emptyWarningText.setVisibility(View.GONE);
            projetosBinding.addProjetoBtn.setVisibility(View.GONE);
        }

        projetosBinding.addProjetoBtn.setOnClickListener( view -> {
            startActivity(new Intent(getApplicationContext(), MeusProjetosActivity.class));
            finish();
        });

        RecyclerView recylerProjetos = projetosBinding.recycler;
        recylerProjetos.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recylerProjetos.setHasFixedSize(true);
        recylerProjetos.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        recylerProjetos.setAdapter(
                new AdapterProjetos(ProjetoUtils.loadList(getApplicationContext()), getApplicationContext(), this, false, false)
        );

        recylerProjetos.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recylerProjetos,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemClick(View view, int position) {
                        nomeProjeto = ProjetoUtils.loadList(getApplicationContext()).get(position).getNomeProjeto();

                        binding.nomeProjetoText.setText(
                                "Categoria do Ponto: " + categoriaEscolhida
                                + "\n" +
                                "Projeto selecionado: " + nomeProjeto);

                        verProjetosDialog.dismiss();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                }
        ));

        b.setView(projetosBinding.getRoot());
        return b.create();
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

//    private void configurarMenuCategoria (){
//        String[] items = {"poste", "árvore", "área de roçada", "subestação de energia", "erosão", "outros", "ferrugem"};
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
//
//        TextInputLayout categoriaMenu = binding.menu;
//
//        AutoCompleteTextView autoCompleteTextView = categoriaMenu.findViewById(R.id.auto_complete);
//
//        autoCompleteTextView.setAdapter(adapter);
//    }

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
                Intent i = new Intent(getApplicationContext(), MeusPontosActivity.class);
                i.putExtra("idProjeto", b.getString("idProjeto"));
                startActivity(i);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void irParaOmapaActivity(){
        Intent i = new Intent(getApplicationContext(), MapaActivity.class);
        if ( b != null){
            i.putExtra("idProjeto", b.getString("idProjeto"));
        }
        startActivity(i);
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