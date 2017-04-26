package com.moviepass.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moviepass.R;
import com.moviepass.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviesNewReleasesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private View.OnClickListener onClick;
    private ArrayList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesNewReleasesAdapter(Context context, View.OnClickListener onClick, ArrayList<Movie> moviesArrayList) {
        this.context = context;
        this.onClick = onClick;
        this.moviesArrayList = moviesArrayList;
        if (context != null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.poster)
        ImageView imageMovie;

        public ViewHolder(View v) {
            super(v); ButterKnife.bind(this, v);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Movie movie = moviesArrayList.get(position);

        Picasso.with(context).load(movie.getImageUrl()).placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher).into(((ViewHolder) holder).imageMovie);

        ((ViewHolder) holder).imageMovie.setOnClickListener(onClick);
    }

    @Override
    public int getItemCount() { return moviesArrayList.size(); }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }
}
