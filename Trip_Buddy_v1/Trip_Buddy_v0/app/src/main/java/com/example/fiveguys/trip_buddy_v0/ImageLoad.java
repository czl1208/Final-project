package com.example.fiveguys.trip_buddy_v0;

/**
 * Created by shou on 4/26/2017.
 */
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.SearchRecentSuggestions;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.annotations.SerializedName;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
/**
 * Created by shou on 3/25/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageLoad extends BaseAdapter{
    private Context mContext;
    private String[] images2;
    private String[] descriptions2;
    public ImageLoad(Context c,List<String> images,List<String> descriptions ) {
        mContext = c;
        descriptions2 = new String[descriptions.size()];
        images2 = new String[images.size()];
        for (int i=0;i<descriptions.size(); i++) {
            descriptions2[i] = descriptions.get(i);
            images2[i] = images.get(i);
        }

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return descriptions2.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.gridview_text_img, null);
            grid.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Toast.makeText(mContext, "You Clicked "+String.valueOf(position), Toast.LENGTH_LONG)
                            .show();
                }
            });

        } else {
            grid = (View)convertView;
        }
        TextView textView = (TextView) grid.findViewById(R.id.textView);
        ImageView imageView = (ImageView)grid.findViewById(R.id.imageView);
        System.out.println("On getview" + descriptions2.toString() + "position" + position);
        textView.setText(String.valueOf(position)+  " "+descriptions2[position]);
        try{
            Picasso.with(mContext).load(images2[position]).into(imageView);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return grid;
    }
}