package com.mobile.search

import com.mobile.model.Movie

interface AfterSearchListener {

    fun getSearchString(movie: Movie)
}