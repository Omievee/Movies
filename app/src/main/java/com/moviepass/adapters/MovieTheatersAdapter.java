package com.moviepass.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.listeners.ShowtimeClickListener;
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

    MovieShowtimesAdapter ShowtimesAdapter;

    View root;
    private ArrayList<Screening> screeningsArrayList;
    private ArrayList<Theater> theaterArrayList;
    private ArrayList<String> ShowtimesList;
    private ShowtimeClickListener showtimeClickListener;


    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private int selectedPosition = 0;

    public MovieTheatersAdapter(ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener) {
        this.screeningsArrayList = screeningsArrayList;
        this.showtimeClickListener = showtimeClickListener;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.THEATERS_LISTITEM)
        CardView theaterCardViewListItem;
        @BindView(R.id.THEATER_NAME_LISTITEM)
        TextView TheaterName;
        @BindView(R.id.THEATER_ADDRESS2_LISTITEM)
        TextView TheaterAddressListItem;
        @BindView(R.id.THEATER_PIN_LISTITEM)
        ImageView TheaterPin;
        @BindView(R.id.THEATER_SHOWTIMEGRID)
        GridLayout showTimesGrid;

//        @BindView(R.id.icon_ticket)
//        ImageView iconTicket;
//        @BindView(R.id.icon_seat)
//        ImageView iconSeat;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            theaterCardViewListItem = v.findViewById(R.id.THEATERS_LISTITEM);
            TheaterName = v.findViewById(R.id.THEATER_NAME_LISTITEM);
            TheaterAddressListItem = v.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
            TheaterPin = v.findViewById(R.id.THEATER_PIN_LISTITEM);
            showTimesGrid = v.findViewById(R.id.THEATER_SHOWTIMEGRID);
//            iconTicket = v.findViewById(R.id.icon_ticket);
//            iconSeat = v.findViewById(R.id.icon_seat);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        root = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theaters_and_showtimes, parent, false);
        return new ViewHolder(root);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ShowtimesList = new ArrayList<>();

        final Screening screening = screeningsArrayList.get(position);


        holder.TheaterName.setText(screening.getTheaterName());
        holder.TheaterAddressListItem.setText(screening.getTheaterAddress());
        holder.showTimesGrid.setRowCount(1);
        holder.showTimesGrid.setColumnCount(screening.getStartTimes().size());
        holder.showTimesGrid.removeAllViews();
        holder.showTimesGrid.setPadding(40, 10, 40, 10);
        holder.showTimesGrid.setUseDefaultMargins(false);
        holder.showTimesGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        TextView showTime;
        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                showTime = new TextView(root.getContext());
                showTime.setText(screening.getStartTimes().get(i));
                holder.showTimesGrid.addView(showTime);
                Log.d(TAG, "showtimes: " + screening.getStartTimes().get(i));
                showTime.setTextSize(20);
                showTime.setTextColor(root.getResources().getColor(R.color.white));
                showTime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showTime.setPadding(50, 50, 50, 50);
                final TextView finalShowtime = showTime;

                showTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (screening.getFormat().equals("2D")) {
                            if (!finalShowtime.isSelected()) {
                                finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background_selected));
                                finalShowtime.setPadding(50, 50, 50, 50);
                                String selectedShowTime = finalShowtime.getText().toString();
                                showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, selectedShowTime);
                                finalShowtime.setSelected(true);
                            } else {
                                finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                                finalShowtime.setPadding(50, 50, 50, 50);
                                String selectedShowTime = finalShowtime.getText().toString();
                                showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, selectedShowTime);
                                finalShowtime.setSelected(false);
                            }
                        } else {
                            holder.theaterCardViewListItem.setForeground(root.getResources().getDrawable(R.drawable.poster_gradient));
                            Toast.makeText(holder.itemView.getContext(), R.string.Not_Supportd, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                if (!screening.getFormat().equals("2D")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.theaterCardViewListItem.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));

                    }
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return screeningsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }
}
