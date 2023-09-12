package benicio.soluces.aplicativotestebencio.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import benicio.soluces.aplicativotestebencio.adapter.AdapterPontos;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMeusPontosBinding;
import benicio.soluces.aplicativotestebencio.util.PontosUtils;
import benicio.soluces.aplicativotestebencio.util.RecyclerItemClickListener;

public class MeusPontosActivity extends AppCompatActivity {
    private ActivityMeusPontosBinding vbindig;
    private RecyclerView recyclerPontos;
    private AdapterPontos adapterPontos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vbindig = ActivityMeusPontosBinding.inflate(getLayoutInflater());
        setContentView(vbindig.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        configurarRecyclerPontos();
        configurarListenerRecycler();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Pontos salvos");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if( item.getItemId() == android.R.id.home){
            startActivity(new Intent(getApplicationContext(), MapaActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void configurarRecyclerPontos(){
        recyclerPontos = vbindig.recyclerPontos;
        recyclerPontos.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerPontos.setHasFixedSize(true);
        recyclerPontos.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        adapterPontos = new AdapterPontos(PontosUtils.loadList(getApplicationContext()), getApplicationContext());
        recyclerPontos.setAdapter(adapterPontos);

    }

    public void configurarListenerRecycler(){
        recyclerPontos.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerPontos,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent i = new Intent(getApplicationContext(), AdicionarPontoActivity.class);
                        i.putExtra("modoExibicao", true);
                        i.putExtra("position", position);
                        startActivity(i);
                        finish();
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
}