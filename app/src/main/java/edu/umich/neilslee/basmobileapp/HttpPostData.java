package edu.umich.neilslee.basmobileapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Output;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class HttpPostData extends AsyncTask<String, Void, String> {
    CommonData commonData;
    String uri = "";
    String target_did = "";
    Context context;

    Long start_time;
    Long end_time;



    public HttpPostData(Context _ctx, CommonData _cd){
        context = _ctx;
        commonData = _cd;
    }

    @Override
    protected void onPreExecute(){

        super.onPreExecute();
    }


    @Override
    protected String doInBackground(String... params) {
        start_time = System.currentTimeMillis();
        Log.d("TIME MEASUREMENT", "HTTP POST MESSAGE TIME TAKEN 1");

        try {
            URL url = new URL("http://"+commonData.getSystemIPAddress()+":2002/"+params[0]);
            //URL url = new URL("https://ptsv2.com/t/ra4ej-1616855109/post");
            //URL url = new URL("https://webhook.site/df44f30b-c491-4336-8f11-8740631e0639"); // test purpose..
            // https://webhook.site/#!/df44f30b-c491-4336-8f11-8740631e0639/3c43193e-e291-4cb9-b85d-1924ca14b219/1
            HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();

            httpCon.setRequestMethod("POST");
            httpCon.setRequestProperty("Content-type", "application/json");
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);
            httpCon.setDoOutput(true);


            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("device_id", params[1]);

            // convert JSONObject to JSON to String
            String json = "";
            json = jsonObject.toString();

            DataOutputStream dos = new DataOutputStream(httpCon.getOutputStream());
            dos.write(json.getBytes());
            dos.flush();

            Log.d("TIME MEASUREMENT", "HTTP POST MESSAGE TIME TAKEN 1-1");

            int status = httpCon.getResponseCode();
            // this line is actually where the POST request gets sent.
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    String mbox_address = sb.toString().split("\"")[3];
                    return mbox_address;
            }

            Log.d("TIME MEASUREMENT", "HTTP POST MESSAGE TIME TAKEN 1-2");

            dos.close();
            httpCon.disconnect();
            Log.d("TIME MEASUREMENT", "HTTP POST MESSAGE TIME TAKEN 1-3");

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d("HttpPostData", "MalformedURLException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("HttpPostData", "IOException");
        } // try
        catch (JSONException e) {
            e.printStackTrace();
            Log.d("HttpPostData", "JSONException");
        }
        Log.d("TIME MEASUREMENT", "HTTP POST MESSAGE TIME TAKEN 2");
        return null;
    }

    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
        end_time = System.currentTimeMillis();
        Log.d("TIME MEASUREMENT","HTTP POST MESSAGE TIME TAKEN: "+(end_time - start_time));
    }

    private BroadcastReceiver intentMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("Main IntentReceiver", "Got message: " + message);
        }
    };

}
