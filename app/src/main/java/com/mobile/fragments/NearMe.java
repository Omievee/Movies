package com.mobile.fragments;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.moviepass.R;

public class NearMe extends BottomSheetDialogFragment {

    View root;
    WebView webView;
    String searchTheaterURL;
    String javaScript;

    public NearMe() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fr_nearme, container);
        webView = root.findViewById(R.id.Web);
        webView.getSettings().setJavaScriptEnabled(true);

        searchTheaterURL = "https://www.moviepass.com/?myZip=";

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                view.scrollTo(0, 4100);
            }
        });
        webView.loadUrl(searchTheaterURL);


        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
