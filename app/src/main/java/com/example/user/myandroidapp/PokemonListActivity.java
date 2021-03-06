package com.example.user.myandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user.myandroidapp.adapter.PokemonListViewAdapter;
import com.example.user.myandroidapp.model.OwnedPokemonDataManager;
import com.example.user.myandroidapp.model.OwnedPokemonInfo;

import java.util.ArrayList;

public class PokemonListActivity extends CustomizedActivity implements OnPokemonSelectedChangeListener, AdapterView.OnItemClickListener {

    public final static int detailActivityRequestCode = 1;
    public final static String ownedPokemonInfoKey = "ownedPokemonInfoKey";

    PokemonListViewAdapter arrayAdapter;
    ArrayList<OwnedPokemonInfo> ownedPokemonInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_list);

        activityName = this.getClass().getSimpleName();

        OwnedPokemonDataManager dataManager = new OwnedPokemonDataManager(this);
        dataManager.loadListViewData();
        dataManager.loadPokemonTypes();

        ownedPokemonInfos = dataManager.getOwnedPokemonInfos();

        OwnedPokemonInfo[] initPokemonInfos = dataManager.getInitPokemonInfos();
        Intent srcIntent = getIntent();
        int selectedIndex = srcIntent.getIntExtra(MainActivity.selectedPokemonIndexKey, 0);
        ownedPokemonInfos.add(0, initPokemonInfos[selectedIndex]);

        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new PokemonListViewAdapter(this,
                R.layout.row_view_of_pokemon_list,
                ownedPokemonInfos);
        arrayAdapter.pokemonSelectedChangeListener = this;

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (arrayAdapter.selectedPokemons.isEmpty()) {
            return false; //not showing anything on action bar
        } else {
            getMenuInflater().inflate(R.menu.selected_pokemon_action_bar, menu);
            return true; //show the menu items on action bar
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_delete) {
            for (OwnedPokemonInfo ownedPokemonInfo : arrayAdapter.selectedPokemons) {
                ownedPokemonInfos.remove(ownedPokemonInfo);
            }
            arrayAdapter.selectedPokemons.clear();
            arrayAdapter.notifyDataSetChanged();

            // second way to remove from ownedPokemonInfos
//            for(OwnedPokemonInfo ownedPokemonInfo : arrayAdapter.selectedPokemons) {
//                arrayAdapter.remove(ownedPokemonInfo);
//            }
//            arrayAdapter.selectedPokemons.clear();

            invalidateOptionsMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSelectedChange(OwnedPokemonInfo ownedPokemonInfo) {
        invalidateOptionsMenu(); //make system call onCreateOptionsMenu
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OwnedPokemonInfo ownedPokemonInfo = arrayAdapter.getItem(position);

        Intent intent = new Intent();
        intent.setClass(PokemonListActivity.this, DetailActivity.class);
        intent.putExtra(ownedPokemonInfoKey, ownedPokemonInfo);
        startActivityForResult(intent, detailActivityRequestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == detailActivityRequestCode) { //this result came from detail activity
            if (resultCode == DetailActivity.savePokemonIntoComputer) {
                String pokemonName = data.getStringExtra(OwnedPokemonInfo.nameKey);
                if (arrayAdapter != null) {
                    OwnedPokemonInfo ownedPokemonInfo = arrayAdapter.getItemWithName(pokemonName);
                    arrayAdapter.remove(ownedPokemonInfo);

                    //alternatives
//                    ownedPokemonInfos.remove(ownedPokemonInfo);
//                    arrayAdapter.notifyDataSetChanged();

                    Toast.makeText(this, pokemonName + "已經被存到電腦裡了", Toast.LENGTH_SHORT).show();
                }
            }
            // add a new result code in DetailActivity and write a corresponding conditional statement here to do the update effect
            else if (resultCode == DetailActivity.levelUp) {
                OwnedPokemonInfo ownedPokemonInfo = data.getParcelableExtra(OwnedPokemonInfo.class.getSimpleName());
                if (arrayAdapter != null) {
                    arrayAdapter.update(ownedPokemonInfo);
                }
            }

        }

    }
}
