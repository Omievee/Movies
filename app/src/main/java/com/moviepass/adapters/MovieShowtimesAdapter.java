package com.moviepass.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moviepass.R;
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
    private boolean qualifiersApproved;
    int row_index;
    private int selectedPosition = -1;


    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private int imageWidth = 0;
    Snackbar snackbar;

    public MovieShowtimesAdapter(Context context, ArrayList<String> showtimesArrayList, Screening screening, ShowtimeClickListener showtimeClickListener, int screenWidth, boolean qualifiersApproved) {
        this.context = context;
        this.showtimeClickListener = showtimeClickListener;
        this.screening = screening;
        this.showtimesArrayList = showtimesArrayList;
        this.screenWidth = screenWidth;
        this.qualifiersApproved = qualifiersApproved;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.relative_layout)
        CardView ShowtimeCardview;
        SparseBooleanArray selectedItems = null;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_showtime, parent, false);
//        int itemWidth = screenWidth / 4;
//        view.setMinimumWidth(itemWidth);
//        return new ViewHolder(view);
        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder currentHolder, final int position) {
        if (position == selectedPosition) {
            currentHolder.itemView.setSelected(true);
        } else {
            currentHolder.itemView.setSelected(false);
        }

        final String time = showtimesArrayList.get(position);
//        currentHolder.ScreeningShowtimeListItem.setText(time);

        if (qualifiersApproved) {
            currentHolder.ShowtimeCardview.setSelected(currentHolder.ShowtimeCardview.isSelected());
            currentHolder.ShowtimeCardview.setTag(position);
            currentHolder.ShowtimeCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (snackbar != null && snackbar.isShown()) {
                        snackbar.dismiss();
                    }
                    final int currentPosition = currentHolder.getLayoutPosition();
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
                                currentHolder.itemView.setSelected(true);
                            }
                        }, 150);

                    }
                    showtimeClickListener.onShowtimeClick(currentHolder.getAdapterPosition(), screening, time);
                }
            });
        } else {
            currentHolder.ShowtimeCardview.setBackgroundColor(context.getResources().getColor(R.color.gray_dark));
            currentHolder.ShowtimeCardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.make(
                            currentHolder.itemView, R.string.adapter_movie_showtimes_adapter,
                            Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return showtimesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}