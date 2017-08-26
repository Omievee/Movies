package com.moviepass.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.activities.MovieActivity;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.model.Screening;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieShowtimesAdapter extends RecyclerView.Adapter<MovieShowtimesAdapter.ViewHolder> {

    private final ShowtimeClickListener showtimeClickListener;
    private ArrayList<String> showtimesArrayList;
    private Screening screening;
    private int screenWidth;
    int row_index;
    private int selectedPosition = -1;


    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private int imageWidth = 0;

    public MovieShowtimesAdapter(ArrayList<String> showtimesArrayList, Screening screening, ShowtimeClickListener showtimeClickListener, int screenWidth) {
        this.showtimeClickListener = showtimeClickListener;
        this.screening = screening;
        this.showtimesArrayList = showtimesArrayList;
        this.screenWidth = screenWidth;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.relative_layout)
        RelativeLayout relativeLayout;
        @BindView(R.id.showtime)
        TextView showtime;
        SparseBooleanArray selectedItems = null;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            relativeLayout = v.findViewById(R.id.relative_layout);
            showtime = v.findViewById(R.id.showtime);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_showtime, parent, false);

        int itemWidth = screenWidth / 4;
        view.setMinimumWidth(itemWidth);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position == selectedPosition) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        final String time = showtimesArrayList.get(position);

        holder.showtime.setText(time);
        holder.relativeLayout.setSelected(holder.relativeLayout.isSelected());
        holder.relativeLayout.setTag(position);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int currentPosition = holder.getLayoutPosition();
                if (selectedPosition != currentPosition) {

                    // Show Ripple and then change color
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Temporarily save the last selected position
                            int lastSelectedPosition = selectedPosition;
                            // Save the new selected position
                            selectedPosition = currentPosition;
                            // update the previous selected row
                            notifyItemChanged(lastSelectedPosition);
                            // select the clicked row
                            holder.itemView.setSelected(true);
                        }
                    }, 150);

                }

                showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, time);
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