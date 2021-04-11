package edu.umich.neilslee.basmobileapp;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
    }

    public void resetSystem(View v) {
        Toast.makeText(this, "SYSTEM RESET", Toast.LENGTH_LONG).show();
        commonData.removePreviousSetting();
        moveTaskToBack(true); // 태스크를 백그라운드로 이동
        finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
        android.os.Process.killProcess(android.os.Process.myPid()); // 앱 프로세스 종료
    }
}
