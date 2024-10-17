package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.viewHolder> {

    ArrayList<DailyItem> items;
    Context context;
    private ItemClickListener itemClickListener;

    public DailyAdapter(ArrayList<DailyItem> items, ItemClickListener itemClickListener) {
        this.items = items;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public DailyAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.viewholder_daily),parent,false);

        return new viewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyAdapter.viewHolder holder, int position) {
        holder.dayText.setText(items.get(position).getDay());
        holder.descriptionText.setText(items.get(position).getWeatherDescription());
        holder.maxTempText.setText(items.get(position).getMaxTemp());
        holder.minTempText.setText(items.get(position).getMinTemp());
        holder.itemImageView.setImageResource(items.get(position).getIconRes());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class viewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{

        TextView dayText, descriptionText, maxTempText, minTempText;
        ImageView itemImageView;

        private ItemClickListener itemClickListener;

        public viewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);

            dayText= itemView.findViewById(R.id.dayText);
            descriptionText=itemView.findViewById(R.id.descriptionText);
            maxTempText= itemView.findViewById(R.id.maxTemp);
            minTempText= itemView.findViewById(R.id.minTemp);
            itemImageView= itemView.findViewById(R.id.itemImage);
        }

        @Override
        public void onClick(View view) {

            itemClickListener.onItemClick(getAdapterPosition());
        }
    }
}
