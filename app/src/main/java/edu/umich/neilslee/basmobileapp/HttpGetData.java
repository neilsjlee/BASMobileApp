package edu.umich.neilslee.basmobileapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class HttpGetData extends AsyncTask<String, Void, Void> {
    private String task = "";
    private String strUrl = "";
    private URL Url;
    private String strCookie;
    private String result;
    ImageView mImageView;
    CommonData commonData;

    public HttpGetData(CommonData cd) {
        commonData = cd;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //strUrl = "http://192.168.1.89:2002/current_status";
        //strUrl = "http://webhook.site/df44f30b-c491-4336-8f11-8740631e0639"; //test purpose..
        // https://webhook.site/#!/df44f30b-c491-4336-8f11-8740631e0639/3c43193e-e291-4cb9-b85d-1924ca14b219/1
    }

    @Override
    protected Void doInBackground(String... params) {
        if (params[0] == "current_status"){
            try{
                task = "current_status";
                Url = new URL("http://"+commonData.getSystemIPAddress()+":2002/"+params[0]);
                HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);

                strCookie = conn.getHeaderField("Set-Cookie");

                InputStream is = conn.getInputStream();

                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line+ "\n");
                }

                result = builder.toString();

                Log.d("HttpGetData", result);
                commonData.setSystemStatus(result);

            }catch(MalformedURLException | ProtocolException exception) {
                exception.printStackTrace();
            }catch(IOException io){
                io.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (params[0] == "cam_photo"){
            try{
                task = "cam_photo";
                Url = new URL("http://"+commonData.getSystemIPAddress()+":2002/"+params[0]);
                HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);

                strCookie = conn.getHeaderField("Set-Cookie");

                InputStream is = conn.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(is);

                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line+ "\n");
                }

                result = builder.toString();

                Log.d("HttpGetData", result);
                commonData.setSystemStatus(result);

            }catch(MalformedURLException | ProtocolException exception) {
                exception.printStackTrace();
            }catch(IOException io){
                io.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // System.out.println(result);
        if (task.equals("cam_photo")){


        }
    }

}

