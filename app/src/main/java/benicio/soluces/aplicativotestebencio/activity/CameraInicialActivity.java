package benicio.soluces.aplicativotestebencio.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.picasso.Callback;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.databinding.ActivityCameraInicialBinding;
import benicio.soluces.aplicativotestebencio.model.BodyModelValidarion;
import benicio.soluces.aplicativotestebencio.model.ValidationModel;
import benicio.soluces.aplicativotestebencio.service.ServiceValidationVersion;
import benicio.soluces.aplicativotestebencio.util.RetrofitUtils;
import retrofit2.Call;
import retrofit2.Response;

public class CameraInicialActivity extends AppCompatActivity {
    private static final int VERSION_APP = 1;
    private ServiceValidationVersion serviceValidationVersion;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private PreviewView previewView;
    private static final int PERMISSIONS_GERAL = 1;
    private Double latitude, longitude;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private  final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if ( result ){
                startCamera(cameraFacing);
            }
        }
    });
    private ActivityCameraInicialBinding activityBinding;
    private SharedPreferences preferences;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityBinding = ActivityCameraInicialBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        serviceValidationVersion = RetrofitUtils.createServiceValidationVersion(RetrofitUtils.createRetrofitValidationVersion());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        preferences = getSharedPreferences("configPreferences", Context.MODE_PRIVATE);

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
                    configurarTextInfos();
                }
            }
        };

        previewView = activityBinding.cameraPreview;

        activityBinding.flipcam.setOnClickListener( view -> {
            if ( cameraFacing == CameraSelector.LENS_FACING_BACK){
                cameraFacing = CameraSelector.LENS_FACING_FRONT;
            }else{
                cameraFacing = CameraSelector.LENS_FACING_BACK;
            }
            startCamera(cameraFacing);
        });

        activityBinding.map.setOnClickListener( view -> {
            startActivity(new Intent(getApplicationContext(), MapaActivity.class));
        });

        activityBinding.configs.setOnClickListener( view -> {
            Intent i = new Intent(getApplicationContext(), ConfigutacoesActivity.class);
            i.putExtra("vindoDaPrimeira", true);
            startActivity(i);
        });


        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates();
            startCamera(cameraFacing);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, PERMISSIONS_GERAL);
        }

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    public void startCamera(int cameraFacing) {
        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                if ( !this.isDestroyed() ){
                    Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                }

                activityBinding.capture.setOnClickListener( view -> {

                    if (ContextCompat.checkSelfPermission(CameraInicialActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    } else {
                        takePrint(imageCapture);
                    }
                });


                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    public void takePrint(ImageCapture imageCapture){
        activityBinding.configs.setVisibility(View.GONE);
        activityBinding.map.setVisibility(View.INVISIBLE);
        activityBinding.flipcam.setVisibility(View.GONE);
        activityBinding.capture.setVisibility(View.GONE);

        activityBinding.imagePreview.setVisibility(View.VISIBLE);

        File documentosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        File fotoMapaDir = new File(documentosDir, "FOTO MAPA");
        if (!fotoMapaDir.exists()) {
            fotoMapaDir.mkdirs();
        }

        File partesDir = new File(fotoMapaDir, "PARTES");

        if ( !partesDir.exists()){
            partesDir.mkdirs();
        }

        final File file = new File(partesDir, System.currentTimeMillis() + ".png");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> {
                    Picasso.get().load(file).into(activityBinding.imagePreview, new Callback() {
                        @Override
                        public void onSuccess() {
                            try {
                                // create bitmap screen capture
                                View v1 = getWindow().getDecorView().getRootView().findViewById(R.id.maconha);
                                v1.setDrawingCacheEnabled(true);
                                Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                                v1.setDrawingCacheEnabled(false);

                                File documentosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                                File fotoMapaDir = new File(documentosDir, "FOTO MAPA");

                                if (!fotoMapaDir.exists()) {
                                    fotoMapaDir.mkdirs();
                                }

                                File partesDir = new File(fotoMapaDir, "PARTES");

                                if ( !partesDir.exists()){
                                    partesDir.mkdirs();
                                }

                                File imageFile = new File(partesDir, UUID.randomUUID().toString() + ".png");

                                FileOutputStream outputStream = new FileOutputStream(imageFile);
                                int quality = 70;

                                bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);

                                outputStream.flush();
                                outputStream.close();

                                Toast.makeText(CameraInicialActivity.this, "Salvo em Documents/FOTO MAPA", Toast.LENGTH_SHORT).show();

                                activityBinding.configs.setVisibility(View.VISIBLE);
                                activityBinding.map.setVisibility(View.VISIBLE);
                                activityBinding.flipcam.setVisibility(View.VISIBLE);
                                activityBinding.capture.setVisibility(View.VISIBLE);
                                startCamera(cameraFacing);
                                baterPrintDenovo();
                            } catch (Throwable e) {

                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(CameraInicialActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            activityBinding.imagePreview.setVisibility(View.GONE);
                        }
                    });
                });
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(CameraInicialActivity.this, "Erro: "+ exception.getMessage(), Toast.LENGTH_SHORT).show());
                startCamera(cameraFacing);
            }
        });
    }
