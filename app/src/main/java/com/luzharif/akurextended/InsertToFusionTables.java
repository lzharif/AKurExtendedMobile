package com.luzharif.akurextended;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.plus.Plus;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuZharif on 13/09/2015.
 */
public class InsertToFusionTables extends AsyncTask <String, String, String> {

    private MapsActivity activity;

    private static Fusiontables fusiontables;
    private GoogleCredential credential;
    private Fusiontables.Query.Sql sql;

    private String AUTH_TOKEN;
    private String REFRESH_TOKEN;
    private String TABLE_ID;
    private String DATA1;
    private String DATA2;
    private String LOKASI;
    private String TANGGAL;
    private String stringSQL;

    public InsertToFusionTables(String[] args, MapsActivity activity) {
        this.TABLE_ID = args[0];
        this.DATA1= args[1];
        this.DATA2 = args[2];
        this.LOKASI = args[3];
        this.TANGGAL = args[4];
        this.AUTH_TOKEN = args[5];
        this.REFRESH_TOKEN = args[6];

        this.activity = activity;

        credential = new GoogleCredential().setAccessToken(AUTH_TOKEN);
        fusiontables = new Fusiontables.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("MyProject")
                .build();
    }

    public int position;
    Context context;
    public void AsyncMsg(int position,Context con) {
        this.position = position;
        context =con;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        stringSQL = "INSERT INTO " + TABLE_ID + " (KadarCO, Temperatur, Lokasi, Tanggal) "
                + "VALUES ('" + DATA1 + "', '" + DATA2 + "', '" + LOKASI + "', '" + TANGGAL + "')";
    }


    @Override
    protected String doInBackground(String... params) {
//        HttpClient httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost("https://www.googleapis.com/fusiontables/v2/tables");

        //Post Data
        try {
            fusiontables.query().sql(stringSQL).setOauthToken(AUTH_TOKEN).execute();
//            sql = fusiontables.query().sql(stringSQL);
//            sql.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute() {
        activity.taskDone("Class InsertToFusionTables selesai");
        Toast.makeText(context, "Legit", Toast.LENGTH_SHORT);
    }


}
