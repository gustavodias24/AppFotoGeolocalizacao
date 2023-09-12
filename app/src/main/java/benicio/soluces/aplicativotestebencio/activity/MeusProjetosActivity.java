package benicio.soluces.aplicativotestebencio.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.adapter.AdapterPontos;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMeusPontosBinding;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMeusProjetosBinding;
import benicio.soluces.aplicativotestebencio.databinding.LayoutAdicionarProjetoBinding;

public class MeusProjetosActivity extends AppCompatActivity {

    private ActivityMeusProjetosBinding vbindig;
    private RecyclerView recyclerProjetos;
    private AdapterPontos adapterProjetos;
    private Dialog dialogCriarProjeto;
    private String dataAtual;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vbindig = ActivityMeusProjetosBinding.inflate(getLayoutInflater());
        setContentView(vbindig.getRoot());


        dataAtual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        dialogCriarProjeto = criarAlertProjeto();

        vbindig.menuOpcoes.open(true);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Projetos salvos");

        vbindig.fabAdicionarProjeto.setOnClickListener( view -> {
            dialogCriarProjeto.show();
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if( item.getItemId() == android.R.id.home){
            startActivity(new Intent(getApplicationContext(), MapaActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    
    public Dialog criarAlertProjeto(){
        LayoutAdicionarProjetoBinding bindingAdicionarProjeto =  LayoutAdicionarProjetoBinding.inflate(getLayoutInflater());
        
        bindingAdicionarProjeto.dataField.getEditText().setText(dataAtual);
        bindingAdicionarProjeto.pronto.setOnClickListener( view -> {
            String titulo, data;
            titulo = bindingAdicionarProjeto.tituloProjetoField.getEditText().getText().toString().trim();
            data = bindingAdicionarProjeto.dataField.getEditText().getText().toString().trim();
            
            if ( !titulo.isEmpty() && !data.isEmpty()){
                Toast.makeText(this, "Projeto adicionado!", Toast.LENGTH_SHORT).show();
                bindingAdicionarProjeto.tituloProjetoField.getEditText().setText("");
                bindingAdicionarProjeto.dataField.getEditText().setText("");
            }else{
                Toast.makeText(this, "Título e data obrigatórios!", Toast.LENGTH_SHORT).show();
            }
        });
        return new AlertDialog.Builder(MeusProjetosActivity.this).setView(bindingAdicionarProjeto.getRoot()).create();
    }
}