//    public void takePicture( ImageCapture imageCapture){
//        final File file = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".png");
//        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
//        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
//            @Override
//            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(CameraInicialActivity.this, "Imagem salva: " + file.getPath(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                startCamera(cameraFacing);
//            }
//
//            @Override
//            public void onError(@NonNull ImageCaptureException exception) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(CameraInicialActivity.this, "Erro: "+ exception.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                startCamera(cameraFacing);
//            }
//        });
//    }
    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }
    @SuppressLint("SetTextI18n")
    private void configurarTextInfos(){
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        // Formatar a data e a hora
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        String formattedDate = dateFormat.format(currentDate);
        String formattedTime = timeFormat.format(currentDate);

        String operador;

        String nomeOperador = getSharedPreferences("configPreferences", MODE_PRIVATE).getString("operador", "");
        operador = Objects.requireNonNull(nomeOperador.isEmpty() ? "Nome não informado." : nomeOperador);


        @SuppressLint("DefaultLocale") String cordenadas = String.format("Lat: %f Long: %f", latitude, longitude);

        activityBinding.infos.setText(
                String.format("%s ás %s", formattedDate, formattedTime) + "\n" +
                        cordenadas + "\n" +
                        "Operador: " + operador
        );


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {

            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0).replace(",", "\n");
                activityBinding.infos.setText(
                        String.format("%s ás %s", formattedDate, formattedTime) + "\n" +
                                cordenadas + "\n" +
                                fullAddress + "\n" +
                                "Operador: " + operador
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_GERAL) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            // Se todas as permissões foram concedidas, inicie as operações que requerem permissões
            if (allPermissionsGranted) {
                startLocationUpdates();
                startCamera(cameraFacing);
            } else {
                // Se o usuário recusar alguma permissão, exiba uma mensagem informando a necessidade das permissões
                Toast.makeText(this, "PERMISSÃO NEGADA", Toast.LENGTH_SHORT).show();
                finish();
            }
    }}

    public  void baterPrintDenovo (){
        try {

            if ( preferences.getString("logoImage", null) != null){
                activityBinding.logoEmpresa.setVisibility(View.VISIBLE);
                byte[] decodedBytes = Base64.decode(preferences.getString("logoImage", null), Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                activityBinding.logoEmpresa.setImageBitmap(decodedBitmap);
            }

            activityBinding.configs.setVisibility(View.GONE);
            activityBinding.map.setVisibility(View.INVISIBLE);
            activityBinding.flipcam.setVisibility(View.GONE);
            activityBinding.capture.setVisibility(View.GONE);

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView().findViewById(R.id.maconha);
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File documentosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

            File fotoMapaDir = new File(documentosDir, "FOTO MAPA");
            if (!fotoMapaDir.exists()) {
                fotoMapaDir.mkdirs();
            }

            File imageFile = new File(fotoMapaDir, UUID.randomUUID().toString() + ".png");

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 70;

            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);

            outputStream.flush();
            outputStream.close();

            activityBinding.imagePreview.setVisibility(View.GONE);

            activityBinding.configs.setVisibility(View.VISIBLE);
            activityBinding.map.setVisibility(View.VISIBLE);
            activityBinding.flipcam.setVisibility(View.VISIBLE);
            activityBinding.capture.setVisibility(View.VISIBLE);
            activityBinding.logoEmpresa.setVisibility(View.GONE);
            startCamera(cameraFacing);

            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(CameraInicialActivity.this),
                    "benicio.soluces.aplicativotestebencio.provider", imageFile);

            Intent viewImageIntent = new Intent(Intent.ACTION_VIEW);
            viewImageIntent.setDataAndType(uri, "image/*");
            viewImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(viewImageIntent);

//            Intent i = new Intent(Intent.ACTION_SEND);
//            i.setType("image/*");
//            i.putExtra(Intent.EXTRA_STREAM, uri);
//            i.putExtra(Intent.EXTRA_TEXT, "Veja essa imagem que eu tirei utilizando o software FOTO MAPA da empresa Sinapses!");
//            startActivity(Intent.createChooser(i, "Compartilhar via"));

        } catch (Throwable e) {
            Log.d("baterPrintDenovo:",  e.getMessage());
        }
    }

    public void validarVersion(){
        serviceValidationVersion.validarVersion(new BodyModelValidarion(VERSION_APP)).enqueue(new retrofit2.Callback<ValidationModel>() {
            @Override
            public void onResponse(Call<ValidationModel> call, Response<ValidationModel> response) {
                if ( response.isSuccessful() ){
                    ValidationModel validation = response.body();
                    if( !validation.getSuccess() ){
                        AlertDialog.Builder b = new AlertDialog.Builder(CameraInicialActivity.this);
                        b.setTitle("AVISO!");
                        b.setCancelable(false);
                        b.setMessage("Seu app está desatualizado, atualize ele na PlayStore antes de usar novamente. Obrigado!");
                        b.setPositiveButton("OK", (dialogInterface, i) -> {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/")));
                        });
                        b.create().show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ValidationModel> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        validarVersion();
    }
}