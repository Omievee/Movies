package com.mobile.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.mobile.UserPreferences
import com.mobile.rx.Schedulers
import com.moviepass.BuildConfig
import com.moviepass.R
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import kotlinx.android.synthetic.main.fragment_web_view.*

class WebViewFragment : Fragment() {

    private var url: String? = null
        get() {
            return if (field != null) {
                field
            } else {
                BuildConfig.REGISTRATION_URL
            }
        }

    var webViewListener: WebViewListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString("url")
        var fragment: Fragment? = parentFragment
        while (fragment != null) {
            if (fragment is WebViewListener) {
                webViewListener = parentFragment as WebViewListener
                break
            }
            fragment = fragment.parentFragment
        }
        if (webViewListener == null) {
            webViewListener = activity as? WebViewListener
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cookeiManager = CookieManager.getInstance()
        val tokens = arrayOf(Pair("at", UserPreferences.authToken), Pair(("uid"), UserPreferences.userId))
        tokens.forEach {
            cookeiManager.setCookie(url, "${it.first}=${it.second}")
        }
        web?.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    return when (request?.url?.toString() == BuildConfig.REGISTRATION_URL + "/reactivationComplete") {
                        true -> {
                            callOnDone()
                            null
                        }
                        false -> super.shouldInterceptRequest(view, request)
                    }
                }
            }
            settings?.javaScriptEnabled = true
        }
        var urlExtra: String = "/?"
        var firstTime: Boolean = true
        tokens.forEach {
            if (firstTime) {
                firstTime = false
            } else {
                urlExtra = "$urlExtra&"
            }
            urlExtra += "${it.first}=${it.second}"
        }
        Log.d("WEB_VIEW", url + urlExtra)
        web?.loadUrl(url + urlExtra)
    }

    private fun callOnDone() {
        Single.create(
                SingleOnSubscribe<Any> {
                    it.onSuccess(Any())
                }
        ).compose(Schedulers.singleDefault())
                .subscribe({ _ ->
                    webViewListener?.onDoneWithWebview()
                }, { error -> })
    }

    fun canGoBack(): Boolean {
        return web?.canGoBack() == true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web_view, container, false)
    }

    fun goBack() {
        when (web?.canGoBack()) {
            true -> web?.goBack()
        }
    }

    companion object {

        fun newInstance(url: String): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString("url", url)
            fragment.arguments = args
            return fragment
        }
    }
}

interface WebViewListener {
    fun onDoneWithWebview()
}