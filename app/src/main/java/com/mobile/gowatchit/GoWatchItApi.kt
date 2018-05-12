package com.mobile.gowatchit

import com.mobile.responses.GoWatchItResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoWatchItApi {

    @GET("/prod/ingest")
    fun openAppEvent(@Query("ct") ct: String, @Query("ci") ci: String, @Query("cd") cd: String,
                     @Query("e") e: String, @Query("c") campaign: String, @Query("m") m: String, @Query("mc") mc: String,
                     @Query("u") u: String, @Query("o") o: String, @Query("l") l: String,
                     @Query("ln") ln: String, @Query("eid[movie_pass]") movie_pass: String, @Query("eid[aaid]") idfa: String,
                     @Query("ab") ab: String, @Query("av") av: String, @Query("lts") lts: String): Call<GoWatchItResponse>

    @GET("/prod/ingest")
    fun clickOnShowtime(@Query("e") engagement: String, @Query("et") et: String, @Query("tht") tht: String,
                        @Query("thd") thd: String, @Query("tn") th: String, @Query("thc") thc: String,
                        @Query("thr") thr: String, @Query("thz") thz: String, @Query("tha") tha: String,
                        @Query("ct") ct: String, @Query("ci") ci: String, @Query("cd") cd: String,
                        @Query("c") campaign: String, @Query("m") m: String, @Query("mc") mc: String,
                        @Query("u") u: String, @Query("o") o: String, @Query("l") l: String,
                        @Query("ln") ln: String, @Query("eid[movie_pass]") movie_pass: String, @Query("eid[aaid]") idfa: String,
                        @Query("ab") ab: String, @Query("av") av: String, @Query("lts") lts: String): Call<GoWatchItResponse>

    @GET("/prod/ingest")
    fun ticketPurchase(@Query("e") engagement: String, @Query("tht") tht: String,
                       @Query("thd") thd: String, @Query("tn") th: String, @Query("thc") thc: String,
                       @Query("thr") thr: String, @Query("thz") thz: String, @Query("tha") tha: String,
                       @Query("ct") ct: String, @Query("ci") ci: String, @Query("cd") cd: String,
                       @Query("c") campaign: String, @Query("m") m: String, @Query("mc") mc: String,
                       @Query("u") u: String, @Query("o") o: String, @Query("l") l: String,
                       @Query("ln") ln: String, @Query("eid[movie_pass]") movie_pass: String, @Query("eid[aaid]") idfa: String,
                       @Query("ab") ab: String, @Query("av") av: String, @Query("lts") lts: String): Call<GoWatchItResponse>

    @GET("/prod/ingest")
    fun searchTheatersMovies(@Query("e") engagement: String,
                             @Query("ct") ct: String, @Query("ci") ci: String,
                             @Query("tr") tr: String,
                             @Query("c") campaign: String, @Query("m") m: String, @Query("mc") mc: String,
                             @Query("u") u: String, @Query("o") o: String, @Query("l") l: String,
                             @Query("ln") ln: String, @Query("eid[movie_pass]") movie_pass: String, @Query("eid[aaid]") idfa: String,
                             @Query("ab") ab: String, @Query("av") av: String, @Query("lts") lts: String): Call<GoWatchItResponse>

    @GET("/prod/ingest")
    fun openTheaterEvent(@Query("e") engagement: String,
                         @Query("tn") th: String, @Query("thc") thc: String,
                         @Query("thr") thr: String, @Query("thz") thz: String, @Query("tha") tha: String,
                         @Query("ct") ct: String, @Query("ci") ci: String,
                         @Query("c") campaign: String, @Query("m") m: String, @Query("mc") mc: String,
                         @Query("u") u: String, @Query("o") o: String, @Query("l") l: String,
                         @Query("ln") ln: String, @Query("eid[movie_pass]") movie_pass: String, @Query("eid[aaid]") idfa: String,
                         @Query("ab") ab: String, @Query("av") av: String, @Query("lts") lts: String): Call<GoWatchItResponse>

    @GET("/prod/ingest")
    fun openMapEvent(@Query("e") engagement: String,
                     @Query("ct") ct: String, @Query("ci") ci: String,
                     @Query("et") et: String,
                     @Query("c") campaign: String, @Query("m") m: String, @Query("mc") mc: String,
                     @Query("u") u: String, @Query("o") o: String, @Query("l") l: String,
                     @Query("ln") ln: String, @Query("eid[movie_pass]") movie_pass: String, @Query("eid[aaid]") idfa: String,
                     @Query("ab") ab: String, @Query("av") av: String, @Query("lts") lts: String): Call<GoWatchItResponse>


}