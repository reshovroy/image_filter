package com.example.filters;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class MatrixGridActivity extends AppCompatActivity {
    private static int CUSTOM_MATRIX_RESULT = 1;
    TableLayout mytable;
    //float[][] matrix = {{0,0,0,0,0},{0,0,0,0,0},{0,0,1,0,0},{0,0,0,0,0},{0,0,0,0,0}};
    //float[][] matrix = {{0,0,0},{0,1,0},{0,0,0}};
    ArrayList<EditText> my_text_list;
    MyFilterLibrary myFilterLibrary;

    EditText factor_value;
    CheckBox factor_checkbox;

    int filter_size = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_grid);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.action_bar)));

        this.myFilterLibrary = new MyFilterLibrary(null,null);
        this.my_text_list = new ArrayList<>();

        this.initUI();
    }


    private void initUI(){
        this.mytable = findViewById(R.id.my_table);
        this.factor_value = findViewById(R.id.factor_value);
        this.factor_checkbox = findViewById(R.id.factor_checkbox);

        this.mytable.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d("after: ",String.valueOf(mytable.getWidth()));
                mytable.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupMatrixTable(myFilterLibrary.identity_filter_3);
            }
        });

    }

    private void setupMatrixTable(float[][] filter){
        this.my_text_list.clear();
        this.my_text_list = new ArrayList<>();
        this.mytable.removeAllViews();

        TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams tableRowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT);
        tableRowLayout.bottomMargin = 5;
        int width = this.mytable.getWidth() - (this.mytable.getPaddingLeft()+this.mytable.getPaddingRight());
        int spacing = 8;
        int box_size = width/filter.length - spacing;

        for(int i=0;i<filter.length;i++){
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(tableLayout);
            tableRowLayout.rightMargin = spacing;
            this.mytable.addView(tableRow);
            for(int j=0;j<filter[i].length;j++) {
                EditText editText = new EditText(this);
                if(j == filter[i].length)
                    tableRowLayout.rightMargin = 0;
                editText.setLayoutParams(tableRowLayout);
                editText.setHeight(box_size);
                editText.setWidth(box_size);
                editText.setGravity(Gravity.CENTER);
                //editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
                //editText.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                editText.setText(Float.toString(filter[i][j]));
                editText.setBackgroundResource(R.drawable.stroke_box);
                this.my_text_list.add(editText);
                tableRow.addView(editText);
            }
        }
    }

    public void onFilterSizeRadioSelect(View view){
        boolean checked = ((RadioButton)view).isChecked();

        switch(view.getId()){
            case R.id.size_3:
                if(checked){
                    this.filter_size = 3;
                    setupMatrixTable(this.myFilterLibrary.identity_filter_3);
                }
                break;
            case R.id.size_5:
                if(checked){
                    this.filter_size = 5;
                    setupMatrixTable(this.myFilterLibrary.identity_filter_5);
                }
                break;
        }
    }

    public void onProceed(View view){
        //create the matrix
        float[][] filter = new float[this.filter_size][this.filter_size];
        int c = 0;
        for(int i=0;i<this.filter_size;i++){
            filter[i] = new float[this.filter_size];
            for(int j=0;j<this.filter_size;j++){
                String text = this.my_text_list.get(c++).getText().toString();
                try{
                    float value = Float.parseFloat(text);
                    filter[i][j] = value;
                }
                catch(NumberFormatException e){
                    filter[i][j] = 0;
                }
            }
        }

        /*for(int i=0;i<filter.length;i++){
            String row="";
            for(int j=0;j<filter.length;j++){
                row = row + " " + filter[i][j];
            }
            Log.d("matrix ",row);
        }*/
        boolean has_factor = this.factor_checkbox.isChecked();
        float factor = Float.valueOf(this.factor_value.getText().toString());
        MatrixFilter matrixFilter = this.myFilterLibrary.createCustomFilter(filter,0,has_factor,factor);

        this.goToPreviousIntent(matrixFilter);

    }

    public void onResetUI(View view){
        ((RadioButton)findViewById(R.id.size_3)).toggle();
        setupMatrixTable(this.myFilterLibrary.identity_filter_3);
        this.factor_checkbox.setChecked(false);
        this.factor_value.setText("0.0");
    }

    private void goToPreviousIntent(MatrixFilter matrixFilter){
        Intent intent = new Intent();
        intent.putExtra("custom_filter",matrixFilter);
        //startActivity(intent);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }
}
