package edu.umich.neilslee.basmobileapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class HttpGetData extends AsyncTask<String, Void, Void> {
    private String strUrl = "";
    private URL Url;
    private String strCookie;
    private String result;
    CommonData commonData;

    public HttpGetData(CommonData cd) {
        commonData = cd;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //strUrl = "http://192.168.1.89:2002/current_status"; //탐색하고 싶은 URL이다.
        //strUrl = "http://webhook.site/df44f30b-c491-4336-8f11-8740631e0639"; //test purpose..
        // https://webhook.site/#!/df44f30b-c491-4336-8f11-8740631e0639/3c43193e-e291-4cb9-b85d-1924ca14b219/1
    }

    @Override
    protected Void doInBackground(String... params) {
        try{
            Url = new URL("http://"+commonData.getSystemIPAddress()+":2002/"+params[0]); // URL화 한다.
            HttpURLConnection conn = (HttpURLConnection) Url.openConnection(); // URL을 연결한 객체 생성.
            conn.setRequestMethod("GET"); // get방식 통신
            conn.setDoInput(true); // 읽기모드 지정
            conn.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
            conn.setDefaultUseCaches(false); // 캐싱데이터 디폴트 값 설정

            strCookie = conn.getHeaderField("Set-Cookie"); //쿠키데이터 보관

            InputStream is = conn.getInputStream(); //input스트림 개방

            StringBuilder builder = new StringBuilder(); //문자열을 담기 위한 객체
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8")); //문자열 셋 세팅
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
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        System.out.println(result);
    }

}

