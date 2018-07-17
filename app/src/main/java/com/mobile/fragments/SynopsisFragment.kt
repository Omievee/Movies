package com.mobile.fragments

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mobile.Constants
import com.mobile.model.Movie
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_dialogfragment_synopsis.*

class SynopsisFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_dialogfragment_synopsis, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val movie = arguments?.getParcelable<Movie>(Constants.MOVIE)
        synopsisText.text = movie?.synopsis
        synopsisTitle.text = movie?.title
    }

    companion object {

        fun newInstance(movie: Movie): SynopsisFragment {
            val fragment = SynopsisFragment()
            val args = Bundle()
            args.putParcelable(Constants.MOVIE, movie)
            fragment.arguments = args
            return fragment
        }
    }
}


