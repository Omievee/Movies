package com.mobile.fragments

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moviepass.R
import kotlinx.android.synthetic.main.fr_theaterpolicy.*

class TheaterPolicy : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fr_theaterpolicy, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val theaterText = arguments!!.getString(POLICY)

        theaterName.text = theaterText

        close.setOnClickListener { v ->
            dismiss()
        }
    }

    companion object {

        const val POLICY = "policy"

        fun newInstance(movie: String): TheaterPolicy {
            val fragment = TheaterPolicy()
            val args = Bundle()
            args.putString(POLICY, movie)
            fragment.arguments = args
            return fragment
        }
    }
}
