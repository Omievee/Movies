package com.mobile.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mobile.Constants
import com.mobile.model.Movie
import com.mobile.utils.isComingSoon
import com.mobile.utils.releaseDateFormatted
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_dialogfragment_synopsis.*


class SynopsisFragment : MPFragment() {

    var movie: Movie? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        movie = arguments?.getParcelable(Constants.MOVIE)
        return inflater.inflate(R.layout.fr_dialogfragment_synopsis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        synopsisText.text = movie?.synopsis
        val date = movie?.releaseDateFormatted
        synopsisTitle.text = when (movie?.isComingSoon == true && date != null) {
            true -> resources.getString(R.string.in_theaters_date, date)
            else -> movie?.title
        }
    }

    companion object {

        fun newInstance(movie: Movie): SynopsisFragment {
            return SynopsisFragment().apply {
                arguments = Bundle().apply { putParcelable(Constants.MOVIE, movie) }
            }
        }
    }
}


