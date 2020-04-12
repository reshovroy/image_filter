package com.example.filters;

import java.io.Serializable;

public class MatrixFilter implements Serializable {

    public static String FILTER_MATRIX = "FILTER_MATRIX";
    public static String FILTER_GRAY = "FILTER_GRAY";
    public static String FILTER_NEGATIVE = "FILTER_NEGATIVE";
    public String name;
    public int rows,cols;
    public float factor;
    public boolean has_factor;
    public int progress;
    public int default_progress;
    public float[][] filter;
    public float[][] identity_filter;
    public float[][] current_filter;

    public MatrixFilter(String name,float[][] id_filter,float[][] filter,int default_progress,boolean has_factor,float factor){
        this.name = name;

        this.rows = filter.length;
        this.cols = filter[0].length;
        this.has_factor = has_factor;
        this.factor = factor;
        this.filter = new float[this.rows][this.cols];
        this.identity_filter = new float[this.rows][this.cols];
        this.current_filter = new float[this.rows][this.cols];
        for(int i=0;i<id_filter.length;i++){
            this.identity_filter[i] = id_filter[i].clone();
            this.current_filter[i] = id_filter[i].clone();
        }
        for(int i=0;i<filter.length;i++){
            this.filter[i] = filter[i].clone();
        }

        this.default_progress = default_progress;

    }


    public void updateFilter(int progress){
        this.progress = progress;
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                float value = this.identity_filter[i][j] + (this.filter[i][j] - this.identity_filter[i][j])*((float)progress/(float)100.0);
                value =(float)(Math.round(value*10.0)/10.0);
                this.current_filter[i][j] = value;
            }
        }
    }
}
