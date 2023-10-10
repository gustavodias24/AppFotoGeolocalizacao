package benicio.soluces.aplicativotestebencio.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import benicio.soluces.aplicativotestebencio.databinding.ActivityConfigutacoesBinding;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;

public class ConfigutacoesActivity extends AppCompatActivity {

    ActivityConfigutacoesBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final int REQUEST_IMAGE_LOGO = 1000;
    private String operador;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfigutacoesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("configPreferences", Context.MODE_PRIVATE);
        editor = preferences.edit();


        binding.textInfosContato.setOnClickListener( view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sinapsessolutions.com.br")));
        });

        operador = preferences.getString("operador", "");

        binding.nomeOperadorField.getEditText().setText(operador);

        if ( preferences.getString("logoImage", null) != null){
            binding.logoImg.setVisibility(View.VISIBLE);
            byte[] decodedBytes = Base64.decode(preferences.getString("logoImage", null), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            binding.logoImg.setImageBitmap(decodedBitmap);
        }

        binding.tutorialBtn.setOnClickListener( tutorialView -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=n79nS_xtlI4")));
        });

        binding.logoBtn.setOnClickListener( logoView ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_LOGO);
        });

        binding.salvarBtn.setOnClickListener( view -> {

            String nomeOperador = binding.nomeOperadorField.getEditText().getText().toString();
            if ( !nomeOperador.isEmpty() ){
                editor.putString("operador", nomeOperador);
                editor.apply();
                Toast.makeText(this, "Informações salvas", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Preencha as Informações", Toast.LENGTH_SHORT).show();
            }

        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Configurações");

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ( item.getItemId() == android.R.id.home){
            Bundle b = getIntent().getExtras();
            Boolean vindoDaPrimeira = false;
            if ( b != null){
                vindoDaPrimeira = b.getBoolean("vindoDaPrimeira", false);
            }
            if ( !vindoDaPrimeira ){
                startActivity(new Intent(getApplicationContext(), MapaActivity.class));
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}