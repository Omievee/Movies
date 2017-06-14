package com.moviepass.adapters;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.TheatersClickListener;
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

    private final static int FADE_DURATION = 1000;

    public TheatersAdapter(ArrayList<Theater> theatersArrayList, TheatersClickListener theatersClickListener) {
        this.theatersClickListener = theatersClickListener;
        this.theatersArrayList = theatersArrayList;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_theater)
        LinearLayout listItemTheater;
        @BindView(R.id.theater_name)
        TextView name;
        @BindView(R.id.theater_address)
        TextView address;
        @BindView(R.id.theater_distance)
        TextView distance;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            listItemTheater = v.findViewById(R.id.list_item_theater);
            name = v.findViewById(R.id.theater_name);
            address = v.findViewById(R.id.theater_address);
            distance = v.findViewById(R.id.theater_distance);
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

        holder.name.setText(theater.getTitle());
        holder.address.setText(theater.getAddress());

/*
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        float distanceInMeters = loc1.distanceTo(loc2); */

        String formattedAddress = theater.getDistance() + " miles";
        holder.distance.setText(formattedAddress);

        holder.listItemTheater.setTag(position);

//        animate(holder);

//        ViewCompat.setTransitionName(holder.posterImageView, movie.getImageUrl());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theatersClickListener.onTheaterClick(holder.getAdapterPosition(), theater);
            }
        });
    }

    @Override
    public int getItemCount() { return theatersArrayList.size(); }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        viewHolder.itemView.setAnimation(animAnticipateOvershoot);
    }
}