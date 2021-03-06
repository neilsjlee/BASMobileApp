package edu.umich.neilslee.basmobileapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CommonData extends Application {
    private String HomeWiFiSSID;
    private String HomeWiFiPW;
    private String ControlHubIPAddress;
    private String ControlHubPrivateIPAddress;
    private String mostRecentSystemStatus;
    private JSONArray mostRecentSystemStatusJsonArray;
    private JSONObject mostRecentSystemStatusJsonObject;
    private boolean previous_save_exists = false;
    private boolean public_ip_unavailable = false;


    private final String SETTING_FILE_NAME = "commondata_save_file.txt";

    public void onCreate() {
        super.onCreate();
        ControlHubIPAddress = "";
        mostRecentSystemStatus = "";
        mostRecentSystemStatusJsonObject = null;
        //load_previous_setting();
    }

    public boolean load_previous_setting(){
        try {
            StringBuffer data = new StringBuffer();
            FileInputStream fis = openFileInput(SETTING_FILE_NAME);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
            String str = buffer.readLine();
            Log.d("load_previous_setting", "Read String: " + str);
            while(str != null){
                data.append(str + "\n");
                str = buffer.readLine();
            }
            JSONObject jsonObject = new JSONObject(data.toString());
            setSystemIPAddress2(jsonObject.getString("public_ip"), jsonObject.getString("private_ip"));
            setHomeWiFiInfo(jsonObject.getString("ssid"), jsonObject.getString("pw"));
            Log.d("load_previous_setting", "Set Value: " + ControlHubIPAddress + ", " + ControlHubPrivateIPAddress + ", " + HomeWiFiSSID + ", " + HomeWiFiPW);

            fis.close();

            previous_save_exists = true;

        } catch (FileNotFoundException e) {
            Log.d("CommonData", "Previous Setting File does not exist.");
            previous_save_exists = false;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return previous_save_exists;
    }

    private void save_setting() throws IOException {
        //FileOutputStream fos = openFileOutput(SETTING_FILE_NAME, Context.MODE_APPEND);
        File f = new File(getFilesDir().getPath()+"/"+SETTING_FILE_NAME);
        if(f.delete()){
            Log.d("save_setting", "Previous Setting File Deleted");
        }
        else{
            Log.d("save_setting", "Previous Setting File Not Deleted");
        }
        f.createNewFile();
        PrintWriter printWriter = new PrintWriter(f);
        Log.d("save_setting", "{\"public_ip\":\""+ControlHubIPAddress+"\", \"private_ip\":\""+ControlHubPrivateIPAddress+"\", \"ssid\":\""+HomeWiFiSSID+"\", \"pw\":\""+HomeWiFiPW+"\"}");
        Log.d("save_setting", "file path: "+f.getPath());
        printWriter.println("{\"public_ip\":\""+ControlHubIPAddress+"\", \"private_ip\":\""+ControlHubPrivateIPAddress+"\", \"ssid\":\""+HomeWiFiSSID+"\", \"pw\":\""+HomeWiFiPW+"\"}");
        printWriter.close();
    }

    public String getSystemIPAddress(){
        if (!public_ip_unavailable){
            return ControlHubIPAddress;
        }
        else {
            return ControlHubPrivateIPAddress;
        }
    }

    public String getSystemPrivateIPAddress(){
        return ControlHubPrivateIPAddress;
    }

    public String getHomeWiFiSSID(){
        return HomeWiFiSSID;
    }

    public String getHomeWiFiPW(){
        return HomeWiFiPW;
    }

    public void setSystemIPAddress(String public_ip, String private_ip){
        ControlHubIPAddress = public_ip;
        ControlHubPrivateIPAddress = private_ip;
        try{
            save_setting();
            Log.d("CommonData save_setting", "Setting Saved");
            StringBuffer data = new StringBuffer();
            FileInputStream fis = openFileInput(SETTING_FILE_NAME);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
            String str = buffer.readLine();
            Log.d("CommonData save_setting", "Read String: " + str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("CommonData save_setting", "Failed to save setting");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent("fromCommonData");
        intent.putExtra("message", ControlHubIPAddress + ", " + ControlHubPrivateIPAddress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void setSystemIPAddress2(String public_ip, String private_ip){
        ControlHubIPAddress = public_ip;
        ControlHubPrivateIPAddress = private_ip;
    }


    public void setHomeWiFiInfo(String ssid, String pw){
        HomeWiFiSSID = ssid;
        HomeWiFiPW = pw;
    }

    public String getMostRecentSystemStatus(){
        return mostRecentSystemStatus;
    }

    public JSONObject getMostRecentSystemStatusJson() throws JSONException {
        return mostRecentSystemStatusJsonObject;
    }

    public void setSystemStatus(String s) throws JSONException {
        mostRecentSystemStatus = s;
        try {
            mostRecentSystemStatusJsonObject = new JSONObject(mostRecentSystemStatus);
        } catch(JSONException e){
            e.printStackTrace();
        };
    }

    public void removePreviousSetting(){
        try{
            File dir = getFilesDir();
            File file = new File(dir, SETTING_FILE_NAME);
            Log.d("CommonData","Previous Setting File Exist: "+file.exists());
            file.delete();
            Log.d("CommonData","Previous Setting File Deleted");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("CommonData","Previous Setting File could not be deleted");
        }

    }

    public void set_public_ip_unavailable(boolean b){
        public_ip_unavailable = b;
    }

}

