package edu.umich.neilslee.basmobileapp;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MQTTservice extends Service {
    private MqttClient mqttClient;
    private String mqtt_broker_ip = "";
    private String MQTT_BROKER_PORT = "2005";
    private String TOPIC = "system_to_mobile_app";
    CommonData commondata;


    private void push_notification(String notification_type, String did){
        if (notification_type.equals("alert")) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("ALERT")
                    .setContentText("DEVICE " + did + " DETECTED MOTION")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(new NotificationChannel("default", "default channel", NotificationManager.IMPORTANCE_DEFAULT));
                notificationManager.notify(1, builder.build());
            }
        }

    }

    private void connectMqtt() throws Exception {
        Log.d(getClass().getCanonicalName(), "MQTT Starting");
        mqttClient = new MqttClient("tcp://"+ commondata.getSystemIPAddress() + ":" + MQTT_BROKER_PORT, MqttClient.generateClientId(),null);
        mqttClient.connect();
        Log.d(getClass().getCanonicalName(), "MQTT Connected");
        mqttClient.subscribe(TOPIC);
        Log.d(getClass().getCanonicalName(), "Subscribed: "+TOPIC);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(getClass().getCanonicalName(),"Mqtt ReConnect - " + mqtt_broker_ip);
                Boolean failing = Boolean.TRUE;
                while(failing) {
                    try {
                        connectMqtt();
                        failing = Boolean.FALSE;
                    } catch (Exception e) {
                        Log.d(getClass().getCanonicalName(), "MqttReConnect Error");
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(getClass().getCanonicalName(),"MQTT Message Received");
                String str = new String(message.getPayload(), "UTF-8");
                Log.d(getClass().getCanonicalName(), "Received MQTT Message: "+str);
                if (str.contains("*")){
                    sendMessage(str);
                    Log.d(getClass().getCanonicalName(), "Received MQTT Message - MBOX ADDR: "+str.split("\\*")[0]);
                    String temp = str.split("\\*")[1];
                    str = temp;
                }
                JSONObject json = new JSONObject(str);
                //Log.d(getClass().getCanonicalName(),json.getString("id") + json.getString("content"));
                Log.d(getClass().getCanonicalName(), "Received MQTT Message: "+json.toString());
                sendMessage(str);
                if (json.getString("message_type").equals("alert")){
                    push_notification("alert", json.getString("device_id"));
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    private void sendMessage(String string){
        Log.d("messageService", "Broadcasting message");
        Intent intent = new Intent("fromMqttService");
        intent.putExtra("message", string);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private BroadcastReceiver intentMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("Service IntentReceiver", "Got message: " + message);
            mqtt_broker_ip = commondata.getSystemIPAddress();
            try {
                mqttClient.disconnect();
                Log.d(getClass().getCanonicalName(), "MQTT Disconnected");
            } catch (MqttException e) {
                e.printStackTrace();
            }
            try {
                Log.d(getClass().getCanonicalName(), "MQTT Reconnect - " + mqtt_broker_ip);
                connectMqtt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        commondata = (CommonData)getApplication();
        LocalBroadcastManager.getInstance(this).registerReceiver( intentMessageReceiver, new IntentFilter("fromCommonData"));

        try {
            connectMqtt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        try {
            mqttClient = new MqttClient("tcp://"+ mqtt_broker_ip + ":" + MQTT_BROKER_PORT, MqttClient.generateClientId(),null);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            mqttClient.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            mqttClient.subscribe(TOPIC);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(getClass().getCanonicalName(),"Mqtt ReConnect");
                try{mqttClient.connect();}catch (Exception e){}
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

         */

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}