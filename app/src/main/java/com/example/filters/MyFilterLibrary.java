package com.example.filters;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;

import java.util.ArrayList;

public class MyFilterLibrary implements ThreadCallbackInterface{

    public float[][] identity_filter_3 = {{0,0,0},{0,1,0},{0,0,0}};
    public float[][] identity_filter_5 = {{0,0,0,0,0},{0,0,0,0,0},{0,0,1,0,0},{0,0,0,0,0},{0,0,0,0,0}};
    private float[][] edge_filter = {{-1,-1,-1},{-1,8,-1},{-1,-1,-1}};
    private float[][] sharp_filter = {{-1,-1,-1},{-1,9,-1},{-1,-1,-1}};
    private float[][] color_edge_filter = {{1,1,1},{1,-7,1},{1,1,1}};
    private float[][] emboss_filter = {{-1,-1,0},{-1,0,1},{0,1,1}};
    private float[][] blur_filter = {{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1}};
    private float[][] brightness_filter = {{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1},{1,1,1,1,1}};


    private MatrixFilterThread[] matrixFilterThreads;
    private int maxThreads = 4;
    private int active_threads;

    private Handler main_handler;
    private FilterActivityInterface filterActivityInterface;

    private Bitmap output_bitmap;

    public MyFilterLibrary(Handler main_handler,FilterActivityInterface filterActivityInterface){
        this.main_handler = main_handler;
        this.filterActivityInterface = filterActivityInterface;

        this.matrixFilterThreads = new MatrixFilterThread[this.maxThreads];
        for(int i=0;i<this.maxThreads;i++)
            this.matrixFilterThreads[i] = new MatrixFilterThread(this);

        this.active_threads = 0;
    }

    //utilities

    public Bitmap resizeBitmap(Bitmap original_bitmap,int newWidth){
        int width = original_bitmap.getWidth();
        int height = original_bitmap.getHeight();

        float aspectRatio = ((float)width/(float)height);
        int newHeight = Math.round(newWidth/aspectRatio);

        Bitmap resized_bitmap = Bitmap.createScaledBitmap(original_bitmap,newWidth,newHeight,false);

        return resized_bitmap;
    }

    public MatrixFilter createCustomFilter(float[][] filter,int progress,boolean has_factor,float factor){
        float[][] identity = filter.length == 3 ? this.identity_filter_3:this.identity_filter_5;
        return new MatrixFilter("custom",identity,filter,progress,has_factor,factor);
    }

    //filters

    public MatrixFilter getSharpFilter(){
        return new MatrixFilter("sharp",this.identity_filter_3,this.sharp_filter,0,false,1);
    }
    public MatrixFilter getEdgeFilter(){
        return new MatrixFilter("edge",this.identity_filter_3,this.edge_filter,0,false,1);
    }
    public MatrixFilter getColorEdgeFilter(){
        return new MatrixFilter("color edge",this.identity_filter_3,this.color_edge_filter,0,false,1);
    }
    public MatrixFilter getEmbossFilter(){
        return new MatrixFilter("emboss",this.identity_filter_3,this.emboss_filter,0,false,1);
    }
    public MatrixFilter getBlurFilter(){
        return new MatrixFilter("blur",this.identity_filter_5,this.blur_filter,0,true,9);
    }
    public MatrixFilter getBrightnessFilter(){
        return new MatrixFilter("brightness",this.identity_filter_5,this.brightness_filter,50,false,9);
    }

    public ArrayList<MatrixFilter> getMatrixFilterList(){
        ArrayList<MatrixFilter> matrixFilters = new ArrayList<>();
        matrixFilters.add(getSharpFilter());
        matrixFilters.add(getEdgeFilter());
        matrixFilters.add(getColorEdgeFilter());
        matrixFilters.add(getEmbossFilter());
        matrixFilters.add(getBlurFilter());
        matrixFilters.add(getBrightnessFilter());

        return matrixFilters;
    }




    public MatrixFilter getIdentityFilter(int dimension){
        MatrixFilter res = null;
        if(dimension == 3)
            res = new MatrixFilter("identity 3",this.identity_filter_3,this.identity_filter_3,0,false,1);
        else if(dimension == 5)
            res = new MatrixFilter("identity 5",this.identity_filter_5,this.identity_filter_5,0,false,1);
        return res;
    }

    //filter application methods

    public void applyMatrixFilter(Bitmap original_img,MatrixFilter filter,int thread_count){

        this.active_threads = 0;

        this.output_bitmap = Bitmap.createBitmap(original_img.getWidth(),original_img.getHeight(),original_img.getConfig());

        this.handleFilterThreadCreation(original_img,filter,thread_count,R.string.FILTER_MATRIX);

        //start the threads
        for(int j=0;j<thread_count;j++){
            this.active_threads++;
            new Thread(this.matrixFilterThreads[j]).start();
        }
    }

    public void applyGrayFilter(Bitmap original_img,int thread_count){
        this.active_threads = thread_count;
        this.output_bitmap = Bitmap.createBitmap(original_img.getWidth(),original_img.getHeight(),original_img.getConfig());

        this.handleFilterThreadCreation(original_img,null,thread_count,R.string.FILTER_GRAY);

        for(int i=0;i<thread_count;i++)
            new Thread(this.matrixFilterThreads[i]).start();
    }

    public void applyNegativeFilter(Bitmap original_img,int thread_count){
        this.active_threads = thread_count;
        this.output_bitmap = Bitmap.createBitmap(original_img.getWidth(),original_img.getHeight(),original_img.getConfig());

        this.handleFilterThreadCreation(original_img,null,thread_count,R.string.FILTER_NEGATIVE);

        for(int i=0;i<thread_count;i++)
            new Thread(this.matrixFilterThreads[i]).start();
    }

    public void handleFilterThreadCreation(Bitmap original_img,MatrixFilter filter,int threads,int type){
        int height = original_img.getHeight();
        int width = original_img.getWidth();
        int shift = 0;
        if(filter !=null)
            shift = filter.rows/2;

        threads = Math.min(this.maxThreads,threads);
        int block_size = height/threads;
        int b_start = 0;
        //deal with start and end blocks separately
        //starting block
        int i=0;

        this.matrixFilterThreads[i++].setupThread(shift,width - shift-1,shift,b_start + block_size-1,original_img,this.output_bitmap,filter,type);
        b_start += block_size;
        for(;i<threads-1;i++){
            this.matrixFilterThreads[i].setupThread(shift,width - shift-1,b_start,b_start + block_size-1,original_img,this.output_bitmap,filter,type);
            b_start += block_size;
        }
        //last block
        this.matrixFilterThreads[i].setupThread(shift,width - shift-1,b_start,height - shift-1,original_img,this.output_bitmap,filter,type);
    }


    private void onImageFilterThreadJobDoneUpdate(){
        this.active_threads--;
        if(this.active_threads == 0){
            this.filterActivityInterface.filterAppliedCallback(this.output_bitmap);
        }
    }

    @Override
    public void threadOnComplete() {
        this.main_handler.post(new Runnable() {
            @Override
            public void run() {
                onImageFilterThreadJobDoneUpdate();
            }
        });
    }

    public void stopAllProcessing(){
        for(int i=0;i<this.maxThreads;i++)
            this.matrixFilterThreads[i].stopThread();
    }

}
