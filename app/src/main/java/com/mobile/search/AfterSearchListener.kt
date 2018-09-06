package com.mobile.search

import com.mobile.model.Movie
import com.mobile.responses.AllMoviesResponse

interface AfterSearchListener {

    fun getSearchString(movie: Movie)
}