package com.mobile.loyalty

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mobile.adapters.ItemSame
import com.mobile.utils.text.toSentenceCase
import kotlinx.android.parcel.Parcelize

@Parcelize
open class TheaterChain(@SerializedName("chain_name") var chainNameKey: String? = null,
                        @SerializedName("is_user_registered") var isUserRegistered: Boolean = false,
                        @SerializedName("required_fields") var requiredFields: RequiredField? = null


) : ItemSame<TheaterChain>, Parcelable {


    val chainName: String? by lazy {
        this.chainNameKey.toSentenceCase()
    }


    override fun sameAs(same: TheaterChain): Boolean {
        return same.chainNameKey == same.chainNameKey
    }

    override fun contentsSameAs(same: TheaterChain): Boolean {
        return hashCode() == same.hashCode()
    }

    override fun toString(): String {
        return chainName ?: ""
    }

    fun getRequiredFields(): String? {
        requiredFields?.cardNumber.let {
            return it
        }
    }
}


@Parcelize
data class RequiredField(
        @SerializedName("cardNumber")
        val cardNumber: String? = null
) : Parcelable
