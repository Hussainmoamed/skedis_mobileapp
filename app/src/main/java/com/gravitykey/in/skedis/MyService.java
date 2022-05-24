package com.gravitykey.in.skedis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.renderscript.RenderScript;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class MyService extends FirebaseMessagingService  {
    public static final String TAG = "MsgFirebaseServ";

    public void onMessageReceived(RemoteMessage remoteMessage) {
      //  super.onMessageReceived(remoteMessage);
            //message data payload
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "Message data payload: " + remoteMessage.getData());
                try{
                    String title=remoteMessage.getData().get("title");
                    String body=remoteMessage.getData().get("body");
                    String image=remoteMessage.getData().get("image");
                    if(image.equals("no_image") ){
                        String[] values=body.split("\\|");
                        String msg=values[0];
                        String url=values[1];
                        send_notify(title,msg,url);
                    }else{
                        String[] values=body.split("\\|");
                        String msg=values[0];
                        String url=values[1];
                        String date=values[2];
                        String from=values[3];
                        String to=values[4];
                        sendNotification(title,msg,image,url,date,from,to);
                    }
                }catch (Exception e){
                }
            }
    }

    public  void send_notify(String title, String msg,String url) {
        try{
            Intent intent = new Intent(this, Notification_route.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
           // intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Bundle bundle =new Bundle();
            bundle.putString("weburl",url);
            intent.putExtras(bundle);
          //  bundle.putCharSequence("weburl",url);

            final int id=(int)(Math.random() * 100);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,id, intent,PendingIntent.FLAG_UPDATE_CURRENT);
            String cid="Default";

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,cid)
                    .setSmallIcon(R.drawable.skedis_logo)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(PRIORITY_MIN)
                    .setOngoing(false).
                            setDefaults(Notification.DEFAULT_ALL);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel=new NotificationChannel(cid,"Default channel",NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            // startForeground(id,notificationBuilder.build());
            notificationManager.notify(0, notificationBuilder.build());

        }catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotification(String title, String msg,String image,String url,String date,String from,String to) {
        try{
            Intent intent = new Intent(this, Notification_redirect.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Bundle bundle =new Bundle();
            bundle.putString("Body",url);
            bundle.putString("date",date);
            bundle.putString("from",from);
            bundle.putString("to",to);
            intent.putExtras(bundle);
            final int id=(int)(Math.random() * 100);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,id, intent,PendingIntent.FLAG_UPDATE_CURRENT);
            String cid="Default";
            Bitmap bitmap = getBitmapfromUrl(image);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,cid)
                    .setSmallIcon(R.drawable.skedis_logo)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(PRIORITY_MIN)
                    .setOngoing(false).
                     setDefaults(Notification.DEFAULT_ALL).
                     setStyle(
                          new NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null)
                       ).setLargeIcon(bitmap);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel=new NotificationChannel(cid,"Default channel",NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
           // startForeground(id,notificationBuilder.build());
            notificationManager.notify(0, notificationBuilder.build());

        }catch (Exception e){
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            Log.e("awesome", "Error in getting notification image: " + e.getLocalizedMessage());
            return null;
        }
    }


}
