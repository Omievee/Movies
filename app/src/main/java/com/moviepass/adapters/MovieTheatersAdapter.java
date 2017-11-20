package com.moviepass.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.listeners.MovieTheaterClickListener;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieTheatersAdapter extends RecyclerView.Adapter<MovieTheatersAdapter.ViewHolder> {
    public static final String TAG = "Showtimes/";

    private ArrayList<Screening> screeningsArrayList;
    private ArrayList<Theater> theaterArrayList;
    private final MovieTheaterClickListener movieTheaterClickListener;


    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private int selectedPosition = -1;

    public MovieTheatersAdapter(ArrayList<Screening> screeningsArrayList, MovieTheaterClickListener movieTheaterClickListener) {
        this.screeningsArrayList = screeningsArrayList;
        this.movieTheaterClickListener = movieTheaterClickListener;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.THEATERS_LISTITEM)
        CardView TheaterListItem;
        @BindView(R.id.THEATER_NAME_LISTITEM)
        TextView TheaterName;
        @BindView(R.id.THEATER_ADDRESS2_LISTITEM)
        TextView TheaterAddressListItem;
        @BindView(R.id.SHOWTIME_RECYCLER)
        RecyclerView TheaterShowtimesRecycler;
        @BindView(R.id.THEATER_PIN_LISTITEM)
        ImageView TheaterPin;
        @BindView(R.id.SHOWTIME_CARD)
        TextView TheaterShowtimeCard;


//        @BindView(R.id.icon_ticket)
//        ImageView iconTicket;
//        @BindView(R.id.icon_seat)
//        ImageView iconSeat;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            TheaterListItem = v.findViewById(R.id.THEATERS_LISTITEM);
            TheaterName = v.findViewById(R.id.THEATER_NAME_LISTITEM);
            TheaterAddressListItem = v.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
            TheaterShowtimesRecycler = v.findViewById(R.id.SHOWTIME_RECYCLER);
            TheaterPin = v.findViewById(R.id.THEATER_PIN_LISTITEM);
            TheaterShowtimeCard = v.findViewById(R.id.SHOWTIME_CARD);
//            iconTicket = v.findViewById(R.id.icon_ticket);
//            iconSeat = v.findViewById(R.id.icon_seat);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theaters_and_showtimes, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Screening screening = screeningsArrayList.get(position);


        if (position == selectedPosition) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        holder.TheaterListItem.setTag(position);

        holder.TheaterName.setText(screening.getTheaterName());
        holder.TheaterAddressListItem.setText(screening.getTheaterAddress());
        holder.TheaterName.setText(screening.getTheaterName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
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
                movieTheaterClickListener.onTheaterClick(holder.getAdapterPosition(), screening);
            }
        });

    }

    @Override
    public int getItemCount() {
        return screeningsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}
