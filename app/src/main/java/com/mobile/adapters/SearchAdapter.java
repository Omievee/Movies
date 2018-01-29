package com.mobile.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.view.LayoutInflater;
import android.widget.Filter;

import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.List;

import com.mobile.model.Movie;
import com.moviepass.R;

/**
 * Created by o_vicarra on 1/29/18.
 */

public class SearchAdapter extends SuggestionsAdapter<Movie, SearchAdapter.SuggestionHolder> {


    View root;

    public SearchAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    @Override
    public void onBindSuggestionHolder(Movie suggestion, SuggestionHolder holder, int position) {
        holder.title.setText(suggestion.getTitle());
    }

    @Override
    public int getSingleViewHeight() {
        return 10;
    }

    @Override
    public SuggestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        root = getLayoutInflater().inflate(R.layout.list_item_moviesearch, parent, false);


        return new SuggestionHolder(root);
    }

    static class SuggestionHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView subtitle;
        protected ImageView image;

        public SuggestionHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.MovieSearch_Title);
        }
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();

                String movieSearch = charSequence.toString();
                if (movieSearch.isEmpty()) {
                    suggestions = suggestions_clone;
                } else {
                    suggestions = new ArrayList<>();
                    for (Movie movieTitle : suggestions_clone)
                        if (movieTitle.getTitle().toLowerCase().contains(movieSearch.toLowerCase()))
                            suggestions.add(movieTitle);
                }
                results.values = suggestions;


                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                suggestions = (List<Movie>) filterResults.values;
                notifyDataSetChanged();
            }
        };

    }
}

