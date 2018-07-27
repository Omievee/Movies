package com.mobile.theater

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_map_search_box.view.*

class MapSearchBox(context: Context?, attrs: AttributeSet?=null) : ConstraintLayout(context, attrs) {

    var listener:MapSearchBoxListener? = null

    init {
        inflate(context, R.layout.layout_map_search_box, this)
        discardOrClose
                .setOnClickListener {
                    when(editText.text.isEmpty()) {
                        true-> {
                            listener?.onClose()
                        }
                        false-> editText.setText("")
                    }
                }

        editText.setOnKeyListener({ _: View?, keyCode: Int, _: KeyEvent? ->
            when(keyCode) {
                KeyEvent.KEYCODE_ENTER,KeyEvent.KEYCODE_SEARCH-> {
                    listener?.onSearch(editText.text.toString())
                    true
                }
                else-> false

            }
        })
    }
}

interface MapSearchBoxListener {
    /**
     * Should probably close the keyboard within this listener
     */
    fun onClose()

    fun onSearch(query:String)

}