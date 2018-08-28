package com.mobile.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class MPTextWatcher {

    private var callback: TextWatcherInterface? = null
    private var textWatcher: TextWatcher ? = null

    fun setCallBack(callback: TextWatcherInterface) : MPTextWatcher {
        this.callback = callback
        return this
    }

    fun registerEditText(editText: EditText) : MPTextWatcher {
        textWatcher = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                callback?.afterTextChanged(s, editText)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                callback?.beforeTextChanged(s,start,count,after, editText)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                callback?.onTextChanged(s,start,before,count)
            }
        }
        editText.addTextChangedListener(textWatcher)
        return this
    }

    fun unregisterEditText(editText: EditText) : MPTextWatcher {
       editText.removeTextChangedListener(textWatcher)
        return this
    }
}

interface TextWatcherInterface{
    fun afterTextChanged(s: Editable?, editText: EditText)
    fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int, editText: EditText)
    fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
}
