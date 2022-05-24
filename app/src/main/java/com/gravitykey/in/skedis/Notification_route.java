package com.gravitykey.in.skedis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Notification_route extends AppCompatActivity {
    WebView wv; ProgressDialog progressDialog;String red;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_route);
        wv = findViewById(R.id.web1);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

     //   try{
            Bundle extras=getIntent().getExtras();
             red=extras.getString("weburl");
//        }catch (Exception e){
//            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//        }






        // Notification url webview
        if(red != null){
            if(red.startsWith("https:") || red.startsWith("http:")){

                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        //view.loadUrl(url);
                        if (url.startsWith("whatsapp:") || url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("mailto:")) {
                            Intent in = new Intent(Intent.ACTION_VIEW);
                            in.setData(Uri.parse(url));
                            startActivity(in);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        progressDialog.show();
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if(progressDialog.isShowing()){

//                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                                wv.loadUrl("javascript:add_cart_product('"+date+"','"+from+"','"+to+"')");
//                            }
                            progressDialog.dismiss();

                        }
                    }
                });
                wv.getSettings().setJavaScriptEnabled(true);
                wv.getSettings().setUseWideViewPort(true);
                wv.getSettings().setLoadWithOverviewMode(true);
                wv.getSettings().setDomStorageEnabled(true);
                wv.getSettings().setSupportZoom(true);
                // wv.setWebContentsDebuggingEnabled(true);
                wv.loadUrl(red);
            }
        }
    }
}