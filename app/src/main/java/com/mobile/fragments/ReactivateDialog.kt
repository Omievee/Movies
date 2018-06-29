package com.mobile.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.mobile.activities.LogInActivity
import com.moviepass.R


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ReactivateDialog : BottomSheetDialogFragment() {

    var reactivateLink: View? = null
    var closeButton: ImageView? = null
    var activity: LogInActivity? = null


    private var param1: String? = null
    private var param2: String? = null
//    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reactivate_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reactivateLink = view.findViewById(R.id.reactivate)
        closeButton = view.findViewById(R.id.closeButton)

        reactivateLink?.setOnClickListener {
            activity?.openWebVIew()
            dismiss()
        }

        closeButton?.setOnClickListener {
            dismiss()
        }

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as LogInActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ReactivateDialog().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
