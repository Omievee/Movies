package com.moviepass.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.meg7.widget.SvgImageView;
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
    public static final String TAG = "found it...";
    private final MoviePosterClickListener moviePosterClickListener;
    private ArrayList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesNewReleasesAdapter(Context context, ArrayList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
        this.moviePosterClickListener = moviePosterClickListener;
        this.moviesArrayList = moviesArrayList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_movie_poster)
        RelativeLayout listItemMoviePoster;
        @BindView(R.id.poster_movie_title)
        TextView title;
        @BindView(R.id.ticket_top_red_dark)
        ImageView mDraweeView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemMoviePoster = v.findViewById(R.id.list_item_movie_poster);
            title = v.findViewById(R.id.poster_movie_title);
            mDraweeView = v.findViewById(R.id.ticket_top_red_dark);

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

        String imgUrl = movie.getImageUrl();
        Log.d(TAG, "onBindViewHolder: " + imgUrl.toString());

//        Uri uri = Uri.parse(movie.getImageUrl());
//        holder.mDraweeView.setImageURI(uri);



//        holder.listItemMoviePoster.setTag(position);
//
//        ViewCompat.setTransitionName(holder.mDraweeView, movie.getImageUrl());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.mDraweeView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moviesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}