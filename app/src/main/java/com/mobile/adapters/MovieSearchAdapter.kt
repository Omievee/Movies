package com.mobile.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.TextView

import com.mobile.model.Movie
import com.moviepass.R

/**
 * Created by o_vicarra on 11/21/17.
 */

class MovieSearchAdapter(context: Context, internal var movieSearchList: List<Movie>)  : ArrayAdapter<Movie> (context, R.layout.list_item_moviesearch, movieSearchList), Filterable{
    internal lateinit var MovieTitle_Search: TextView
    internal lateinit var MovieRating_Search: TextView



//    constructor(context: Context, resource: Int, objects: List<Movie>) : super(context, resource, objects)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView: View

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.list_item_moviesearch, parent, false)

        } else {

            rowView = convertView
        }
//        val movieSearchList: LayoutInflater
//        movieSearchList = LayoutInflater.from(context)
//        movieSearchList.inflate(R.layout.list_item_moviesearch, null)


        val search = getItem(position)
        if (search != null) {
            MovieTitle_Search = rowView.findViewById(R.id.MovieSearch_Title)
            MovieRating_Search = rowView.findViewById(R.id.MovieSearch_Rating)

            MovieTitle_Search.text = search.title
            MovieRating_Search.text = "Rated: " + search.rating

        }


        return rowView
    }

    override fun getCount(): Int {
        return movieSearchList!!.size
    }
}
