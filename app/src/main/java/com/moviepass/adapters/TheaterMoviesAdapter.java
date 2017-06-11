package com.moviepass.adapters;

import android.content.Context;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.ScreeningPosterClickListener;
import com.moviepass.model.Screening;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheaterMoviesAdapter extends RecyclerView.Adapter<TheaterMoviesAdapter.ViewHolder> {

    private final ScreeningPosterClickListener screeningPosterClickListener;
    private ArrayList<Screening> screeningsArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public TheaterMoviesAdapter(ArrayList<Screening> screeningsArrayList, ScreeningPosterClickListener screeningPosterClickListener) {
        this.screeningPosterClickListener = screeningPosterClickListener;
        this.screeningsArrayList = screeningsArrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_movie_poster)
        RelativeLayout listItemMoviePoster;
        @BindView(R.id.text_title)
        TextView title;
        @BindView(R.id.text_run_time)
        TextView runTime;
        @BindView(R.id.poster)
        ImageView posterImageView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Screening screening = screeningsArrayList.get(position);

        if (screening.getImageUrl().isEmpty()) {
            Picasso.with(holder.itemView.getContext())
                    .load(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(holder.posterImageView);
        } else {
            Picasso.with(holder.itemView.getContext())
                    .load(screening.getImageUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.posterImageView);
        }

        holder.title.setText(screening.getTitle());

        int t = screening.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (screening.getRunningTime() == 0) {
            holder.runTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            holder.runTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            holder.runTime.setText(translatedRunTime);
        }

        holder.listItemMoviePoster.setTag(position);

        ViewCompat.setTransitionName(holder.posterImageView, screening.getImageUrl());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screeningPosterClickListener.onScreeningPosterClick(holder.getAdapterPosition(), screening, holder.posterImageView);
            }
        });
    }

    @Override
    public int getItemCount() { return screeningsArrayList.size(); }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}