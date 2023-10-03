package benicio.soluces.aplicativotestebencio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    // Tempo de exibição da splash screen em milissegundos
    private static final int SPLASH_SCREEN_TIMEOUT = 3000;
    private ActivitySplashBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.splashscreen;
        binding.videoView.setVideoPath(videoPath);
        binding.videoView.start();

        // Define um Handler para atrasar a transição para a próxima atividade
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent para iniciar a próxima atividade após o tempo de exibição da splash screen
                Intent intent = new Intent(SplashActivity.this, CameraInicialActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN_TIMEOUT);

    }
}