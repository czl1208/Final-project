package com.example.fiveguys.trip_buddy_v0;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by caozhongli on 4/26/17.
 */

public class customList extends ArrayAdapter<String> {
    private final Activity context;
    private final List<String> imageId;
    public customList(Activity context,
                      String[] web, List<String> imageId) {
        super(context, R.layout.listview, web);
        this.context = context;
        this.imageId = imageId;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.listview, null, true);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imgView);
        //Bitmap bitmap = BitmapFactory.decodeFile(imageId.get(2));

        Picasso.with(getApplicationContext()).load(imageId.get(position)).into(imageView);
        return rowView;
    }
}
