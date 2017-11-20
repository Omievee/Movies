package com.moviepass.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.moviepass.activities.ViewTheatersActivity;
import com.moviepass.listeners.MovieTheaterClickListener;
import com.moviepass.R;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieTheatersAdapter extends RecyclerView.Adapter<MovieTheatersAdapter.ViewHolder> {


    private ArrayList<Screening> screeningsArrayList;
    private ArrayList<Theater> theaterArrayList;


    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private int selectedPosition = -1;

    public MovieTheatersAdapter(ArrayList<Screening> screeningsArrayList) {
        this.screeningsArrayList = screeningsArrayList;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.THEATERS_LISTITEM)
        CardView TheaterListItem;
        @BindView(R.id.THEATER_NAME_LISTITEM)
        TextView TheaterName;
        @BindView(R.id.THEATER_ADDRESS_LISTITEM)
        TextView TheaterAddressListItem;
        @BindView(R.id.THEATER_ADDRESS2_LISTITEM)
        TextView TheaterCityAddressListItem;
        @BindView(R.id.SHOWTIME_LIST)
        ListView TheaterShowtimes;
        @BindView(R.id.THEATER_PIN_LISTITEM)
        ImageView TheaterPin;
//        @BindView(R.id.THEATER_SHOWTIME_LISTITEM)
//        RecyclerView TheaterShowTimesListItem;

//        @BindView(R.id.icon_ticket)
//        ImageView iconTicket;
//        @BindView(R.id.icon_seat)
//        ImageView iconSeat;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            TheaterListItem = v.findViewById(R.id.THEATERS_LISTITEM);
            TheaterName = v.findViewById(R.id.THEATER_NAME_LISTITEM);
            TheaterAddressListItem = v.findViewById(R.id.THEATER_ADDRESS_LISTITEM);
            TheaterCityAddressListItem = v.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
            TheaterShowtimes = v.findViewById(R.id.SHOWTIME_LIST);
            TheaterPin = v.findViewById(R.id.THEATER_PIN_LISTITEM);
//            TheaterShowTimesListItem = v.findViewById(R.id.THEATER_SHOWTIME_LISTITEM);
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

        holder.TheaterName.setText(screening.getTheaterName());
        holder.TheaterAddressListItem.setText(screening.getTheaterAddress());
        holder.TheaterName.setText(screening.getTheaterName());

        holder.TheaterListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.TheaterShowtimes.getVisibility() == View.GONE) {
                    holder.TheaterShowtimes.setVisibility(View.VISIBLE);
//                    holder.TheaterShowtimes

                } else {
                    holder.TheaterShowtimes.setVisibility(View.GONE);

                }
            }
        });
//        holder.TheaterListItem.setTag(position);
    }

    @Override
    public int getItemCount() {
        return screeningsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }


//    public class showtimesAdapter extends ArrayAdapter<Screening> {
//
//        public showtimesAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Screening> objects) {
//            super(context, resource, textViewResourceId, objects);
//        }
//
//        public showtimesAdapter(@NonNull Context context, int resource) {
//            super(context, resource);
//
//
//        }
//
//        @NonNull
//        @Override
//        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//
//
//            return super.getView(position, convertView, parent);
//
//        }
//    }


}

/*
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        float distanceInMeters = loc1.distanceTo(loc2); */

//        holder.TheaterCityAddressListItem.setVisibility(View.GONE);
//        holder.TheaterDistance.setVisibility(View.GONE);
//        holder.TheaterPin.setVisibility(View.GONE);

//        if (screening.getProvider().ticketTypeIsStandard()) {
//            holder.iconTicket.setVisibility(View.INVISIBLE);
//            holder.iconSeat.setVisibility(View.INVISIBLE);
//        } else if (screening.getProvider().ticketTypeIsETicket()) {
//            holder.iconSeat.setVisibility(View.INVISIBLE);
//        }
//
//        String city = screening.getTheaterAddress();
//        String state = screening.getTheaterAddress();
//        String zip = String.valueOf(screening.getTheaterAddress());
//
//        String cityThings = city + ", " + state + " " + zip;

//        holder.TheaterDistance.setText((int) viewTheaters.mTheaters.get(position).getDistance());

//        String formattedAddress = screening.getTheaterAddress() + " miles";
//        //holder.TheaterDistance.setText(formattedAddress);


//        holder.itemView.setSelected(holder.itemView.isSelected());
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final int currentPosition = holder.getLayoutPosition();
//                if (selectedPosition != currentPosition) {
//
//                    // Show Ripple and then change color
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Temporarily save the last selected position
//                            int lastSelectedPosition = selectedPosition;
//                            // Save the new selected position
//                            selectedPosition = currentPosition;
//                            // update the previous selected row
//                            notifyItemChanged(lastSelectedPosition);
//                            // select the clicked row
//                            holder.itemView.setSelected(true);
//                        }
//                    }, 150);
//
//                }
////                movieTheaterClickListener.onTheaterClick(holder.getAdapterPosition(), screening);
//            }
//        });