package com.mobile.requests

class SurgeCheckRequest(val data: SurgeCheck)

class SurgeCheck(val tribuneTheaterId: Int,
                 val normalizedMovieId: Int,
                 val showtimeStart: String,
                 val dateTime:String
)
