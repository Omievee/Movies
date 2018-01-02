package com.moviepass.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.fragments.SignUpStepOneFragment;
import com.moviepass.model.PlaceAPI;

import java.util.ArrayList;

/**
 * Created by o_vicarra on 1/2/18.
 */

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    SignUpStepOneFragment signUpStepOneFragment = new SignUpStepOneFragment();
    Context context;
    public PlaceAPI api = new PlaceAPI();
    int resource;
    public ArrayList<String> placesResults = new ArrayList<>();


    public PlacesAutoCompleteAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.context = context;
        this.resource = resource;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        //if (convertView == null) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (position != (placesResults.size() - 1))
            view = inflater.inflate(R.layout.list_item_autocomplete_places, null);
        else
            view = inflater.inflate(R.layout.list_item_autocomplete_places, null);

        if (position != (placesResults.size() - 1)) {
            TextView autocompleteTextView = view.findViewById(R.id.Autocomplete_Results);
            autocompleteTextView.setText(placesResults.get(position));
        }

        return view;
    }

    @Override
    public int getCount() {
        return placesResults.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return placesResults.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    api.autocomplete(constraint.toString());


                    results.values = placesResults;
                    results.count = placesResults.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };


        return filter;
    }
}
