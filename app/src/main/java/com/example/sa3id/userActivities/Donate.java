package com.example.sa3id.userActivities;

import android.os.Bundle;
import android.webkit.WebView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;

public class Donate extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_donate);
        WebView myWebView = (WebView) findViewById(R.id.webViewDonate);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl("https://www.paypal.com/donate/?hosted_button_id=BVAZMC5Z444AC");

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_donate;
    }
}