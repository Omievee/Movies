package com.mobile.seats

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moviepass.R
import kotlinx.android.synthetic.main.fragment_mp_bottom_sheet.*

class MPBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mp_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeButton.setOnClickListener {
            dismiss()
        }
        val data = arguments?.getParcelable<SheetData>("data") ?: return
        title.text = data.title
        title.visibility = when {
            title.text.length == 0 -> View.GONE
            else -> View.VISIBLE
        }
        error.text = data.error
        error.visibility = when {
            data.error?.isNotEmpty() == true -> View.VISIBLE
            else -> View.GONE
        }
        description.text = data.description
        subDescription.text = data.subDescription
        subDescription.visibility = when {
            data.subDescription?.isNotEmpty()==true-> View.VISIBLE
            else-> View.GONE
        }
        arrayOf(subDescription, error, title, description).forEach {
            it.gravity = data.gravity
        }

    }

    companion object {
        fun newInstance(sheetData: SheetData): MPBottomSheetFragment {
            return MPBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("data", sheetData)
                }
            }
        }
    }
}