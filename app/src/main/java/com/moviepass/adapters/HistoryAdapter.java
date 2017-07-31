package com.moviepass.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meg7.widget.SvgImageView;
import com.moviepass.R;
import com.moviepass.model.Movie;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<Movie> historyArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public HistoryAdapter(Context context, ArrayList<Movie> historyArrayList) {
        this.historyArrayList = historyArrayList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_history)
        RelativeLayout listItemHistory;
        @BindView(R.id.poster)
        ImageView posterImageView;
        @BindView(R.id.movie_title)
        TextView title;
        @BindView(R.id.theater)
        TextView theater;
        @BindView(R.id.auditorium)
        TextView auditorium;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.date)
        TextView date;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemHistory = v.findViewById(R.id.list_item_history);
            posterImageView = v.findViewById(R.id.poster);
            title = v.findViewById(R.id.movie_title);
            theater = v.findViewById(R.id.theater);
            auditorium = v.findViewById(R.id.auditorium);
            time = v.findViewById(R.id.time);
            date = v.findViewById(R.id.date);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Movie movie = historyArrayList.get(position);

        String imgUrl = movie.getImageUrl();
        String movieTitle = movie.getTitle();

        long createdAt = movie.getCreatedAt();
        Date date = new Date(createdAt);
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm a", Locale.getDefault());

        Picasso.Builder builder = new Picasso.Builder(context);
        builder.build()
                .load(imgUrl)
                .error(R.drawable.history_ticket_placeholder)
                .centerCrop()
                .fit()
                .into(holder.posterImageView);

        holder.title.setText(movieTitle);
        holder.theater.setText("");
        holder.date.setText(sdf.format(date));
        holder.time.setText(sdfTime.format(date));

        holder.listItemHistory.setTag(position);
    }

    @Override
    public int getItemCount() { return historyArrayList.size(); }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}