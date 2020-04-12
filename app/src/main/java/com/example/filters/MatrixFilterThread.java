package com.example.filters;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class MatrixFilterThread implements Runnable {
    private int sx,sy,ex,ey;
    private Bitmap original_image,result_image;
    private float [][] filter;
    private int filter_size;
    private float filter_factor;
    private boolean has_factor;
    private int type;
    private volatile boolean flag = false;

    private ThreadCallbackInterface callback_interface;

    public MatrixFilterThread(ThreadCallbackInterface callback_interface){
        this.callback_interface = callback_interface;
    }

    public void setupThread(int sx, int ex, int sy, int ey, Bitmap original_image,Bitmap result_image,MatrixFilter filter,int type){
        this.sx = sx;
        this.sy = sy;
        this.ey = ey;
        this.ex = ex;
        this.original_image = original_image;
        this.result_image = result_image;
        if(filter != null){
            this.filter = filter.current_filter;
            this.filter_size = filter.rows;
            this.has_factor = filter.has_factor;
            this.filter_factor = filter.factor;

            if(this.has_factor == true){
                float factor = 0;
                for(int i=0;i<filter_size;i++)
                    for(int j=0;j<filter_size;j++)
                        factor += this.filter[i][j];
                this.filter_factor = factor;
            }
        }
        this.flag = false;
        this.type = type;
    }

    private void applyMatrix(){
        //Log.d("applying matrix","started");
        int A,R,G,B;
        for(int y = this.sy;y<=this.ey;y++){
            for(int x = this.sx;x<=this.ex;x++){

                if(this.flag) return;
                A = Color.alpha(this.original_image.getPixel(x,y));
                R = G = B = 0;
                int[][] pixels = new int[this.filter_size][this.filter_size];
                for(int i=0;i<this.filter_size;i++){
                    pixels[i] = new int[this.filter_size];
                    for(int j=0;j<this.filter_size;j++){
                        pixels[i][j] = this.original_image.getPixel(x - this.filter_size/2 + i,y - this.filter_size/2 + j);
                    }
                }
                for(int i=0;i<this.filter_size;i++){
                    for(int j=0;j<this.filter_size;j++){
                        R += this.filter[i][j] * Color.red(pixels[i][j]);
                        G += this.filter[i][j] * Color.green(pixels[i][j]);
                        B += this.filter[i][j] * Color.blue(pixels[i][j]);

                    }
                }

                R = (int)(R/this.filter_factor);
                G = (int)(G/this.filter_factor);
                B = (int)(B/this.filter_factor);


                R = Math.min(Math.max(R,0),255);
                G = Math.min(Math.max(G,0),255);
                B = Math.min(Math.max(B,0),255);


                this.result_image.setPixel(x,y,Color.argb(A,R,G,B));
            }
        }
    }

    private void applyGray(){
        int A,R,G,B;
        for(int y=this.sy;y<=this.ey;y++){
            for(int x = this.sx;x<=this.ex;x++){
                if(this.flag) return;
                int pixel = this.original_image.getPixel(x,y);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                A = Color.alpha(pixel);

                int gray = Math.round((float)(R*0.299 + G*0.587 + B*0.114));
                gray = Math.min(Math.max(gray,0),255);
                R = G = B = gray;

                this.result_image.setPixel(x,y,Color.argb(A,R,G,B));
            }
        }
    }

    private void applyNegative(){
        int A,R,G,B;
        for(int y=this.sy;y<=this.ey;y++){
            for(int x = this.sx;x<=this.ex;x++){
                if(this.flag) return;
                int pixel = this.original_image.getPixel(x,y);
                R = 255 - Color.red(pixel);
                G = 255 - Color.green(pixel);
                B = 255 - Color.blue(pixel);
                A = Color.alpha(pixel);

                this.result_image.setPixel(x,y,Color.argb(A,R,G,B));
            }
        }
    }

    @Override
    public void run() {
        if(!flag){
            Log.d("Thread: ","thread started");
            switch(this.type){
                case R.string.FILTER_MATRIX:
                    this.applyMatrix();
                    break;
                case R.string.FILTER_GRAY:
                    this.applyGray();
                    break;
                case R.string.FILTER_NEGATIVE:
                    this.applyNegative();
            }
            Log.d("Thread: ","thread task completed");
        }
        this.callback_interface.threadOnComplete();
    }

    public void stopThread(){
        this.flag = true;
    }
}
