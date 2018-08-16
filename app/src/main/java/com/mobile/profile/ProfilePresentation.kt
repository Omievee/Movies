package com.mobile.profile

import com.mobile.adapters.ItemSame

data class ProfilePresentation(
        val type: Profile,
        val header: String? = null,
        val title: String? = null,
        val subHeader: String? = null,
        var toggled: Boolean = false,
        val link:String? = null,
        val data:Any? = null
) : ItemSame<ProfilePresentation> {
    override fun sameAs(same: ProfilePresentation): Boolean {
        return equals(same)
    }

    override fun contentsSameAs(same: ProfilePresentation): Boolean {
        return hashCode() == same.hashCode()
    }

}