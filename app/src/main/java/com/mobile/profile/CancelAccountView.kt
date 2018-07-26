package com.mobile.profile

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.moviepass.R
import kotlinx.android.synthetic.main.edit_text_with_digits_counter.view.*

class CancelAccountView(context: Context, attributeSet: AttributeSet?) : ConstraintLayout(context, attributeSet) {

    var totalCount: Int = 0

    init {
        inflate(context, R.layout.edit_text_with_digits_counter, this)
        bind()
    }

    fun bind(){
        totalCount = 0
        counter.text = totalCount.toString().plus(resources.getString(R.string.counter_characters))

        comments.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                totalCount = s?.length ?: 0
                counter.text = totalCount.toString().plus(resources.getString(R.string.counter_characters))
            }
        })
    }

    fun getComments(): String{
        return comments.text.toString()
    }
}