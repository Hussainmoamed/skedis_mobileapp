package com.gravitykey.in.skedis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

public class Splash extends AppCompatActivity {
    ImageView iv;Boolean gps;String cy;
    NetworkInfo active;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
        setContentView(R.layout.activity_splash);
//        progressDialog=new ProgressDialog(this);
//        progressDialog.setMessage("Loading...");
        ConnectivityManager cmgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        iv = (ImageView) findViewById(R.id.imageView);
        active = cmgr.getActiveNetworkInfo();
        LocationManager loc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gps = loc.isProviderEnabled(LocationManager.GPS_PROVIDER);
        SharedPreferences sh = getSharedPreferences("Token", MODE_PRIVATE);
        SharedPreferences.Editor editor = sh.edit();
        cy = sh.getString("city", "");

            SharedPreferences sp = getSharedPreferences("Noc", MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            String nocity = sp.getString("nocty", "");

              if (active != null && active.isConnected()) {

                  //progressDialog.show();
            //if Check Permission
               try{
                   if(nocity.length() > 0){
                     //  progressDialog.show();
                       new Handler().postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               Intent in = new Intent(Splash.this, MainActivity.class);
                               startActivity(in);
                           }
                       },3000);

                   }
                   else if (ActivityCompat.checkSelfPermission(Splash.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                           && ActivityCompat.checkSelfPermission(Splash.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                       // If request To User for Get Permission
                      // progressDialog.dismiss();
                       ActivityCompat.requestPermissions(Splash.this, new String[]{
                               Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                       }, 10);
                   }else{
                       // progressDialog.show();
                       getgps();
                   }
               }catch(Exception e){

               }
        } else {
            AlertDialog.Builder alt = new AlertDialog.Builder(Splash.this);
            alt.setTitle("Connect to a Network");
            alt.setCancelable(false);
            alt.setMessage("To use Skedis,Turn on Mobile data or Wifi");
            alt.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    System.exit(0);
                }
            });
            alt.show();
        }
    }catch (Exception ex){
       // Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
    }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        for(String permission: permissions){
            try{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){

                    try {
                        SharedPreferences s = getSharedPreferences("Noc", Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = s.edit();
                        e.putString("nocty","nocity");
                        e.apply();
                        Intent in = new Intent(Splash.this, MainActivity.class);
                        startActivity(in);
                        return;
                    }catch (Exception ex){
                        //Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
                        getgps();

                    } else{
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                        alertDialogBuilder.setTitle("Permission needed");
                        alertDialogBuilder.setMessage("This permission needed for accessing Your Location");
                        alertDialogBuilder.setPositiveButton("Open Setting", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", Splash.this.getPackageName(),
                                        null);
                                intent.setData(uri);
                                Splash.this.startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Log.d(TAG, "onClick: Cancelling");
                            }
                        });

                        AlertDialog dialog = alertDialogBuilder.create();
                        dialog.show();
                    }
                }
            }catch (Exception e){
               // Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }

        }
    }
    public  void getgps(){
        if(cy.length() > 0){

            try{
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent in = new Intent(Splash.this, MainActivity.class);
                        in.putExtra("url","");
                        startActivity(in);
                        finish();
                    }
                },1000);

            }catch(Exception e){

            }


        }else if(gps==true){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent in = new Intent(Splash.this, MainActivity.class);
                    in.putExtra("url","");
                    startActivity(in);
                    finish();
                }
            }, 3000);
        }else if(gps==false){
            AlertDialog.Builder alt = new AlertDialog.Builder(Splash.this);
            alt.setTitle("Welcome to Skedis!");
            alt.setMessage("Skedis needs access to your location to be able to list the services available in your locality");
            alt.setCancelable(false);
            alt.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent gp = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gp);
                    dialog.dismiss();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent in = new Intent(Splash.this, MainActivity.class);
                            startActivity(in);
                            finish();
                        }
                    }, 4000);

                }
            }).setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent in = new Intent(Splash.this, MainActivity.class);
                    in.putExtra("url","");
                    startActivity(in);
                    finish();

                }
            });
            alt.show();
        }

    }
}
