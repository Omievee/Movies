package com.moviepass.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v13.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.listeners.ScreeningPosterClickListener;
import com.moviepass.model.Screening;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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
    private int selectedPosition = -1;

    public TheaterMoviesAdapter(ArrayList<Screening> screeningsArrayList, ScreeningPosterClickListener screeningPosterClickListener) {
        this.screeningPosterClickListener = screeningPosterClickListener;
        this.screeningsArrayList = screeningsArrayList;
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

            listItemMoviePoster = v.findViewById(R.id.list_item_movie_poster);
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
        final Screening screening = screeningsArrayList.get(position);

        if (position == selectedPosition) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        String imgUrl = screening.getImageUrl();

        if (imgUrl.isEmpty()) {
            Picasso.Builder builder = new Picasso.Builder(holder.itemView.getContext());
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    exception.printStackTrace();

                    holder.title.setText(screening.getTitle());
                }
            });
            builder.build()
                    .load(R.drawable.ticket_top_red_dark)
                    .placeholder(R.drawable.ticket_top_red_dark)
                    .error(R.drawable.ticket_top_red_dark)
                    .into(holder.posterImageView);
            holder.title.setText(screening.getTitle());
        } else {
            Picasso.Builder builder = new Picasso.Builder(holder.itemView.getContext());
            builder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    exception.printStackTrace();

                    holder.title.setText(screening.getTitle());
                }
            });
            builder.build()
                    .load(imgUrl)
                    .placeholder(R.drawable.ticket_top_red_dark)
                    .error(R.drawable.ticket_top_red_dark)
                    .into(holder.posterImageView);
        }

        holder.listItemMoviePoster.setTag(position);

        ViewCompat.setTransitionName(holder.posterImageView, screening.getImageUrl());

        final List<String> startTimes = screening.getStartTimes();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int currentPosition = holder.getLayoutPosition();
                if (selectedPosition != currentPosition) {

                    // Show Ripple and then change color
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Temporarily save the last selected position
                            int lastSelectedPosition = selectedPosition;
                            // Save the new selected position
                            selectedPosition = currentPosition;
                            // update the previous selected row
                            notifyItemChanged(lastSelectedPosition);
                            // select the clicked row
                            holder.itemView.setSelected(true);
                        }
                    }, 150);
                }

                screeningPosterClickListener.onScreeningPosterClick(holder.getAdapterPosition(), screening, startTimes, holder.posterImageView);
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