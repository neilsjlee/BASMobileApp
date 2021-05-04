package edu.umich.neilslee.basmobileapp;



import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;




public class ControlHubSetting extends Activity implements CreateNdefMessageCallback {
    NfcAdapter nfcAdapter;
    TextView textView;
    CommonData commonData;

    BufferedReader in;
    Socket socket;
    String msg="";
    boolean read_flag = true;
    boolean nfc_setting_success = false;

    private final int REQUEST_PERMISSION_LOCATION_STATE=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commonData = (CommonData)getApplication();
        setContentView(R.layout.control_hub_setting);
        TextView textView = (TextView) findViewById(R.id.nfc_read);

        try {
            getCurrentSSID();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        //get Wi-Fi SSID
        textView = (TextView) findViewById(R.id.acquired_ssid);
        String ssidStr = textView.getText().toString();

        //get Wi-Fi password
        TextInputEditText wifi_password_TIET = (TextInputEditText) findViewById(R.id.wifi_password);

        //get IP Address
        String ipStr = "";
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService (Context.WIFI_SERVICE);
        int ipInt = wifiManager.getConnectionInfo().getIpAddress();
        try {
            ipStr = InetAddress.getByAddress(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array())
                    .getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        commonData.setHomeWiFiInfo(ssidStr, wifi_password_TIET.getText().toString());


        String text = ( "{\"SSID\":\"" + ssidStr +
                        "\", \"PW\":\""   + wifi_password_TIET.getText().toString() +
                        "\", \"IP\":\""   + ipStr + "\"}");
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime("application/edu.umich.neilslee.basmobileapp", text.getBytes())
                        //"application/vnd.com.example.android.beam", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                        */
                        //,NdefRecord.createApplicationRecord("com.example.android.beam")
                });
        return msg;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {

        textView = (TextView) findViewById(R.id.nfc_read);
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        Log.i("Sangjun_NFC_Setting", new String(msg.getRecords()[0].getPayload()));
        textView.setText(new String(msg.getRecords()[0].getPayload()));
    }

    public void startNDEFPush(View v) {
        // Check for available NFC Adapter
        long start_time = System.currentTimeMillis();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        nfcAdapter.setNdefPushMessageCallback(this, this);
        Toast.makeText(this, "NFC Started", Toast.LENGTH_LONG).show();
        ServerSocketOpen();
        final ControlHubSetting activity = this;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 30 seconds
                nfcAdapter.setNdefPushMessageCallback(null, activity);
                Log.d("ControlHubSetting","NFC stopped.");
            }
        }, 30000);
    }

    public void ServerSocketOpen(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = null;
                    ServerSocket myServerSocket = new ServerSocket(3000);
                    myServerSocket.setReuseAddress(true);
                    Log.d("Socket","waiting for connection");
                    socket = myServerSocket.accept();
                    read_flag = true;
                    // input_stream = new DataInputStream(socket.getInputStream());
                    // output_stream = new DataOutputStream(socket.getOutputStream());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while(read_flag){
                        try{
                            msg = in.readLine();
                            Log.d("Socket", "Received Message: " + msg);
                            try{
                                jsonObject = new JSONObject(msg);
                                commonData.setSystemIPAddress(jsonObject.getString("public_ip"),jsonObject.getString("private_ip"));
                                textView = (TextView) findViewById(R.id.nfc_read);
                                textView.setText("SETTING DONE\n\nControl Hub is now online.\nPlease register other devices.");
                                synchronized (this) {
                                    this.wait(5000);
                                }
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            read_flag = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    myServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void getCurrentSSID() throws InterruptedException {
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        getPermissionForAccessFineLocation();
        Context context = getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ssid_all  = info.getSSID();
        String[] ssid =ssid_all.split("\"");
        try{
            textView = (TextView) findViewById(R.id.acquired_ssid);
            textView.setText(ssid[1]);
        } catch(Exception e) {
            textView.setText("Wi-Fi Unavailable");
        }
    }

    public void getSSIDButton(View v) throws InterruptedException {
        getCurrentSSID();
    }

    private void getPermissionForAccessFineLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_LOCATION_STATE);
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_LOCATION_STATE);
            }
        } else {
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }
}