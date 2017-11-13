package com.moviepass.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.braintreepayments.api.Card;
import com.moviepass.R;
import com.moviepass.listeners.TheatersClickListener;
import com.moviepass.model.Theater;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheatersAdapter extends RecyclerView.Adapter<TheatersAdapter.ViewHolder> {

    private final TheatersClickListener theatersClickListener;
    private ArrayList<Theater> theatersArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public TheatersAdapter(ArrayList<Theater> theatersArrayList, TheatersClickListener theatersClickListener) {
        this.theatersClickListener = theatersClickListener;
        this.theatersArrayList = theatersArrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_theater)
        CardView listItemTheater;
        @BindView(R.id.theater_name)
        TextView name;
        @BindView(R.id.theater_address)
        TextView address;
        @BindView(R.id.theater_city_things)
        TextView cityThings;
        @BindView(R.id.theater_distance)
        TextView distance;
        @BindView(R.id.icon_ticket)
        ImageView iconTicket;
        @BindView(R.id.icon_seat)
        ImageView iconSeat;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            listItemTheater = v.findViewById(R.id.list_item_theater);
            name = v.findViewById(R.id.theater_name);
            address = v.findViewById(R.id.theater_address);
            cityThings = v.findViewById(R.id.theater_city_things);
            distance = v.findViewById(R.id.theater_distance);
            iconTicket = v.findViewById(R.id.icon_ticket);
            iconSeat = v.findViewById(R.id.icon_seat);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theater, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Theater theater = theatersArrayList.get(position);

        holder.name.setText(theater.getName());
        holder.address.setText(theater.getAddress());

/*
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        float distanceInMeters = loc1.distanceTo(loc2); */

        String city = theater.getCity();
        String state = theater.getState();
        String zip = String.valueOf(theater.getZip());

        String cityThings = city + ", " + state + " " + zip;
        holder.cityThings.setText(cityThings);

        String formattedAddress = theater.getDistance() + " miles";
        holder.distance.setText(formattedAddress);

        if (theater.ticketTypeIsStandard()) {
            holder.iconTicket.setVisibility(View.INVISIBLE);
            holder.iconSeat.setVisibility(View.INVISIBLE);
        } else if (theater.ticketTypeIsETicket()) {
            holder.iconSeat.setVisibility(View.INVISIBLE);
        }

        holder.listItemTheater.setTag(position);

        setSlideAnimation(holder.listItemTheater);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theatersClickListener.onTheaterClick(holder.getAdapterPosition(), theater, (int) holder.itemView.getX(), (int) holder.itemView.getY());
            }
        });
    }

    @Override
    public int getItemCount() {
        return theatersArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    private void setSlideAnimation(View view) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.slide_up);
        view.startAnimation(animation);
    }
}