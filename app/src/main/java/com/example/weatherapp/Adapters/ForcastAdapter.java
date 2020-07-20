package com.example.weatherapp.Adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;

import org.w3c.dom.Text;

import java.util.List;

//This class handles the recycler view for 7-Day forcast
public class ForcastAdapter extends RecyclerView.Adapter<ForcastAdapter.MyViewHolder> {

    //List to store the data fetched from JSON
    private List<String> date,maxTemp,minTemp,main,desc;

    //This class contains the basic format of the forcast recycler view and using this class we will create
    //7 recycler view items
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView dateTV;
        TextView maxTempTV;
        TextView minTempTV;
        TextView mainTV;
        TextView descTV;

        public MyViewHolder(View itemView) {
            super(itemView);
            dateTV = itemView.findViewById(R.id.date);
            maxTempTV  = itemView.findViewById(R.id.maxTemp);
            minTempTV = itemView.findViewById(R.id.minTemp);
            mainTV = itemView.findViewById(R.id.main);
            descTV = itemView.findViewById(R.id.desc);
        }
    }

    //Constructor to initialize the lists
    public ForcastAdapter(List<String> date, List<String> maxTemp, List<String> minTemp, List<String> main, List<String> desc) {
        this.date = date;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.main = main;
        this.desc = desc;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daycard, parent, false);

        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {
        //The data fetched from JSON is assigned
        TextView dateTV = holder.dateTV;
        TextView maxTempTV = holder.maxTempTV;
        TextView minTempTV = holder.minTempTV;
        TextView mainTV = holder.mainTV;
        TextView descTV = holder.descTV;

        dateTV.setText(date.get(listPosition));
        maxTempTV.setText("Max Temp: " + maxTemp.get(listPosition)+  " \u2103" );
        minTempTV.setText("Min Temp: " + minTemp.get(listPosition)+  " \u2103" );
        mainTV.setText(main.get(listPosition));
        descTV.setText(desc.get(listPosition).toUpperCase());
    }

    @Override
    public int getItemCount() {
        return date.size();
    }}
