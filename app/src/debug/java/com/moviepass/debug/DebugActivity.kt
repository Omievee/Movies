package com.moviepass.debug

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobile.MPActivty
import com.mobile.seats.SeatPreviewListener
import com.moviepass.R
import dagger.android.AndroidInjection
import dagger.android.support.HasSupportFragmentInjector
import java.io.InputStreamReader

class DebugActivity : MPActivty(), SeatPreviewListener {

    override fun onClose() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_debug)

        val gson = Gson()
        val json:List<DMA> = gson.fromJson(InputStreamReader(resources.openRawResource(R.raw.csvjson)),object : TypeToken<List<DMA>>() {

        }.type)
        val dirty = json.associateByTo(mutableMapOf<String,Boolean>(), {it.zipcode} , {true})
        println(gson.toJson(dirty))
    }
}
