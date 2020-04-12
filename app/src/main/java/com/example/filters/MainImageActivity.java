package com.example.filters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainImageActivity extends AppCompatActivity implements FilterActivityInterface {

    private static int CUSTOM_MATRIX_RESULT = 1;

    SeekBar mySeekBar;
    ImageView main_imageView;
    Bitmap image_bitmap,current_image_bitmap;
    RecyclerView recycler_filter_list;
    TextView effect_text;
    String image_uri;

    Object selected_filter,current_filter;
    MyFilterLibrary myFilterLibrary;

    ArrayList<MatrixFilter> myLibraryMatrixFilters;

    Handler main_handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_image);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.action_bar)));

        if(savedInstanceState != null){
            this.image_uri = savedInstanceState.get("image_uri").toString();
        }
        else{
            this.image_uri = getIntent().getExtras().get("image_uri").toString();
        }

        main_handler = new Handler();
        setupFilter();
        //checkCustomFilter();
        initUI();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("image_uri",this.image_uri);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /*@Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.image_uri = savedInstanceState.get("image_uri").toString();
        Log.d("image uri",image_uri);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_app_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if(itemID == R.id.save_image){
            this.saveBitmapToExternalStorage();
        }
        else if(itemID == R.id.share_image ){
            this.shareBitmapToSocial();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI(){
        this.main_imageView = (ImageView)findViewById(R.id.main_image);
        this.effect_text = (TextView)findViewById(R.id.effect_text);

        this.recycler_filter_list = (RecyclerView)findViewById(R.id.recycler_filter_list);

        try {
            this.image_bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(this.image_uri));
            /*ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            this.image_bitmap.compress(Bitmap.CompressFormat.JPEG,40,bstream);

            byte[] byteArray = bstream.toByteArray();
            this.image_bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);*/
            //resize Bitmap
            this.image_bitmap = this.myFilterLibrary.resizeBitmap(this.image_bitmap,480);

            this.main_imageView.setImageBitmap(this.image_bitmap);
            Log.d("dimensions ",Integer.toString(this.image_bitmap.getWidth())+ " " + Integer.toString(this.image_bitmap.getHeight()));


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("received_uri",this.image_uri);

        //setup seekbar
        this.mySeekBar = (SeekBar)findViewById(R.id.effect_seekbar);
        this.mySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                controlUI(false);
                updateCurrentFilter(seekBar.getProgress());
                applyCurrentFilter();
            }
        });
        this.initHorizontalList();
        this.resetSeekbarUI();
        this.resetImage();
    }

    private void initHorizontalList(){
        HorizontalListAdapter myListAdapter = new HorizontalListAdapter(myLibraryMatrixFilters,this);
        this.recycler_filter_list.setHasFixedSize(true);
        this.recycler_filter_list.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false));
        this.recycler_filter_list.setAdapter(myListAdapter);
    }

    private void controlUI(boolean enabled){
        this.mySeekBar.setEnabled(enabled);
        this.recycler_filter_list.setEnabled(enabled);
    }

    private void setupFilter(){
        this.myFilterLibrary = new MyFilterLibrary(this.main_handler,this);
        this.myLibraryMatrixFilters = this.myFilterLibrary.getMatrixFilterList();
        this.selected_filter = this.myLibraryMatrixFilters.get(0);

    }


    private void updateCurrentFilter(int progress){
        //switch depending on class

        if(MatrixFilter.class.isInstance(this.selected_filter)){
            ((MatrixFilter)this.selected_filter).updateFilter(progress);
        }

    }

    private void applyCurrentFilter(){
        this.myFilterLibrary.applyMatrixFilter(this.image_bitmap,(MatrixFilter)this.selected_filter,4);
    }

    private void resetImage(){
        this.current_image_bitmap = this.image_bitmap;
        this.main_imageView.setImageBitmap(this.current_image_bitmap);
    }

    private void resetSeekbarUI(){
        MatrixFilter cur_filter = (MatrixFilter)this.selected_filter;
        this.effect_text.setText(cur_filter.name);
        this.mySeekBar.setProgress(cur_filter.default_progress);
    }

    @Override
    public void filterListItemSelectedCallback(MatrixFilter filter){
        resetImage();
        this.selected_filter = filter;
        ((MatrixFilter)this.selected_filter).progress = 0;
        this.resetImage();
        this.resetSeekbarUI();
    }

    @Override
    public void filterAppliedCallback(Bitmap result_bitmap) {
        this.current_image_bitmap = result_bitmap;
        Log.d("dimensions ",Integer.toString(this.current_image_bitmap.getWidth())+ " " + Integer.toString(this.current_image_bitmap.getHeight()));
        this.main_imageView.setImageBitmap(this.current_image_bitmap);

        this.controlUI(true);
        Toast.makeText(this,"filter applied",Toast.LENGTH_SHORT).show();
    }

    //onclickReset
    public void onClickResetButton(View view){
        this.selected_filter = this.myLibraryMatrixFilters.get(0);
        this.resetImage();
        this.resetSeekbarUI();
    }

    //create grayscale image
    public void onClickGrayButton(View view){
        this.myFilterLibrary.applyGrayFilter(this.current_image_bitmap,2);
    }
    //create negative image
    public void onClickNegativeButton(View view){
        this.myFilterLibrary.applyNegativeFilter(this.current_image_bitmap,2);
    }
    //create custom filter
    public void onCreateCustomFilterIntent(View view){
        Intent intent = new Intent(this,MatrixGridActivity.class);
        startActivityForResult(intent,CUSTOM_MATRIX_RESULT);
    }
    //listen for custom matrix response
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CUSTOM_MATRIX_RESULT && resultCode == RESULT_OK){
            MatrixFilter m = (MatrixFilter)data.getExtras().getSerializable("custom_filter");
            if(this.myLibraryMatrixFilters.get(this.myLibraryMatrixFilters.size()-1).name.equals("custom")){
                Log.d("custom","true");
                this.myLibraryMatrixFilters.remove(this.myLibraryMatrixFilters.size()-1);
            }

            this.myLibraryMatrixFilters.add(m);
            //handle new custom filter
            this.selected_filter = m;
            this.initHorizontalList();
            this.resetImage();
            this.resetSeekbarUI();
        }
    }

    //save image
    protected String saveBitmapToExternalStorage(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "FILTERY" + timeStamp + ".jpg";
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

        File myDir = new File(root + "/Filtery");
        myDir.mkdirs();

        File file = new File(myDir,imageFileName);
        FileOutputStream out = null;
        try{
            out = new FileOutputStream(file);
            this.current_image_bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();
            Toast.makeText(this,"file saved",Toast.LENGTH_SHORT).show();

            MediaScannerConnection.scanFile(this,new String[]{file.toString()},null,null);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        String file_path = "file://" + file.getAbsolutePath();
        return file_path;

    }

    protected void shareBitmapToSocial(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //save the bitmap first
        String path = this.saveBitmapToExternalStorage();
        Uri uri = Uri.parse(path);

        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM,uri);
        startActivity(Intent.createChooser(shareIntent,"Select App"));

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.myFilterLibrary.stopAllProcessing();
    }
}
