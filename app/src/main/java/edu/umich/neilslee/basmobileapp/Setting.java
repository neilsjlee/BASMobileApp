package edu.umich.neilslee.basmobileapp;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import androidx.core.app.ActivityCompat;


public class Setting extends Activity implements NfcAdapter.CreateNdefMessageCallback {

    CommonData commonData;
    NfcAdapter nfcAdapter;

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        //get Wi-Fi SSID
        String ssidStr = commonData.getHomeWiFiSSID();
        String pwStr = commonData.getHomeWiFiPW();
        String ipStr = commonData.getSystemPrivateIPAddress();



        String text = ( "{\"SSID\":\"" + ssidStr +
                "\", \"PW\":\""   + pwStr +
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commonData = (CommonData) getApplication();
        setContentView(R.layout.setting);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    public void registerNewDevice(View v) {
        // Check for available NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        nfcAdapter.setNdefPushMessageCallback(this, this);
        Toast.makeText(this, "NFC Started\nTap this phone to a new device.", Toast.LENGTH_LONG).show();
        final Setting activity = this;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // NFC will be stopped after 30 seconds.
                nfcAdapter.setNdefPushMessageCallback(null, activity);
                Log.d("Setting","NFC stopped.");
            }
        }, 30000);
    }

    public void resetSystem(View v) throws ExecutionException, InterruptedException {
        String mailbox_address = new HttpPostData(getParent(), commonData).execute("all_deregister", "IGNORE_DEVICE_ID").get();
        Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address);
        Toast.makeText(this, "SYSTEM RESET", Toast.LENGTH_LONG).show();
        commonData.removePreviousSetting();
        moveTaskToBack(true);
        finishAndRemoveTask();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void publicIpUnavailableButton(View v){
        boolean on = ((ToggleButton) v).isChecked();
        commonData.set_public_ip_unavailable(on);

    }
}
