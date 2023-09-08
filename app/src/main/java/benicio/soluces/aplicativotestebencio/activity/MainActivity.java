package benicio.soluces.aplicativotestebencio.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import benicio.soluces.aplicativotestebencio.adapter.AdapterFotos;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMainBinding;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;

public class MainActivity extends AppCompatActivity {

    private List<Uri> listaDeFotos = new ArrayList<>();
    private AdapterFotos adapterFotos;
    private RecyclerView recyclerFotos;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final int REQUEST_IMAGE_LOGO = 1000;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        preferences = getSharedPreferences("logoPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();

        if ( preferences.getString("logoImage", null) != null){
            binding.logoImg.setVisibility(View.VISIBLE);
            byte[] decodedBytes = Base64.decode(preferences.getString("logoImage", null), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            binding.logoImg.setImageBitmap(decodedBitmap);
        }
        binding.cameraBtn.setOnClickListener( fotoView -> {
            Intent i  = new Intent(getApplicationContext(), ExibirActivity.class);
            i.putExtra("operador", binding.nomeOperadorField.getEditText().getText().toString());
            i.putExtra("obs", binding.obsField.getEditText().getText().toString());
            i.putExtra("tipo", 0);
            startActivity(i);
        });

        binding.tutorialBtn.setOnClickListener( tutorialView -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=n79nS_xtlI4")));
        });

        binding.logoBtn.setOnClickListener( logoView ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_LOGO);
        });

        verificarPermissoes();
        criarRecyclerFotos();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



    }

    @Override
    protected void onResume() {
        super.onResume();

        listaDeFotos.clear();
        for ( String imageString : ImageUtils.loadList(getApplicationContext())){
            listaDeFotos.add(Uri.parse(imageString));
        }
        adapterFotos.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ( item.getItemId() == android.R.id.home){
            startActivity(new Intent(getApplicationContext(), MapaActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_LOGO && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            editor.putString("logoImage", ImageUtils.imageToBase64(selectedImageUri, getApplicationContext()));
            editor.apply();


            binding.logoImg.setVisibility(View.VISIBLE);
            byte[] decodedBytes = Base64.decode(preferences.getString("logoImage", null), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            binding.logoImg.setImageBitmap(decodedBitmap);
        }
    }

    public void verificarPermissoes(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            return;
        }

    }

    public void criarRecyclerFotos(){
        recyclerFotos = binding.recyclerFotos;
        recyclerFotos.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFotos.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.HORIZONTAL));
        recyclerFotos.setHasFixedSize(true);
        adapterFotos = new AdapterFotos(listaDeFotos, getApplicationContext(), MainActivity.this);
        recyclerFotos.setAdapter(adapterFotos);
    }

}