package com.gravitykey.in.skedis;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    WebView wv;
    String city = "", cty = "";
    ProgressDialog progressDialog;
    Double latitude, longitude;
    FirebaseDatabase fdb = FirebaseDatabase.getInstance();
    String tok;
    DatabaseReference ref = fdb.getReference();
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            wv = findViewById(R.id.web);
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            SharedPreferences sh = getSharedPreferences("Token", MODE_PRIVATE);
            SharedPreferences.Editor editor = sh.edit();
             tok = sh.getString("token", "");
            String cy = sh.getString("city", "");

           //String red="https://skedis.in/index.php?route=product/product&product_id=550";

            // get Firebase Token if not Exist
            if (tok.equals("")) {
                getToken();
            }




//            Bundle bundle = getIntent().getExtras();
//            if (bundle != null) {
//                String dataText = "";
//                for (String key : bundle.keySet()) {
//                    dataText += key + " = " + bundle.get(key) + "\n";
//                }
//                dataText = "Data Received from Notification\n\n" + dataText.trim();
//                //Toast.makeText(this, dataText, Toast.LENGTH_SHORT).show();
//            }


        // Empty city webview
            if (cy.isEmpty()  ) {
                try {
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

                        @SuppressLint("JavascriptInterface")
                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            super.onPageStarted(view, url, favicon);
                            String d="data from native";
                          //  wv.addJavascriptInterface(new Update_token(this), "Android");
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                wv.loadUrl("javascript:update_token('"+tok+"')");
                            }
                            progressDialog.show();
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                    wv.loadUrl("javascript:update_token('"+tok+"')");
                                }
                            }
                        }

                    });
                    wv.setWebChromeClient(new WebChromeClient() {

                        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                            if (uploadMessage != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
                                    uploadMessage.onReceiveValue(null);
                                }
                                uploadMessage = null;
                            }

                            uploadMessage = filePathCallback;

                            Intent intent = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                intent = fileChooserParams.createIntent();
                            }
                            try {
                                startActivityForResult(intent, REQUEST_SELECT_FILE);
                            } catch (ActivityNotFoundException e) {
                                uploadMessage = null;
                                return false;
                            }
                            return true;
                        }


                    });
                    wv.getSettings().setJavaScriptEnabled(true);
                    wv.getSettings().setUseWideViewPort(true);
                    wv.getSettings().setLoadWithOverviewMode(true);
                    wv.getSettings().setDomStorageEnabled(true);
                    wv.getSettings().setSupportZoom(true);
                    wv.getSettings().setUserAgentString(USER_AGENT);
                    wv.setWebContentsDebuggingEnabled(true);
                    
                    wv.loadUrl("https://skedis.in/index.php");
                    progressDialog.dismiss();
                } catch (Exception e) {
                }
            }

            // If city is not Empty Webview

            if (cy.length() > 0 ) {

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
                            progressDialog.dismiss();
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            wv.loadUrl("javascript:update_token('"+tok+"')");
                        }
                    }



                });
                wv.setWebChromeClient(new WebChromeClient() {

                    public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                        if (uploadMessage != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
                                uploadMessage.onReceiveValue(null);
                            }
                            uploadMessage = null;
                        }

                        uploadMessage = filePathCallback;

                        Intent intent = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intent = fileChooserParams.createIntent();
                        }
                        try {
                            startActivityForResult(intent, REQUEST_SELECT_FILE);
                        } catch (ActivityNotFoundException e) {
                            uploadMessage = null;
                            return false;
                        }
                        return true;
                    }


                });
                wv.getSettings().setJavaScriptEnabled(true);
                wv.getSettings().setUseWideViewPort(true);
                wv.getSettings().setLoadWithOverviewMode(true);
                wv.getSettings().setDomStorageEnabled(true);
                wv.getSettings().setSupportZoom(true);
                wv.getSettings().setUserAgentString(USER_AGENT);
                wv.loadUrl("https://skedis.in/index.php?city=" + cy);
                progressDialog.dismiss();
            } else {
                getLastLocation();
            }
        } catch (Exception ex) {
            //  Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }


    }
