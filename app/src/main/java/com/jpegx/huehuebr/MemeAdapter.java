package com.jpegx.huehuebr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.snatik.storage.Storage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpegx on 8/13/17.
 */

class MemeAdapter extends BaseAdapter{

    private final List<Meme> memes;
    private final Context context;

    public MemeAdapter(Context context, List<Meme> memes){
        this.context = context;
        this.memes  = memes;
    }

    @Override
    public int getCount() {
        return this.memes.size();
    }

    @Override
    public Object getItem(int position) {
        return this.memes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.memes.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater;
        View gridView;
        Meme meme = memes.get(position);

        if(convertView==null){
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridView = inflater.inflate(R.layout.meme_item, parent, false);

            TextView textView = (TextView) gridView.findViewById(R.id.meme_item_tv_tag);
            textView.setText(meme.tags);

            ImageView imageView = (ImageView) gridView.findViewById(R.id.meme_item_iv_meme);

            Storage store = new Storage(context);
            final File file = store.getFile(meme.path);
            Picasso picasso = Picasso.with(context);
            picasso.setLoggingEnabled(true);
            picasso.load(file)
                    .resize(250, 250)
                    .centerCrop()
                    .error(R.mipmap.ic_launcher)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(200);

                    final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpeg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_meme)));
                }
            });
        }else{
            gridView = convertView;
        }

        return gridView;
    }
}
