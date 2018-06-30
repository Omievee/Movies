package com.mobile.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.MoviePosterClickListener;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class DynamicMoviesTabAdapter extends RecyclerView.Adapter<DynamicMoviesTabAdapter.ViewHolder> {

    //Title List
    private List<String> titlesList;

    //Movie List
    private List<List<Movie>> moviesList;

    //Interface
    private MoviePosterClickListener listener;

    /**
     * Constructor
     *
     * @param titlesList - Titles of different sections
     * @param moviesList - Movies on each section
     * @param listener   - Interface
     */
    public DynamicMoviesTabAdapter(List<String> titlesList, List<List<Movie>> moviesList, MoviePosterClickListener listener) {
        this.titlesList = titlesList;
        this.moviesList = moviesList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dynamic_movies_recycler_view, parent, false);
        return new DynamicMoviesTabAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.titleText.setText(titlesList.get(holder.getAdapterPosition()));
        MoviePostersAdapter adapter = new MoviePostersAdapter(moviesList.get(holder.getAdapterPosition()), listener, holder.getAdapterPosition() + 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(holder.titleText.getContext(), LinearLayoutManager.HORIZONTAL, false);
        holder.recyclerView.setLayoutManager(layoutManager);
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return titlesList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        RecyclerView recyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.listTitle);
            recyclerView = itemView.findViewById(R.id.moviesRecyclerView);
        }
    }
}