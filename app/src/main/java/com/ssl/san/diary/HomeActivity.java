package com.ssl.san.diary;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HomeActivity extends AppCompatActivity {
    WebView web;
    boolean errOccurs = false;
    String baseUrl = "http://smartopd.in/diary/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        web = (WebView) findViewById(R.id.web);
        web.getSettings().setJavaScriptEnabled(true);
        web.addJavascriptInterface(new JavaBridge(this), "JavaBridge");

        Log.d("INTENT URL","#"+getIntent().getStringExtra("url")+"#");
        if(getIntent().getStringExtra("url")!=null){
            loadApp(baseUrl + getIntent().getStringExtra("url"));
        } else {
            if(BuildConfig.CITY_ID > 0){
                String devId = Settings.Secure.getString(HomeActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
                loadApp(baseUrl + "index.php?cityId="+BuildConfig.CITY_ID+"&devId="+devId);
            } else {
                loadApp(baseUrl);
            }
        }
        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                findViewById(R.id.progress1).setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                findViewById(R.id.progress1).setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                web.loadData(getErrorPage(),"text/html","charset=UTF-8");
                super.onReceivedError(view, request, error);
            }
        });
    }

    public void loadApp(String url){
        web.loadUrl(url);
    }
    @Override
    public void onBackPressed() {
        if (web.canGoBack() && !errOccurs) {
            web.goBack();
        } else {
            if(getIntent().getStringExtra("url")!=null){
                super.onBackPressed();
            } else {
                goBack();
            }
        }
    }

    public void goBack(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("बाहेर पडा?");
        alert.setPositiveButton("हो", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HomeActivity.super.onBackPressed();
            }
        });
        alert.setNegativeButton("नाही", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public String getErrorPage(){
        String errStr = "<style>body, html {padding: 0px; margin: 0px;}</style>" +
                "<div style='background-color: #2196f3; color: white;padding: 10px;font-size: 25px;'><center>Diary<center></div>" +
                "<div style='padding-top: 100px;font-family: arial'>" +
                "<center>Check Your Internet Connection<br/><br/>" +
                "<button style='background-color: #f44336; border-radius: 3px; " +
                "padding: 10px 40px 10px 40px;color: white;border: 1px solid #d32f2f;'" +
                " onclick='javascript:window.JavaBridge.ReloadApp()'>Retry</button>" +
                "</center></div>";
        errOccurs = true;
        return errStr;
    }

    public class JavaBridge {
        Activity parentActivity;
        public JavaBridge(Activity activity) {
            parentActivity = activity;
        }
        @JavascriptInterface
        public void ReloadApp(){
            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(0,0);
        }
        @JavascriptInterface
        public void newPage(String url){
            Log.e("URL",url);
            Intent newPage = new Intent(getApplicationContext(),HomeActivity.class);
            newPage.putExtra("url",url);
            startActivity(newPage);
        }

        @JavascriptInterface
        public void backPressed(){
            HomeActivity.this.finish();
        }
    }
}
