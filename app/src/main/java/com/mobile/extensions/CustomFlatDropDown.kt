package com.mobile.extensions

import android.content.Context
import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder
import com.moviepass.R
import kotlinx.android.synthetic.main.layout_custom_flat_drop_down.view.*
import kotlinx.android.synthetic.main.layout_custom_flat_drop_down_recycler_view.view.*

class CustomFlatDropDown(context:Context, attributeSet: AttributeSet ?= null) : ConstraintLayout(context,attributeSet), CustomFlatDropDownRecyclerViewClickListener{

    var listener: CustomFlatDropDownClickListener ? = null
    private var isRequired: Boolean = false

    init {
        View.inflate(context, R.layout.layout_custom_flat_drop_down,this)
    }

    fun bind(title: String, options: List<String>, listener: CustomFlatDropDownClickListener, isRequired: Boolean = false){
        this.listener = listener
        this.title.text = title

        if (isRequired){
            this.isRequired = true
            requiredText.visibility = View.VISIBLE
        }

        root.setOnClickListener {
            onDropDownClick()
        }
        var layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = CustomFlatDropDownAdapter(this,options)
    }

    override fun onRecyclerViewClick(reason: String) {
        title.text = reason
        listener?.onClick(reason)
        hideRequiredText()
        onDropDownClick()
    }

    private fun hideRequiredText(){
        requiredText.visibility = View.GONE
    }

    private fun showRequiredText(){
        requiredText.visibility = View.VISIBLE
    }

    private fun onDropDownClick(){
        when(recyclerView.visibility){
            View.VISIBLE -> {
               closeDropDown()
            }
            else -> {
               openDropDown()
            }
        }
    }

    fun closeDropDown(){
        if(isRequired && requiredText.visibility == View.INVISIBLE){
            showRequiredText()
        }
        arrow.animate().rotation(180f).start()
        recyclerView.visibility = View.GONE
        setTextAppearance(DropDownSelection.NO_SELECTED.value)
    }

    fun openDropDown(){
        if(isRequired && requiredText.visibility != View.GONE){
            requiredText.visibility = View.INVISIBLE
        }
        arrow.animate().rotation(0f).start()
        setTextAppearance(DropDownSelection.SELECTED.value)
        recyclerView.visibility = View.VISIBLE
    }

    private fun setTextAppearance(isSelected: Int){
        when(isSelected){
            DropDownSelection.SELECTED.value -> {
                if (Build.VERSION.SDK_INT < 23) {
                    title.setTextAppearance(context,R.style.CustomFlatDropDownSelected)
                } else {
                    title.setTextAppearance(R.style.CustomFlatDropDownSelected)
                }
            }
            DropDownSelection.NO_SELECTED.value -> {
                if (Build.VERSION.SDK_INT < 23) {
                    title.setTextAppearance(context,R.style.CustomFlatDropDownNoSelected)
                } else {
                    title.setTextAppearance(R.style.CustomFlatDropDownNoSelected)
                }
            }
        }
    }
}

class CustomFlatDropDownAdapter(var listener: CustomFlatDropDownRecyclerViewClickListener, private var optionList: List<String>) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(DropDownView(parent.context))
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.itemView
        (view as? DropDownView)?.bind(optionList[position],listener, position == optionList.size-1)
    }

    class DropDownView(context:Context) : ConstraintLayout(context){

        var listener: CustomFlatDropDownRecyclerViewClickListener? = null

        init {
            val view = View.inflate(context, R.layout.layout_custom_flat_drop_down_recycler_view,this)
            view.layoutParams = LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        }

        fun bind(option: String, listener: CustomFlatDropDownRecyclerViewClickListener, isLastPosition: Boolean){
            if(isLastPosition) divider.visibility = View.GONE
            this.listener = listener
            dropDownOption.text = option
            rootRv.setOnClickListener {
                listener.onRecyclerViewClick(option)
            }
        }
    }
}

interface CustomFlatDropDownClickListener{
    fun onClick(reason: String)
}

interface CustomFlatDropDownRecyclerViewClickListener{
    fun onRecyclerViewClick(reason: String)
}

enum class DropDownFields(val type: String){
    TITLE("title"),
    OPTION("option"),
    UNKNOWN("unknown")
}

enum class DropDownSelection(val value: Int){
    SELECTED(0),
    NO_SELECTED(1)
}