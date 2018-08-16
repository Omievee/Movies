package com.mobile.responses

import com.mobile.model.Movie

class CurrentMoviesResponse(var featured: List<Movie> = emptyList(),
                            var categorizedMovies: List<Pair<String, List<Movie>>> = emptyList()
)