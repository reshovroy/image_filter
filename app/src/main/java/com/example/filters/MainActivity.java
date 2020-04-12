package com.example.filters;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1,REQUEST_GALLERY_PICK=2;

    Button capture_button,gallery_button;
    String imageFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.action_bar)));
        initUI();
    }

    private void initUI(){
        this.capture_button = (Button)findViewById(R.id.button_capture);
        this.gallery_button = (Button)findViewById(R.id.button_gallery);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //This is the directory in which the file will be created. This is the default location of Camera photos
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for using again
        this.imageFilePath = "file://" + image.getAbsolutePath();
        return image;
    }

    public void onCapture(View v){
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void onGallery(View v){
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        String[] mimeTypes = {"image/jpeg","image/jpg","image/png"};
        i.putExtra(i.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(i,REQUEST_GALLERY_PICK);
    }



    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_IMAGE_CAPTURE:
                    Log.d("path",this.imageFilePath);
                    //Bitmap captured_img = (Bitmap)data.getExtras().get("data");
                    //this.selectedImage = captured_img;

                    this.moveToImageActivity();
                    break;
                case REQUEST_GALLERY_PICK:
                    Bitmap img;
                    Uri image_uri = data.getData();
                    this.imageFilePath = image_uri.toString();
                    Log.d("path",image_uri.toString());
                    this.moveToImageActivity();
                    break;
            }
        }
    }

    protected void moveToImageActivity(){
        Intent intent = new Intent(this,MainImageActivity.class);
        intent.putExtra("image_uri",this.imageFilePath);
        startActivity(intent);
    }




}
