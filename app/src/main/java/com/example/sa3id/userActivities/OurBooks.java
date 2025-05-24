package com.example.sa3id.userActivities;

import android.os.Bundle;
import android.webkit.WebView;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;

public class OurBooks extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_our_books);
        WebView myWebView = (WebView) findViewById(R.id.webViewBooks);
        myWebView.loadUrl("https://sites.google.com/view/sa3id/%D8%A7%D9%84%D9%85%D8%AA%D9%88%D9%83%D9%84");

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_our_books;
    }
}