// Get User Location Function
    private void getLastLocation() {
        try {
if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
return;
   }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            try {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {

                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    Geocoder g = new Geocoder(MainActivity.this, Locale.getDefault());
                                    List<Address> addresses = null;
                                    try {
                                        addresses = g.getFromLocation(latitude, longitude, 1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        city = addresses.get(0).getLocality();
                                        String cty = Base64.encodeToString(city.getBytes(), Base64.NO_WRAP);
                                        if (city.length() > 0) {

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
                                                        progressDialog.dismiss();
                                                    }
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                                        wv.loadUrl("javascript:update_token('"+tok+"')");
                                                    }
                                                }


                                            });
                                            wv.setWebChromeClient(new WebChromeClient() {

                                                public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                                                    if (uploadMessage != null) {
                                                        uploadMessage.onReceiveValue(null);
                                                        uploadMessage = null;
                                                    }

                                                    uploadMessage = filePathCallback;

                                                    Intent intent = null;
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                                        intent = fileChooserParams.createIntent();
                                                    }
                                                    try {
                                                        startActivityForResult(intent, REQUEST_SELECT_FILE);
                                                    } catch (ActivityNotFoundException e) {
                                                        uploadMessage = null;
                                                        return false;
                                                    }
                                                    return true;
                                                }


                                            });
                                            wv.getSettings().setJavaScriptEnabled(true);
                                            wv.getSettings().setUseWideViewPort(true);
                                            wv.getSettings().setLoadWithOverviewMode(true);
                                            wv.getSettings().setDomStorageEnabled(true);
                                            wv.getSettings().setSupportZoom(true);
                                            wv.getSettings().setUserAgentString(USER_AGENT);
                                            wv.loadUrl("https://skedis.in/index.php?city=" + cty);
                                            SharedPreferences s = getSharedPreferences("Token", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor e = s.edit();
                                            e.putString("city", cty);
                                            e.apply();
                                            progressDialog.dismiss();
                                        }
                                    } catch (Exception ex) {
                                        // Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception ex) {
                                // Toast.makeText(getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
        }catch (Exception ex){
           // Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

    }
    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(0);
            mLocationRequest.setFastestInterval(0);
            mLocationRequest.setNumUpdates(1);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest, mLocationCallback,
                    Looper.myLooper()
            );
        }catch(Exception ex){
           // Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            try {
                Location mLastLocation = locationResult.getLastLocation();
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                Geocoder g = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = g.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    city = addresses.get(0).getLocality();
                    String cty = Base64.encodeToString(city.getBytes(), Base64.NO_WRAP);
                    if (city.length() > 0) {

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
                                    progressDialog.dismiss();
                                }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                    wv.loadUrl("javascript:update_token('"+tok+"')");
                                }
                            }


                        });
                        wv.setWebChromeClient(new WebChromeClient(){

                            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
                            {
                                if (uploadMessage != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1) {
                                        uploadMessage.onReceiveValue(null);
                                    }
                                    uploadMessage = null;
                                }

                                uploadMessage = filePathCallback;

                                Intent intent = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    intent = fileChooserParams.createIntent();
                                }
                                try
                                {
                                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                                } catch (ActivityNotFoundException e)
                                {
                                    uploadMessage = null;
                                    return false;
                                }
                                return true;
                            }



                        });
                        wv.getSettings().setJavaScriptEnabled(true);
                        wv.getSettings().setUseWideViewPort(true);
                        wv.getSettings().setLoadWithOverviewMode(true);
                        wv.getSettings().setDomStorageEnabled(true);
                        wv.getSettings().setSupportZoom(true);
                        wv.getSettings().setUserAgentString(USER_AGENT);
                        wv.loadUrl("https://skedis.in/index.php?city=" + cty);
                        SharedPreferences s = getSharedPreferences("Token", Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = s.edit();
                        e.putString("city",cty);
                        e.apply();


                        progressDialog.dismiss();
                    }
                } catch (Exception ex) {

                }
            }catch(Exception ex){
               // Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    };

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode == REQUEST_SELECT_FILE)
            {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        }
        else if (requestCode == FILECHOOSER_RESULTCODE)
        {
            if (null == mUploadMessage)
                return;

            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }


    }



    public  void getToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("msg_token_fmt", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        tok=token;
                        // ref.child("Token").push().setValue(token);
                        SharedPreferences s = getSharedPreferences("Token", Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = s.edit();
                        e.putString("token",token);
                        e.apply();
                        Storedb strdb=new Storedb();
                        strdb.execute(token);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if(wv!=null && wv.canGoBack()){
            wv.goBack();
        }else{
            super.onBackPressed();
        }

    }
    private class Storedb extends AsyncTask {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            String reg_url="https://gravitykey.com/token.php";
            String token= (String) objects[0];
            try{
                URL url = new URL(reg_url);
                HttpURLConnection httpURLConnection =(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream OS = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(OS, "UTF-8"));
                String data= URLEncoder.encode("Token","UTF-8")+"="+URLEncoder.encode(token,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                OS.close();
                InputStream IS = httpURLConnection.getInputStream();
                IS.close();
                return "Registration Success!!";
            }catch (Exception e){
                //Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
             //Toast.makeText(MainActivity.this, city, Toast.LENGTH_LONG).show();
        }
    }
}
