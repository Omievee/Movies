package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mobile.model.Movie;
import com.moviepass.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by omievee on 1/29/18.
 */

public class MovieSearchAdapter extends BaseAdapter implements Filterable {

    private ItemFilter mFilter = new ItemFilter();
    private List<Movie> movieList;
    private List<Movie> moviesFiltered;
    private LayoutInflater mInflater;

    public MovieSearchAdapter(Context context, List<Movie> movies) {
        this.movieList = movies;
        this.moviesFiltered = movies;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return movieList.size();
    }

    @Override
    public Object getItem(int position) {
        return moviesFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_moviesearch, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.MovieSearch_Title);

            // Bind the data efficiently with the holder.

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // If weren't re-ordering this you could rely on what you set last time
        holder.text.setText((CharSequence) moviesFiltered.get(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }


    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Movie> list = movieList;

            int count = list.size();
            final ArrayList<Movie> nlist = new ArrayList<>(count);

            Movie filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i);
                if (filterableString.getTitle().toLowerCase().contains(filterString)) {
                    nlist.add(filterableString);
                }
            }
            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            moviesFiltered = (List<Movie>) filterResults.values;
            notifyDataSetChanged();
        }
    }

    static class ViewHolder {

        public TextView text;

    }
}
