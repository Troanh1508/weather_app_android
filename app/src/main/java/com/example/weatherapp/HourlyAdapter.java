package com.example.weatherapp;

import android.content.Context;
import android.text.method.CharacterPickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.viewHolder> {

    ArrayList<HourlyItem> items;
    Context context;
    private ItemClickListener itemClickListener;

    public HourlyAdapter(ArrayList<HourlyItem> items, ItemClickListener itemClickListener) {
        this.items = items;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public HourlyAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.viewholder_hourly),parent,false);

        return new viewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyAdapter.viewHolder holder, int position) {
        holder.hourText.setText(items.get(position).getHour());
        holder.temperatureText.setText(items.get(position).getTemperature());
        holder.itemImageView.setImageResource(items.get(position).getIconRes());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{

        TextView hourText, temperatureText;
        ImageView itemImageView;

        private ItemClickListener itemClickListener;

        public viewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);

            hourText= itemView.findViewById(R.id.hourText);
            temperatureText= itemView.findViewById(R.id.temperatureText);
            itemImageView= itemView.findViewById(R.id.itemImageView);
        }

        @Override
        public void onClick(View view) {

            itemClickListener.onItemClick(getAdapterPosition());
        }
    }
}
