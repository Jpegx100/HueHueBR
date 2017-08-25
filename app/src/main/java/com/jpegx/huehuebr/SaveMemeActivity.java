package com.jpegx.huehuebr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.snatik.storage.Storage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class SaveMemeActivity extends AppCompatActivity {
    private FloatingActionButton btSaveMeme;
    private ImageView ivMeme;
    private EditText etTags;
    private Context context;
    private Uri memeUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_meme);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = SaveMemeActivity.this;

        btSaveMeme = (FloatingActionButton) findViewById(R.id.btSaveMeme);
        btSaveMeme.setOnClickListener(onClickSaveMeme());

        ivMeme = (ImageView) findViewById(R.id.save_meme_iv_meme);
        etTags = (EditText) findViewById(R.id.save_meme_et_tags);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(Intent.ACTION_SEND.equals(action) && type!=null){
            if(type.startsWith("image/")){
                handleReceivedMeme(intent);
            }
        }

    }

    private void handleReceivedMeme(Intent intent) {
        memeUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        Picasso.with(context).load(memeUri).into(ivMeme);
    }

    private View.OnClickListener onClickSaveMeme() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etTags.getText()!=null && etTags.getText().toString()!=null
                        && etTags.getText().toString().length()>0) {
                    Storage storage = new Storage(context);
                    String fromPath = memeUri.getPath();
                    String[] subParts = fromPath.split("/");
                    String toPath = storage.getExternalStorageDirectory(Environment.DIRECTORY_PICTURES)
                            + File.separator + "AqueleMeme";
                    if (!storage.isDirectoryExists(toPath)) {
                        storage.createDirectory(toPath);
                    }
                    toPath += File.separator + subParts[subParts.length - 1] + ".jpeg";
                    try {
                        Bitmap bitmap = null;
                        bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), memeUri);
                        storage.createFile(toPath, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Meme meme = new Meme(toPath, etTags.getText().toString());
                    meme.save();

                    Toast.makeText(context, getString(R.string.meme_saved), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(context, getString(R.string.no_tag_error), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

}
