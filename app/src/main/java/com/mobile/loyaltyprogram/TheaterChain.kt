package com.mobile.loyaltyprogram

import com.google.gson.annotations.SerializedName
import com.mobile.utils.text.*

open class TheaterChain(@SerializedName("chain_name") var chainNameKey: String? = null,
                        @SerializedName("is_user_registered") var isUserRegistered: Boolean = false,
                        private @SerializedName("required_fields") var _requiredFields: Map<String, RequiredField>? = emptyMap()
) {

    val chainName: String? by lazy {
        this.chainNameKey.toSentenceCase()
    }

    val requiredFields: Map<String,RequiredField>? by lazy {
        this._requiredFields?.mapKeys {
            it.key.replace("value","").trim()
        }
    }

    override fun toString(): String {
        return "TheaterChain(chainName=$chainName, isUserRegistered=$isUserRegistered)"
    }
}

enum class RequiredField {
    @SerializedName("INT", alternate = ["Int", "Integer", "int", "integer", "Number", "NUMBER"])
    FI_INT,
    @SerializedName("STRING", alternate = ["String", "Str", "str"])
    FI_STRING
}
