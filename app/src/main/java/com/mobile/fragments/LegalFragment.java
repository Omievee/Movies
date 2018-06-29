package com.mobile.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.moviepass.R;

/**
 * Created by o_vicarra on 1/25/18.
 */

public class LegalFragment extends Fragment {


    private WebView webView;
    RelativeLayout privacyPolicy, ToS;


    public LegalFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fr_legal, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = view.findViewById(R.id.Web);
        webView.getSettings().setJavaScriptEnabled(true);


        privacyPolicy = view.findViewById(R.id.PP);
        ToS = view.findViewById(R.id.TOS);

        final String ppURL = "https://www.moviepass.com/privacy/";
        final String tosURL = "https://www.moviepass.com/terms";
        ToS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                webView.setVisibility(View.VISIBLE);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        view.loadUrl(tosURL);
                        return true;
                    }
                });

                webView.loadUrl(tosURL);
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.VISIBLE);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                        view.loadUrl(ppURL);
                        return true;
                    }
                });
                webView.loadUrl(ppURL);
            }
        });


    }



}
