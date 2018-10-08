package com.mobile.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.mobile.MPActivty
import com.moviepass.R
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity : MPActivty() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        webview.webViewClient = WebViewClient()
        webview.settings.javaScriptEnabled = true
        webview.loadUrl(intent?.getStringExtra("url"))
    }

    companion object {
        fun newIntent(ctx: Context, url:String): Intent {
            return Intent(ctx,WebViewActivity::class.java).putExtra("url", url)
        }
    }

}