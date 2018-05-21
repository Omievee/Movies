package com.moviepass.debug

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.BarcodeFormat
import com.mobile.barcode.BarcodeLayout
import com.moviepass.R

class DebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        val barcodeLayout = findViewById<BarcodeLayout>(R.id.barcode_bl)
        barcodeLayout.bind("0F34", BarcodeFormat.PDF_417)
    }
}
