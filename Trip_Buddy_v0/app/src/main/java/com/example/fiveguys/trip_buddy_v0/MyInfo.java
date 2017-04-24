package com.example.fiveguys.trip_buddy_v0;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MyInfo extends AppCompatActivity implements View.OnClickListener{
    private String username;
    private String email;
    private String uid;
    private String photoUrl;
    private EditText edtUserName;
    private EditText edtUserSex;
    private EditText edtUserFav;
    private EditText edtUserAge;
    private ImageView UserImage;
    private Button btnDelet;
    private TextView txtEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            username = user.getDisplayName();
            email = user.getEmail();
            uid = user.getUid();
            photoUrl = user.getPhotoUrl().toString();
        } else {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_my_info);
        edtUserName = (EditText) findViewById(R.id.edtUserName);
        edtUserAge = (EditText) findViewById(R.id.edtUserAge);
        edtUserSex = (EditText) findViewById(R.id.editUserSex);
        edtUserFav = (EditText) findViewById(R.id.edtUserFav);
        UserImage = (ImageView) findViewById(R.id.UserImag);
        txtEdit = (TextView) findViewById(R.id.txtEdit);
        edtUserFav.setEnabled(false);
        edtUserSex.setEnabled(false);
        edtUserAge.setEnabled(false);
        edtUserName.setEnabled(false);
        if(username.length()>0){
            edtUserName.setText(username);
        }
        if(photoUrl.length()>0){
            Picasso.with(getApplicationContext()).load(photoUrl.toString()).into(UserImage);
        }
        txtEdit.setOnClickListener(this);
        UserImage.setOnClickListener(this);

    }
    public void updateProfile(){
        String option = txtEdit.getText().toString();
        if(option.equals("Update your profile")){
            String sex = edtUserSex.getText().toString();
            String username = edtUserName.getText().toString();
            String age = edtUserAge.getText().toString();
            String favorite = edtUserFav.getText().toString();
            edtUserName.setBackground(getDrawable(R.color.LoginBackground));
            edtUserFav.setBackground(getDrawable(R.color.LoginBackground));
            edtUserSex.setBackground(getDrawable(R.color.LoginBackground));
            edtUserAge.setBackground(getDrawable(R.color.LoginBackground));
            edtUserName.setEnabled(false);
            edtUserFav.setEnabled(false);
            edtUserSex.setEnabled(false);
            edtUserAge.setEnabled(false);
            txtEdit.setText("edit your profile");
        }else{
            edtUserName.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserFav.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserSex.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserAge.setBackground(getDrawable(R.drawable.roundedttext));
            edtUserName.setEnabled(true);
            edtUserFav.setEnabled(true);
            edtUserSex.setEnabled(true);
            edtUserAge.setEnabled(true);
            txtEdit.setText("Update your profile");
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtEdit:
                updateProfile();
                break;
            case R.id.UserImag:
                break;
        }
    }
}
