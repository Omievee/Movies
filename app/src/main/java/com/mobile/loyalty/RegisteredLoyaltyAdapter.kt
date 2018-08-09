package com.mobile.loyalty

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.calculateDiff
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.mobile.adapters.BaseViewHolder
import com.mobile.adapters.BasicDiffCallback
import com.mobile.widgets.MaterialSpinnerSpinnerView

class RegisteredLoyaltyAdapter(val theaterChainClickListener: TheaterChainClickListener) : RecyclerView.Adapter<BaseViewHolder>() {

    var data: Data? = null
        set(value) {
            field = value
            value?.diffResult?.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(TheaterChainView(parent.context, theaterChainClickListener = theaterChainClickListener))
    }

    override fun getItemCount(): Int {
        return data?.list?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val element = data?.list?.get(position)
        element?.let { theaterChain ->
            (holder.itemView as? TheaterChainView)?.bind(theaterChain)
        }
    }

    companion object {
        fun create(oldData: Data?, data: List<TheaterChain>): Data {
            val oldList: List<TheaterChain> = oldData?.list ?: emptyList()
            return Data(
                    data,
                    calculateDiff(BasicDiffCallback<TheaterChain>(oldList, data))
            )
        }
    }
}

class TheaterChainView(context: Context, val theaterChainClickListener: TheaterChainClickListener) : MaterialSpinnerSpinnerView(context) {
    var theater: TheaterChain? = null

    init {
        this.setOnClickListener {
            val theater = this.theater?: return@setOnClickListener
            theaterChainClickListener.onLoyaltyProgramClicked(theater)
        }
    }
    fun bind(theaterChain: TheaterChain) {
        theater = theaterChain
        theater?.let {
            it.chainName?.let {
                bind(it)
            }
        }
    }

    fun unbind() {
        bind("")
    }
}

class Data(val list: List<TheaterChain>? = null,
           val diffResult: DiffUtil.DiffResult)