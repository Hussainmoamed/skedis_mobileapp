package com.gravitykey.in.skedis;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Notification_redirect extends AppCompatActivity {
    WebView wv; ProgressDialog progressDialog;String url,date,from,to;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_redirect);
        wv = findViewById(R.id.web);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);


        Bundle extras=getIntent().getExtras();
        String red=extras.getString("Body");
         date=extras.getString("date");
         from=extras.getString("from");
         to=extras.getString("to");




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

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                wv.loadUrl("javascript:add_cart_product('"+date+"','"+from+"','"+to+"')");
                            }
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
        }else{
            progressDialog.show();
            String site="https://gravitykey.com/notify.php";
            Log.i("URL",site);
            StringRequest stringRequest=new StringRequest(Request.Method.POST, site, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    try{
                        JSONObject object=new JSONObject(response);
                        JSONArray jsonArray=object.getJSONArray("notification");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject  object1=jsonArray.getJSONObject(i);
                            String body=object1.getString("body")+"  "+object1.getString("title");
                            Log.i("Post Value",body);
                           // Toast.makeText(Notification_redirect.this, "Posted Data="+body, Toast.LENGTH_SHORT).show();
                        }

                    }catch (JSONException e) {
                       // Toast.makeText(Notification_redirect.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                   // Toast.makeText(Notification_redirect.this, error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            });
            RequestQueue requestQueue= Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        }

    }
}