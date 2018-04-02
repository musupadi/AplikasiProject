/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tampir.jlastpower.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tampir.jlastpower.App;
import com.tampir.jlastpower.BuildConfig;
import com.tampir.jlastpower.R;
import com.tampir.jlastpower.activity.Main;
import com.tampir.jlastpower.utils.ContentJson;
import com.tampir.jlastpower.utils.Storage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        if(BuildConfig.BUILD_TYPE == "debug") Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            if(BuildConfig.BUILD_TYPE == "debug") Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow(remoteMessage);
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            if(BuildConfig.BUILD_TYPE == "debug") Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(RemoteMessage remoteMessage) {
        if(BuildConfig.BUILD_TYPE == "debug")  Log.d(TAG, "Short lived task is done.");
        if (remoteMessage.getFrom().matches("/topics/chatroom")){
            if(BuildConfig.BUILD_TYPE == "debug") Log.d(TAG,"Chatroom:" + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            ContentJson messsage = new ContentJson()
                            .put("id",data.get("id"))
                            .put("message",data.get("message"))
                            .put("member_id", data.get("member_id"))
                            .put("fullname",data.get("fullname"))
                            .put("msisdn",data.get("msisdn"))
                            .put("inserted_date",data.get("inserted_date"))
                            .put("foto", data.get("foto"));
            Intent intent = new Intent();
            intent.setAction("com.tampir.jlastpower.main.screen.chat_screen.Chat");
            intent.putExtra("NEWCHAT", messsage.toString());
            sendBroadcast(intent);
        }else{
            Map<String, String> data = remoteMessage.getData();
            if (data.containsKey("flag")){
                ContentJson user = App.storage.getCurrentUser();
                if (data.get("flag").matches("privatechat") && user!=null){
                    String member_to = data.get("member_to");
                    ContentJson content = new ContentJson(data.get("masage_content"));
                    if (user.getString("id").matches(member_to)){
                        ContentJson member_msg = new ContentJson()
                                .put("id",content.getString("id"))
                                .put("message",content.getString("message"))
                                .put("member_id",content.getString("member_id"))
                                .put("fullname",content.getString("fullname"))
                                .put("inserted_date",content.getString("inserted_date"))
                                .put("read","1")
                                .put("foto", content.getString("foto"))
                                .putBoolean("me", false);
                        ContentJson member = new ContentJson()
                                .put("id",content.getString("member_id"))
                                .put("member_code",content.getString("member_code"))
                                .put("fullname",content.getString("fullname"))
                                .put("foto",content.getString("foto"));
                        App.storage.saveChat(member_msg.toString(), member);
                        if (App.storage.getContent("is_active_" + member.getString("id"))!=null){
                            Intent intent = new Intent();
                            intent.setAction("com.tampir.jlastpower.main.screen.chat_screen.PrivateChat");
                            intent.putExtra("NEWCHAT", member_msg.toString());
                            sendBroadcast(intent);
                        }else{
                            //notification
                            ContentJson notif = new ContentJson()
                                    .put("message",member_msg.getString("message"))
                                    .put("foto",member_msg.getString("foto"))
                                    .put("title",member_msg.getString("fullname"));
                            chatNotification(notif);
                        }
                    }
                }else if(data.get("flag").matches("greet") && user!=null){
                    if (App.storage.getCurrentUser()!=null) {
                        ContentJson content = new ContentJson(data.get("masage_content"));
                        ContentJson configure = App.storage.getData("configure").get("data");

                        String message = content.getString("message");
                        String foto = content.getString("foto");
                        String qr = content.getString("my_qr");
                        int greet_count = content.getInt("greet_count");

                        App.storage.removeDataStartWith(Storage.ST_SCANEBARCODE);

                        user.put("qr", qr);
                        App.storage.removeCurrentUser();
                        App.storage.setCurrentUser(user.toString());

                        if (greet_count>=configure.getInt("member_greet_perday") && configure.getInt("member_greet_perday")!=0){
                            App.storage.setData(new ContentJson().put("greet","success").toString(),Storage.ST_SCANEBARCODE);
                        }
                        Intent intent = new Intent();
                        intent.setAction("com.tampir.jlastpower.main.screen.Profile");
                        intent.putExtra("DATA", content.toString());
                        sendBroadcast(intent);

                        ContentJson notif = new ContentJson()
                                .put("message", message)
                                .put("foto", foto)
                                .put("title", "GREET");
                        chatNotification(notif);
                    }
                }
            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private Context mContext;
    private void infoNotification(ContentJson messageBody) {
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        //.setLargeIcon()
                        .setContentTitle(messageBody.getString("fullname"))
                        .setContentText(messageBody.getString("message"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(400, notificationBuilder.build());
    }

    private void chatNotification(ContentJson messageBody) {
        /*
        Intent intent = new Intent(this, Flashscreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        //.setLargeIcon()
                        .setContentTitle(messageBody.getString("fullname"))
                        .setContentText(messageBody.getString("message"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(400, notificationBuilder.build());
        */
        mContext = this;
        ThumbnailNotification mThumbnailNotification = new ThumbnailNotification();
        mThumbnailNotification.execute(messageBody.toString());
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    class ThumbnailNotification extends AsyncTask<String, Void, Bitmap> {
        int mNotificationId = (int) System.currentTimeMillis();
        ContentJson json;

        protected Bitmap doInBackground(String... params) {
            try {
                json = new ContentJson(params[0]);
                return getBitmapFromURL(json.getString("foto"));
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(Bitmap image) {
            Intent intent = new Intent(mContext, Main.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(mContext, channelId)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(image)
                            .setContentTitle(json.getString("title"))
                            .setContentText(json.getString("message"))
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(mNotificationId, notificationBuilder.build());
        }
    }
}