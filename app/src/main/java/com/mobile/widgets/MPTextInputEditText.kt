package com.mobile.widgets

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputLayout
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_mp_text_input_layout.view.*

class MPTextInputEditText(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.layout_mp_text_input_layout, this)
        val array = context.obtainStyledAttributes(attrs, R.styleable.MPTextInputEditText)
        textInputLayoutView.hint = array.getString(R.styleable.MPTextInputEditText_android_hint)
        textInputEditTextView.inputType = array.getInt(R.styleable.MPTextInputEditText_android_inputType, InputType.TYPE_TEXT_VARIATION_NORMAL)
        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(array.getInt(R.styleable.MPTextInputEditText_android_maxLength, 500))
        textInputEditTextView.filters = fArray
        textInputEditTextView.maxLines = array.getInt(R.styleable.MPTextInputEditText_android_maxLines, 100)
        textInputEditTextView.nextFocusDownId = array.getInt(R.styleable.MPTextInputEditText_android_nextFocusDown, 0)
        textInputEditTextView.nextFocusRightId = array.getInt(R.styleable.MPTextInputEditText_android_nextFocusRight, 0)
        if (array.getBoolean(R.styleable.MPTextInputEditText_mp_endOfString, false)) {
            setSelectionToLast()
        }
        textInputLayoutView.isErrorEnabled = true
    }

    var text: String?
        get() {
            return textInputEditTextView.text.toString()
        }
        set(value) {
            textInputEditTextView.setText(value)
        }

    var editText : EditText = textInputEditTextView
    var textInputLayout: TextInputLayout = textInputLayoutView

    var error : String?
        get() = textInputLayoutView.error.toString()
        set(value) {
            textInputLayoutView.error = value
        }


    private fun setSelectionToLast() {
        textInputEditTextView.setOnTouchListener { p0, p1 ->
            textInputEditTextView.onTouchEvent(p1)
            textInputEditTextView.setSelection(textInputEditTextView.text.length)
            true
        }
    }

    fun addTextChangedListener(textWatcher: TextWatcher): MPTextInputEditText {
        textInputEditTextView.addTextChangedListener(textWatcher)
        return this
    }

    fun removeTextChangedListener(textWatcher: TextWatcher) : MPTextInputEditText {
        textInputEditTextView.removeTextChangedListener(textWatcher)
        return this
    }



}