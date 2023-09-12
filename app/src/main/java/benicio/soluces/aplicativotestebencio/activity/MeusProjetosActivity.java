package benicio.soluces.aplicativotestebencio.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.adapter.AdapterPontos;
import benicio.soluces.aplicativotestebencio.adapter.AdapterProjetos;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMeusPontosBinding;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMeusProjetosBinding;
import benicio.soluces.aplicativotestebencio.databinding.LayoutAdicionarProjetoBinding;
import benicio.soluces.aplicativotestebencio.model.ProjetoModel;
import benicio.soluces.aplicativotestebencio.util.PontosUtils;
import benicio.soluces.aplicativotestebencio.util.ProjetoUtils;

public class MeusProjetosActivity extends AppCompatActivity {

    private ActivityMeusProjetosBinding vbindig;
    private RecyclerView recyclerProjetos;
    private AdapterProjetos adapterProjetos;
    private List<ProjetoModel> listaProjetos = new ArrayList<>();
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

        configurarRecyclerProjetos();
        atualizarLista();
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

                ProjetoModel projetoModel = new ProjetoModel(
                        titulo,
                        data,
                        new ArrayList<>()
                );
                List<ProjetoModel> listaProjetoAntiga = ProjetoUtils.loadList(getApplicationContext());
                listaProjetoAntiga.add(projetoModel);

                ProjetoUtils.saveList(
                        listaProjetoAntiga,
                        getApplicationContext()
                );

                Toast.makeText(this, "Projeto adicionado!", Toast.LENGTH_SHORT).show();
                atualizarLista();
                bindingAdicionarProjeto.tituloProjetoField.getEditText().setText("");
                bindingAdicionarProjeto.dataField.getEditText().setText("");
            }else{
                Toast.makeText(this, "Título e data obrigatórios!", Toast.LENGTH_SHORT).show();
            }
        });
        return new AlertDialog.Builder(MeusProjetosActivity.this).setView(bindingAdicionarProjeto.getRoot()).create();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void atualizarLista(){
        listaProjetos.clear();
        listaProjetos.addAll(ProjetoUtils.loadList(getApplicationContext()));
        adapterProjetos.notifyDataSetChanged();

        vbindig.avisoEmptyText.setVisibility( listaProjetos.isEmpty() ? View.VISIBLE : View.GONE);

    }
    public void configurarRecyclerProjetos(){
        recyclerProjetos = vbindig.reyclerProjetos;
        recyclerProjetos.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerProjetos.setHasFixedSize(true);
        recyclerProjetos.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        adapterProjetos = new AdapterProjetos(listaProjetos, getApplicationContext());
        recyclerProjetos.setAdapter(adapterProjetos);
    }
}