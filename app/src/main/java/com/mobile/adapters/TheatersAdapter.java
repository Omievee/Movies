package com.mobile.adapters;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.listeners.TheatersClickListener;
import com.mobile.model.Header;
import com.mobile.model.Theater;
import com.moviepass.R;

import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheatersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LinkedList<Theater> theatersArrayList;

    public static final String THEATER = "cinema";
    TheatersClickListener theatersClickListener;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    Header header;

    public TheatersAdapter(Header header, TheatersClickListener theatersClickListener, LinkedList<Theater> theatersArrayList) {
        this.theatersArrayList = theatersArrayList;
        this.theatersClickListener = theatersClickListener;
        this.header = header;
    }

    public class VHITEM extends RecyclerView.ViewHolder {

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
        @BindView(R.id.distanceView)
        RelativeLayout distanceView;

        public VHITEM(View v) {
            super(v);
            ButterKnife.bind(this, v);

            listItemTheater = v.findViewById(R.id.list_item_theater);
            name = v.findViewById(R.id.theater_name);
            address = v.findViewById(R.id.theater_address);
            cityThings = v.findViewById(R.id.theater_city_things);
            distance = v.findViewById(R.id.theater_distance);
            iconTicket = v.findViewById(R.id.icon_ticket);
            iconSeat = v.findViewById(R.id.icon_seat);
            distanceView = v.findViewById(R.id.distanceView);
        }
    }

    public class VHHEADER extends RecyclerView.ViewHolder {

        TextView theaterHeader;

        public VHHEADER(View v) {
            super(v);
            theaterHeader = v.findViewById(R.id.theaterHeader);
        }
    }

    private Theater getItem(int position) {
        return theatersArrayList.get(position);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theater, parent, false);
            return new VHITEM(v);
        } else if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theater_header, parent, false);
            return new VHHEADER(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHHEADER) {
            ((VHHEADER) holder).theaterHeader.setText("Nearby");

        } else if (holder instanceof VHITEM) {


            Theater theater = theatersArrayList.get(holder.getLayoutPosition() - 1);


            ((VHITEM) holder).name.setText(theater.getName());

            if (theater.ticketTypeIsStandard()) {
                ((VHITEM) holder).iconTicket.setVisibility(View.INVISIBLE);
                ((VHITEM) holder).iconSeat.setVisibility(View.INVISIBLE);
            } else if (theater.ticketTypeIsETicket()) {
                ((VHITEM) holder).iconSeat.setVisibility(View.INVISIBLE);
            } else {
                ((VHITEM) holder).iconSeat.setVisibility(View.VISIBLE);
                ((VHITEM) holder).iconTicket.setVisibility(View.VISIBLE);
            }


            ((VHITEM) holder).address.setText(theater.getAddress());


            String city = theater.getCity();
            String state = theater.getState();
            String zip = String.valueOf(theater.getZip());

            String cityThings = city + ", " + state + " " + zip;
            ((VHITEM) holder).cityThings.setText(cityThings);

            String formattedAddress = theater.getDistance() + " miles";
            ((VHITEM) holder).distance.setText(formattedAddress);

            final Uri uri = Uri.parse("geo:" + theater.getLat() + "," + theater.getLon() + "?q=" + Uri.encode(theater.getName()));
            ((VHITEM) holder).distanceView.setOnClickListener(v -> {
                try {
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    holder.itemView.getContext().startActivity(mapIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(holder.itemView.getContext(), "Google Maps isn't installed", Toast.LENGTH_SHORT).show();
                } catch (Exception x) {
                    x.getMessage();
                }

            });

            ((VHITEM) holder).listItemTheater.setTag(position);
            Theater finalTheater = theater;
            holder.itemView.setOnClickListener(v -> {
                theatersClickListener.onTheaterClick(holder.getAdapterPosition(), finalTheater);
            });

        }
    }


//    @Override
//    public void onBindViewHolder(final ViewHolder holder, int position) {
//
//        if (theater.ticketTypeIsStandard()) {
//            holder.iconTicket.setVisibility(View.INVISIBLE);
//            holder.iconSeat.setVisibility(View.INVISIBLE);
//        } else if (theater.ticketTypeIsETicket()) {
//            holder.iconSeat.setVisibility(View.INVISIBLE);
//        } else {
//            holder.iconSeat.setVisibility(View.VISIBLE);
//            holder.iconTicket.setVisibility(View.VISIBLE);
//        }
//
//
//        holder.name.setText(theater.getName());
//        holder.address.setText(theater.getAddress());
//
//
//        String city = theater.getCity();
//        String state = theater.getState();
//        String zip = String.valueOf(theater.getZip());
//
//        String cityThings = city + ", " + state + " " + zip;
//        holder.cityThings.setText(cityThings);
//
//        String formattedAddress = theater.getDistance() + " miles";
//        holder.distance.setText(formattedAddress);
//
//        final Uri uri = Uri.parse("geo:" + theater.getLat() + "," + theater.getLon() + "?q=" + Uri.encode(theater.getName()));
//        holder.distanceView.setOnClickListener(v -> {
//            try {
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
//                mapIntent.setPackage("com.google.android.apps.maps");
//                holder.itemView.getContext().startActivity(mapIntent);
//            } catch (ActivityNotFoundException e) {
//                Toast.makeText(holder.itemView.getContext(), "Google Maps isn't installed", Toast.LENGTH_SHORT).show();
//            } catch (Exception x) {
//                x.getMessage();
//            }
//
//        });
//
//        holder.listItemTheater.setTag(position);
//        holder.itemView.setOnClickListener(v -> {
//            theatersClickListener.onTheaterClick(holder.getAdapterPosition(), theater);
//        });
//    }

    @Override
    public int getItemCount() {
        return theatersArrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


}