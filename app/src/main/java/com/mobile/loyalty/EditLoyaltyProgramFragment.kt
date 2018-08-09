package com.mobile.loyalty


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mobile.fragments.MPFragment
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_edit_loyalty_program.*

private const val ARG_PARAM1 = "param1"


class EditLoyaltyProgramFragment : MPFragment(), EditLoyalProgramView {


    private var loyaltyProgram: TheaterChain? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loyaltyProgram = arguments?.getParcelable(ARG_PARAM1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_loyalty_program, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedloyaltyProgram.text = loyaltyProgram?.chainName
    }


    override fun updateLoyaltyProgramInfo() {

    }

    companion object {
        @JvmStatic
        fun newInstance(theater: TheaterChain) =
                EditLoyaltyProgramFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAM1, theater)
                    }
                }
    }
}
