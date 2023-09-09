package benicio.soluces.aplicativotestebencio.activity;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.databinding.ActivityExibirBinding;
import benicio.soluces.aplicativotestebencio.util.ImageUtils;

public class ExibirActivity extends AppCompatActivity{
    private Intent i;
    private static final int PERMISSON_CODE = 1000;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_WRITE_STORAGE = 2;
    private Dialog dialog_foto;
    private Uri imageUri;
    String formattedDate;
    String formattedTime;
    private ActivityExibirBinding binding;
    private SharedPreferences preferences;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExibirBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        i = getIntent();

        preferences = getSharedPreferences("logoPreferences", Context.MODE_PRIVATE);

        if ( preferences.getString("logoImage", null) == null){
            binding.logo.setVisibility(View.GONE);
            Picasso.get().load(R.raw.logo).into(binding.logo);
        }else{
            binding.logo.setVisibility(View.VISIBLE);
            byte[] decodedBytes = Base64.decode(preferences.getString("logoImage", null), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            binding.logo.setImageBitmap(decodedBitmap);
        }

        configurarTextView();

        binding.imageView.setOnClickListener( tirarOutraFoto -> {
            AlertDialog.Builder  b = new AlertDialog.Builder(ExibirActivity.this);
            b.setMessage("Você quer bater outra foto?");
            b.setPositiveButton("Sim", (dialogInterface, i) -> {
                baterFoto();
                dialog_foto.dismiss();
            });
            b.setNegativeButton("Não", null);
            dialog_foto = b.create();
            dialog_foto.show();
        });
        baterFoto();

        binding.prosseguirFab.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            }else{
                binding.prosseguirFab.setVisibility(View.GONE);

                // Capture a tela como um bitmap
                try {
                    // create bitmap screen capture
                    View v1 = getWindow().getDecorView().getRootView();
                    v1.setDrawingCacheEnabled(true);
                    Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                    v1.setDrawingCacheEnabled(false);

                    File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File imageFile = new File(externalFilesDir, UUID.randomUUID().toString() + ".jpg");

                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    int quality = 100;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    List<String> listaAntiga = ImageUtils.loadList(getApplicationContext());
                    listaAntiga.add(Uri.fromFile(imageFile).toString());
                    ImageUtils.saveList(getApplicationContext(), listaAntiga);

                } catch (Throwable e) {
                    // Several error may come out with file handling or DOM
                    Log.d("bucetuda", "onCreate: " + e.getMessage());
                }
                binding.prosseguirFab.setVisibility(View.VISIBLE);
                finish();
            }

        } );
        configurarTextInfos();
    }

    @SuppressLint("SetTextI18n")
    private void configurarTextInfos(){
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Formatar a data e a hora
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        formattedDate = dateFormat.format(currentDate);
        formattedTime = timeFormat.format(currentDate);

        // operador e obs

        String operador, obs;
        Double latitude, longitude;

        latitude = i.getDoubleExtra("lat", 0);
        longitude = i.getDoubleExtra("long", 0);

        operador = Objects.requireNonNull(i.getStringExtra("operador")).isEmpty() ? "Nome não informado." : i.getStringExtra("operador");
        obs = Objects.requireNonNull(i.getStringExtra("obs")).isEmpty() ? "Sem observações." : i.getStringExtra("obs");


        @SuppressLint("DefaultLocale") String cordenadas = String.format("Lat: %f Long: %f", latitude, longitude);

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {

            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                binding.dados.setText(
                        String.format("%s ás %s", formattedDate, formattedTime) + "\n" +
                                cordenadas + "\n" +
                                fullAddress + "\n" +
                                "Operador: " + operador + "\n" +
                                "Observações: " + obs
                );
                Log.d("Address", fullAddress);
            } else {
                Log.d("Address", "No address found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            binding.imageView.setImageURI(imageUri);

//            Bundle extras = data.getExtras();
//            if (extras != null) {
//                Bitmap imageBitmap = (Bitmap) extras.get("data");
//                binding.imageView.setImageBitmap(imageBitmap);
//            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ( requestCode == PERMISSON_CODE){
            if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            }else{
                Toast.makeText(this, "Conceda as permissões!", Toast.LENGTH_SHORT).show();
            }
        }
//        if (requestCode == REQUEST_IMAGE_CAPTURE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                dispatchTakePictureIntent();
//            } else {
//                Toast.makeText(this, "Pemissão de câmera necessária.", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }



    public void configurarTextView(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ExibirActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(ExibirActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            ActivityCompat.requestPermissions(ExibirActivity.this, new String[] {Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            return;
        }


    }

    public void baterFoto(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if ( checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSON_CODE);
            }
            else {
                // already permisson
                openCamera();
            }
        }
        else{
            // system < M
            openCamera();
        }
    }

    public void openCamera(){
        ContentValues values  = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "nova picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Imagem tirada da câmera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intentCamera =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intentCamera, REQUEST_IMAGE_CAPTURE);
    }

}