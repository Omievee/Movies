package com.moviepass.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
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

public class MoviesTopBoxOfficeAdapter extends RecyclerView.Adapter<MoviesTopBoxOfficeAdapter.ViewHolder> {

    private final MoviePosterClickListener moviePosterClickListener;
    private ArrayList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesTopBoxOfficeAdapter(Context context, ArrayList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
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
        ImageView posterImageView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemMoviePoster = v.findViewById(R.id.my_image_view);
            title = v.findViewById(R.id.poster_movie_title);
            posterImageView = v.findViewById(R.id.ticket_top_red_dark);
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


        Uri uri = Uri.parse(movie.getImageUrl());
        SimpleDraweeView draweeView = (SimpleDraweeView) holder.posterImageView;
        draweeView.setImageURI(uri);
//        Picasso.Builder builder = new Picasso.Builder(context);
//        builder.listener(new Picasso.Listener() {
//            @Override
//            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
//                exception.printStackTrace();
//
//                holder.title.setText(movie.getTitle());
//            }
//        });
//        builder.build()
//                .load(imgUrl)
//                .placeholder(R.drawable.ticket_top_red_dark)
//                .error(R.drawable.ticket_top_red_dark)
//                .into(holder.posterImageView);

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
    public int getItemCount() {
        return moviesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}
