package edu.umich.neilslee.basmobileapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    ArrayList<String> listItem;
    ListView listView1;
    CommonData commonData;
    public static Context context_main;
    public ProgressDialog progressDialog;
    String mailbox_address_on_hold = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        context_main = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading");

        commonData = (CommonData)getApplication();

        listItem = new ArrayList<String>();
        //listItem.add(" ");
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listItem);
        listView1 = findViewById(R.id.listView1);
        listView1.setAdapter(adapter);

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Toast.makeText(MainActivity.this ,listItem.get(position),Toast.LENGTH_SHORT).show();
                final PopupMenu popupMenu = new PopupMenu(getApplicationContext(),view);
                getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if ((listItem.get(position)).split("/")[0] == "ALL DEVICES") {
                            if (menuItem.getItemId() == R.id.action_menu1){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("scr_manual_all","IGNORE_DEVICE_ID").get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else if (menuItem.getItemId() == R.id.action_menu2){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("all_deregister", "IGNORE_DEVICE_ID").get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else if (menuItem.getItemId() == R.id.action_menu3){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("arm_request_all", "IGNORE_DEVICE_ID").get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else if (menuItem.getItemId() == R.id.action_menu4){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("disarm_request_all", "IGNORE_DEVICE_ID").get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (menuItem.getItemId() == R.id.action_menu1){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("scr_manual_single", (listItem.get(position)).split("/")[0]).get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else if (menuItem.getItemId() == R.id.action_menu2){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("deregister", (listItem.get(position)).split("/")[0]).get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else if (menuItem.getItemId() == R.id.action_menu3){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("arm_request_single", (listItem.get(position)).split("/")[0]).get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else if (menuItem.getItemId() == R.id.action_menu4){
                                progressDialog.show();
                                try {
                                    mailbox_address_on_hold = new HttpPostData(getParent(), commonData).execute("disarm_request_single", (listItem.get(position)).split("/")[0]).get();
                                    Log.d("MainActivity","MAILBOX ADDRESS RECEIVED: "+mailbox_address_on_hold);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        Intent intent = new Intent(this, MQTTservice.class);
        startService(intent);

        LocalBroadcastManager.getInstance(this).registerReceiver( intentMessageReceiver, new IntentFilter("fromMqttService"));

        boolean previous_setting_exist = commonData.load_previous_setting();

        Log.d("Main onCreate","previous_setting_exist: "+previous_setting_exist);

        if(!previous_setting_exist){
            Intent intent2 = new Intent(getApplicationContext(), ControlHubSetting.class);
            startActivity(intent2);
        } else {
            try {
                new HttpGetData(commonData).execute("current_status").get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                update_sysem_status_view();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private BroadcastReceiver intentMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Main IntentReceiver", "Got message... something");
            String message = intent.getStringExtra("message");
            Log.d("Main IntentReceiver", "Got message: " + message);
            try {
                commonData.setSystemStatus(message.split("\\*")[1]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                update_sysem_status_view();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();
        }
    };

    public void openSetting(View v){
        Intent intent = new Intent(getApplicationContext(), Setting.class);
        startActivity(intent);
    }

    public void request_new_system_status() throws ExecutionException, InterruptedException {
        //new HttpGetData(commonData).execute("current_status").get();
        new HttpPostData(getParent(), commonData).execute("scr_manual_all", "NO_DEVICE_ID");
    }

    public void get_new_system_status(View v) throws JSONException, ExecutionException, InterruptedException {
        progressDialog.show();
        request_new_system_status();
        //update_sysem_status_view();
    }

    public void update_sysem_status_view() throws JSONException {
        JSONObject latest_system_status = commonData.getMostRecentSystemStatusJson();
        listItem.clear();
        ArrayList<String> deviceList = new ArrayList<>();
        try {
            if (latest_system_status != null){
                Iterator iterator = latest_system_status.keys();
                while (iterator.hasNext()) {
                    String str = iterator.next().toString();
                    deviceList.add(str);
                }

                if (deviceList.size() > 1) {
                    listItem.add("ALL DEVICES/");
                }

                for (int i = 0; i < deviceList.size(); i++) {
                    String did = deviceList.get(i);
                    JSONObject subJsonObject = latest_system_status.getJSONObject(did);

                    String deviceType = subJsonObject.getString("device_type");
                    String devicePower = subJsonObject.getString("device_power");
                    String lastStatusUpdateTime = subJsonObject.getString("last_status_update_time");
                    String upTime = subJsonObject.getString("up_time");
                    Boolean armed = subJsonObject.getBoolean("armed");
                    String armedState = "";

                    if (armed) {
                        armedState = "Armed";
                    } else {
                        armedState = "Disarmed";
                    }
                    listItem.add(did + "/" + deviceType + " - " + armedState);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(MainActivity.this, "There is no registered device.\nPlease register devices to the system.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String convertInputStreamToString(InputStream is) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        // Java 1.1
        return result.toString(StandardCharsets.UTF_8.name());

        // Java 10
        // return result.toString(StandardCharsets.UTF_8);

    }

} // Activity
