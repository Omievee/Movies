package com.moviepass.adapters;

import android.content.Context;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moviepass.R;
import com.moviepass.MoviePosterClickListener;
import com.moviepass.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviesTopBoxOfficeAdapter extends RecyclerView.Adapter<MoviesTopBoxOfficeAdapter.ImageViewHolder> {

    private final MoviePosterClickListener moviePosterClickListener;
    private ArrayList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesTopBoxOfficeAdapter(ArrayList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
        this.moviePosterClickListener = moviePosterClickListener;
        this.moviesArrayList = moviesArrayList;

    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, int position) {
        final Movie movie = moviesArrayList.get(position);

        Picasso.with(holder.itemView.getContext())
                .load(movie.getImageUrl())
                .error(R.mipmap.ic_launcher)
                .into(holder.posterImageView);

        holder.posterImageView.setTag(position);

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

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView posterImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            posterImageView = (ImageView) itemView.findViewById(R.id.poster);
        }
    }

}
