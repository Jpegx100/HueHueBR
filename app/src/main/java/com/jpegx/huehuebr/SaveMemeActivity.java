package com.jpegx.huehuebr;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.snatik.storage.Storage;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                prepareActivity();
            } else {
                requestPermission();
            }
        }else{
            prepareActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_meme_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.save_meme_menu_crop:
                CropImage.activity(memeUri).start(this);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                handleCroppedMeme(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void prepareActivity() {
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

    protected boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    protected void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "É preciso da permissão para escrita", Toast.LENGTH_LONG).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareActivity();
                } else {
                    Log.e("value", "Permissão negada. Não é possível escrever na memória externa.");
                }
                break;
        }
    }

    private void handleReceivedMeme(Intent intent) {
        memeUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        Picasso.with(context).load(memeUri).into(ivMeme);
    }

    private void handleCroppedMeme(Uri croppedImg) {
        memeUri = croppedImg;
        Picasso.with(context).load(memeUri).into(ivMeme);
    }

    private View.OnClickListener onClickSaveMeme() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Storage storage = new Storage(context);

                String fromPath = memeUri.getPath();
                String extension = getMimeType(context, memeUri);

                String[] subParts = fromPath.split("/");
                String toPath = storage.getExternalStorageDirectory(Environment.DIRECTORY_PICTURES)
                        + File.separator + "AqueleMeme";
                if (!storage.isDirectoryExists(toPath)) {
                    storage.createDirectory(toPath);
                }
                toPath += File.separator + subParts[subParts.length - 1] + "." + extension;
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
            }
        };
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return extension;
    }

}
