package com.jpegx.huehuebr;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.snatik.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String SAVE_IMAGE_DIALOG_TAG = "save_img_dialog";
    private FloatingActionButton fab;
    private GridView gridView;
    private Context context;
    private List<Meme> memes;
    private List<Meme> allMemes;
    private MemeAdapter memeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
    }

    private void initViews() {
        context = MainActivity.this;
        memes = Meme.listAll(Meme.class);
        allMemes = Meme.listAll(Meme.class);
        Collections.reverse(memes);
        Collections.reverse(allMemes);

        fab = (FloatingActionButton) findViewById(R.id.btSaveMeme);
        fab.setOnClickListener(onClickAdd());

        memeAdapter = new MemeAdapter(context, memes);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(memeAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            Intent intent = new Intent(context, SaveMemeActivity.class);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(intent);
        }
    }

    private View.OnClickListener onClickAdd() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    memes.clear();
                    memes.addAll(allMemes);
                    memeAdapter.notifyDataSetChanged();
                    searchView.onActionViewCollapsed();
                    return true;
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchPlate.setHint(getString(R.string.search));
            View searchPlateView = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            // use this method for search process
            searchView.setOnQueryTextListener(onQuerySearchText());
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        }
        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener onQuerySearchText() {
        return new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMeme(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchMeme(newText);
                return false;
            }
        };
    }

    private void searchMeme(String query){
        ArrayList<Meme> tempMemes = new ArrayList<Meme>();
        if(!query.equals("")) {
            for (Meme m : allMemes) {
                if (m.tags.toLowerCase().contains(query.toLowerCase())) {
                    tempMemes.add(m);
                }
            }
            memes.clear();
            memes.addAll(tempMemes);
        }else{
            memes.clear();
            memes.addAll(allMemes);
        }
        memeAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }
}
