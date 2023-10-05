package benicio.soluces.aplicativotestebencio.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import benicio.soluces.aplicativotestebencio.adapter.AdapterPostagem;
import benicio.soluces.aplicativotestebencio.databinding.ActivityNotificacoesBinding;
import benicio.soluces.aplicativotestebencio.service.ServiceNotificacoes;
import benicio.soluces.aplicativotestebencio.util.PostagemModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificacoesActivity extends AppCompatActivity {

    private ActivityNotificacoesBinding notificacoesBinding;
//    private Dialog dialogCarregando;

    private RecyclerView r;
    private AdapterPostagem adapter;

    private List<PostagemModel> lista = new ArrayList<>();

    private Retrofit retrofit;
    private ServiceNotificacoes service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificacoesBinding = ActivityNotificacoesBinding.inflate(getLayoutInflater());
        setContentView(notificacoesBinding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

//        dialogCarregando = RetrofitUtils.criarDialogCarregando(this, this);

        r = notificacoesBinding.recyclerPostagens;
        r.setHasFixedSize(true);
        r.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        r.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        adapter = new AdapterPostagem(lista, getApplicationContext());
        r.setAdapter(adapter);

        configurarRetrofit();

        getSupportActionBar().setTitle("Novidades");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void configurarRetrofit(){

        retrofit = new Retrofit.Builder()
                .baseUrl("https://comunicao-clientes-kaizen.vercel.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ServiceNotificacoes.class);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ( item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        carregarPostagem();
    }

    public void carregarPostagem(){
        lista.clear();
//        dialogCarregando.show();
        service.recuperarPostagens().enqueue(new Callback<List<PostagemModel>>() {
            @Override
            public void onResponse(Call<List<PostagemModel>> call, Response<List<PostagemModel>> response) {
//                dialogCarregando.dismiss();
                if ( response.isSuccessful() ){
                    lista.addAll(response.body());
                    Collections.reverse(lista);
                    adapter.notifyDataSetChanged();

                    if ( lista.size() > 0 ){
                        notificacoesBinding.avisoEmptyText.setVisibility(View.GONE);
                    }
                }else{
                    Log.d("bucetinha",  response.message());
                    Toast.makeText(NotificacoesActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<PostagemModel>> call, Throwable t) {
                Toast.makeText(NotificacoesActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("bucetinha",  t.getMessage());
//                dialogCarregando.dismiss();
            }
        });
    }
}