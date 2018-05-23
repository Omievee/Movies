package com.mobile.adapters

interface ItemSame<T> {

    fun sameAs(same: T): Boolean

    fun contentsSameAs(same: T): Boolean
}