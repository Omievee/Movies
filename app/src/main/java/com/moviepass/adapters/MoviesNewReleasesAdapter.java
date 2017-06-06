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
import com.moviepass.MoviePosterClickListener;
import com.moviepass.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviesNewReleasesAdapter extends RecyclerView.Adapter<MoviesNewReleasesAdapter.ViewHolder> {

    private final MoviePosterClickListener moviePosterClickListener;
    private ArrayList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesNewReleasesAdapter(ArrayList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
        this.moviePosterClickListener = moviePosterClickListener;
        this.moviesArrayList = moviesArrayList;

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
        final Movie movie = moviesArrayList.get(position);

        Picasso.with(holder.itemView.getContext())
                .load(movie.getImageUrl())
                .error(R.mipmap.ic_launcher)
                .into(holder.posterImageView);

        holder.title.setText(movie.getTitle());


        int t = movie.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (movie.getRunningTime() == 0) {
            holder.runTime.setVisibility(View.GONE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            holder.runTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            holder.runTime.setText(translatedRunTime);
        }


        holder.listItemMoviePoster.setTag(position);

        ViewCompat.setTransitionName(holder.posterImageView, movie.getImageUrl());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.posterImageView);
            }
        });
    }

    @Override
    public int getItemCount() { return moviesArrayList.size(); }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}