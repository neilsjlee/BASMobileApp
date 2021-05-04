package edu.umich.neilslee.basmobileapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetPhoto extends Activity {

    CommonData commonData;

    ProgressDialog mProgressDialog;
    ImageView mImageView;
    URL url;
    AsyncTask mMyTask;
    Context context;
    Activity activity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        commonData = (CommonData)getApplication();

        setContentView(R.layout.get_photo);
        context = getApplicationContext();
        activity = this;

        mImageView = findViewById(R.id.imageView);

        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("AsyncTask");
        mProgressDialog.setMessage("Please wait, we are downloading your image file...");

        AsyncTask mMyTask;

        try {
            url = new URL("http://"+commonData.getSystemIPAddress()+":2002/cam_photo");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mMyTask = new DownloadTask().execute(url);
    }


    private class DownloadTask extends AsyncTask<URL,Void, Bitmap> {
        protected void onPreExecute(){
            mProgressDialog.show();
        }
        protected Bitmap doInBackground(URL...urls){
            URL url = urls[0];
            HttpURLConnection connection = null;
            try{
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                return BitmapFactory.decodeStream(bufferedInputStream);
            }catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }
        // When all async task done
        protected void onPostExecute(Bitmap result){
            mProgressDialog.dismiss();
            if(result!=null){
                mImageView.setImageBitmap(result);
            } else {
                // Notify user that an error occurred while downloading image
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
