package com.example.filters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HorizontalListAdapter extends RecyclerView.Adapter<HorizontalListAdapter.MyViewHolder> {
    private ArrayList<MatrixFilter> filterList;
    FilterActivityInterface itemSelectedInterface;

    public HorizontalListAdapter(ArrayList<MatrixFilter> filterList,FilterActivityInterface itemSelectedInterface){
        this.filterList = filterList;
        this.itemSelectedInterface = itemSelectedInterface;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_option_layout,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(listItem);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final MatrixFilter matrixFilter = this.filterList.get(position);
        holder.textView.setText(matrixFilter.name);

        holder.linearLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                itemSelectedInterface.filterListItemSelectedCallback(matrixFilter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public View itemView;
        public TextView textView;
        public LinearLayout linearLayout;
        public MyViewHolder(View itemView){
            super(itemView);
             this.itemView = itemView;
             linearLayout = (LinearLayout)itemView.findViewById(R.id.filter_item_layout);
             textView = (TextView) itemView.findViewById(R.id.filter_name);
        }
    }
}
