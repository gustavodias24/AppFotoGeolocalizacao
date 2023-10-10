package benicio.soluces.aplicativotestebencio.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import benicio.soluces.aplicativotestebencio.R;
import benicio.soluces.aplicativotestebencio.adapter.AdapterProjetos;
import benicio.soluces.aplicativotestebencio.databinding.ActivityMeusProjetosBinding;
import benicio.soluces.aplicativotestebencio.databinding.LayoutAdicionarProjetoBinding;
import benicio.soluces.aplicativotestebencio.model.ProjetoModel;
import benicio.soluces.aplicativotestebencio.util.ProjetoUtils;

public class MeusProjetosActivity extends AppCompatActivity {

    private ActivityMeusProjetosBinding vbindig;
    private RecyclerView recyclerProjetos;
    private AdapterProjetos adapterProjetos;
    private List<ProjetoModel> listaProjetos = new ArrayList<>();
    private Dialog dialogCriarProjeto;
    private String dataAtual;
    private Dialog dialogDelete;
    private Boolean isSearching= false;

    @SuppressLint("NotifyDataSetChanged")
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
        getSupportActionBar().setTitle("Projetos");

        vbindig.fabAdicionarProjeto.setOnClickListener( view -> {
            dialogCriarProjeto.show();
        });

        configurarRecyclerProjetos();
        atualizarLista();

        configuarAcaoProjetos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pesquisa, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Pesquisar por nome");
        searchView.setOnCloseListener(() -> {
            atualizarLista();
            isSearching = false;
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                isSearching = true;
                listaProjetos.clear();
                listaProjetos.addAll(
                        ProjetoUtils.loadList(getApplicationContext()).stream().filter( projetoModel -> projetoModel.getNomeProjeto().contains(newText) ).collect(Collectors.toList())
                );
                adapterProjetos.notifyDataSetChanged();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if( item.getItemId() == android.R.id.home){
            startActivity(new Intent(getApplicationContext(), MapaActivity.class));
            finish();
        }
        if( item.getItemId() == R.id.lisa_cima || item.getItemId() == R.id.lisa_baixo){
            Collections.reverse(listaProjetos);
            adapterProjetos.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
    
    public Dialog criarAlertProjeto(){
        LayoutAdicionarProjetoBinding bindingAdicionarProjeto =  LayoutAdicionarProjetoBinding.inflate(getLayoutInflater());
        
        bindingAdicionarProjeto.dataField.getEditText().setText(dataAtual);
        bindingAdicionarProjeto.pronto.setOnClickListener( view -> {
            String titulo, data;
            titulo = bindingAdicionarProjeto.tituloProjetoField.getEditText().getText().toString().trim().toUpperCase();
            data = bindingAdicionarProjeto.dataField.getEditText().getText().toString().trim();
            
            if ( !titulo.isEmpty() && !data.isEmpty()){

                List<ProjetoModel> listaProjetoAntiga = ProjetoUtils.loadList(getApplicationContext());

                ProjetoModel projetoModel = new ProjetoModel(
                        UUID.randomUUID().toString(),
                        titulo,
                        data,
                        new ArrayList<>()
                );

                if ( listaProjetoAntiga.size() >= 60){
                    AlertDialog.Builder bAvido = new AlertDialog.Builder(MeusProjetosActivity.this);
                    bAvido.setTitle("AVISO!");
                    bAvido.setMessage("Você atingiu a quantidade máxima de projetos na versão grátis, atualize para versão PRO!");
                    bAvido.setPositiveButton("OK", null);
                    bAvido.create().show();
                }else{
                    listaProjetoAntiga.add(projetoModel);

                    ProjetoUtils.saveList(
                            listaProjetoAntiga,
                            getApplicationContext()
                    );
                    Toast.makeText(this, "Projeto adicionado!", Toast.LENGTH_SHORT).show();
                    atualizarLista();
                    bindingAdicionarProjeto.tituloProjetoField.getEditText().setText("");
                }

                dialogCriarProjeto.dismiss();

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

        adapterProjetos = new AdapterProjetos(listaProjetos, getApplicationContext(), this, true, true);
        recyclerProjetos.setAdapter(adapterProjetos);
    }

    public void configuarAcaoProjetos(){
//        recyclerProjetos.addOnItemTouchListener( new RecyclerItemClickListener(
//                getApplicationContext(),
//                recyclerProjetos,
//                new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        Intent i = new Intent(getApplicationContext(), MapaActivity.class);
//                        i.putExtra("idProjeto", listaProjetos.get(position).getIdProjeto());
//                        startActivity(i);
//                        finish();
//                    }
//
//                    @Override
//                    public void onLongItemClick(View view, int position) {
//
//                    }
//
//                    @Override
//                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                    }
//                }
//        ));
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                AlertDialog.Builder b = new AlertDialog.Builder(MeusProjetosActivity.this);
                b.setCancelable(false);
                b.setMessage(String.format(
                        "Tem certeza que deseja deleta %s?", listaProjetos.get(viewHolder.getAdapterPosition()).getNomeProjeto()
                ));
                b.setPositiveButton("Sim", (dialogInterface, i) -> {
                    if ( !isSearching ){
                        listaProjetos.remove(viewHolder.getAdapterPosition());
                        ProjetoUtils.saveList(listaProjetos, getApplicationContext());
                        Toast.makeText(MeusProjetosActivity.this, "Projeto deletado com sucesso!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MeusProjetosActivity.this, "Impossível remover pesquisando projetos!", Toast.LENGTH_SHORT).show();
                    }

                    atualizarLista();
                    dialogDelete.dismiss();
                });
                b.setNegativeButton("Não", (d, i) -> {adapterProjetos.notifyDataSetChanged(); dialogDelete.dismiss();});

                dialogDelete = b.create();
                dialogDelete.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);

        itemTouchHelper.attachToRecyclerView(recyclerProjetos);
    }


}