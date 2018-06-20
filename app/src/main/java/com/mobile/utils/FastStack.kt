package com.mobile.utils

import kotlin.collections.ArrayList

class FastStack<T> : ArrayList<T>() {

    fun push(o: T) {
        removeAll {
            it==o
        }
        super.add(o)
    }

    override fun add(index: Int, element: T) {
        throw IllegalArgumentException("Operation Not Supported")
    }

    fun pop(): T {
        return removeAt(size - 1)
    }

    fun empty(): Boolean {
        return size == 0
    }

    fun peek(): T? {
        if (size > 0) {
            return get(size - 1)
        }
        return null
    }
}