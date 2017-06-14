package com.moviepass.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.ShowtimeClickListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheaterShowtimesAdapter extends RecyclerView.Adapter<TheaterShowtimesAdapter.ViewHolder> {

    private final ShowtimeClickListener showtimeClickListener;
    private ArrayList<String> showtimesArrayList;
    int row_idex;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public TheaterShowtimesAdapter(ArrayList<String> showtimesArrayList, ShowtimeClickListener showtimeClickListener) {
        this.showtimeClickListener = showtimeClickListener;
        this.showtimesArrayList = showtimesArrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_showtime)
        RelativeLayout listItemShowtime;
        @BindView(R.id.showtime)
        TextView showtime;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            listItemShowtime = v.findViewById(R.id.list_item_showtime);
            showtime = v.findViewById(R.id.showtime);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_showtime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (row_idex == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#c82229"));
            holder.showtime.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.showtime.setTextColor(Color.parseColor("#DE000000"));
        }

        final String time = showtimesArrayList.get(position);

        holder.showtime.setText(time);

        holder.listItemShowtime.setTag(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                row_idex = position;
                holder.listItemShowtime.setSelected(true);
                showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), time);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() { return showtimesArrayList.size(); }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}