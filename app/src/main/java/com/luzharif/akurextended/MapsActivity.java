package com.luzharif.akurextended;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.model.Table;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMyLocationButtonClickListener,
        LocationListener,
        OnMapReadyCallback, View.OnClickListener {

    static String TAG = "Maps Activity";

    //Deklarasi nilai
    public int threshlow = 128;
    public int threshhigh = 255;
    public int filter = 1;
    public int jenisolahcitra = 1;

    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private Location location;
    private Marker markerData;

    private static final String APPLICATION_NAME = "Alat Ukur Extended";
    private static FileDataStoreFactory dataStoreFactory;
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(Environment.getExternalStorageDirectory().getPath() + "/MeterAkur");
    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static Fusiontables fusiontables;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    GoogleCredential credential;
    com.google.api.services.fusiontables.Fusiontables client;

    //Constant untuk OAUTH 2.0
    private static String CLIENT_ID = "838514003249-4upok8peqlsuqvg3tg7ec67utr2da078.apps.googleusercontent.com";
    //Use your own client id
    private static String CLIENT_SECRET = "k7x9BuOxt-dMAkCaA3lR3M11";
    //Use your own client secret
    private static String REDIRECT_URI = "http://localhost";
    private String GRANT_TYPE = "authorization_code";
    private static String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static String OAUTH_SCOPE = "https://www.googleapis.com/auth/fusiontables";

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
    static final int REQUEST_AUTHORIZATION = 1;
    static final int REQUEST_ACCOUNT_PICKER = 2;
    private final static int UPDATE_TABLE = 3;

    Dialog auth_dialog;
    WebView web;
    TextView auth_text;
    TextView Access;
    String jenisAlatUkur;

    EditText etdata1;
    EditText etdata2;
    EditText etlat;
    EditText etlong;
    Button btnSaveData;

    String data1;
    String datalat;
    String datalong;
//    String tableId;
    String keyApi;

    SharedPreferences dataPref;
    SharedPreferences.Editor dataPrefEdit;

    public Table model;
    public int numAsyncTasks;

    private String[] args = {"tableid", "data1", "data2", "lokasi", "tanggal", "authtoken", "refreshtoken"}; //to pass to constructor
    private String[] params = {"tableid", "data1", "data2", "lokasi", "tanggal", "authtoken", "refreshtoken"}; //to pass to doInBackground
    private String authToken;
    private String expire;
    private String refreshToken;
    private String data2;
    KenaliTemplate kenaliTemplate;
    private CheckBox checkBoxSatelit;
    private float akurasiGPS;
    private long lamaProses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SharedPreferences sharedPreferenceSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        jenisAlatUkur = sharedPreferenceSettings.getString("jenisalatukur", "slm");
        kenaliTemplate = new KenaliTemplate();

        //Deklarasi widget
        aturAlatUkur();
        dataPref = getSharedPreferences("DataPreference", 0);
        dataPrefEdit = dataPref.edit();
//        tableId = dataPref.getString("idfusiontables", "1bG3l1PmAe9WxZuOcpmp3tu_Y9zGmKgWn_tQsnEGY"); //TODO Berikan macam2 tabel
        keyApi = dataPref.getString("apifusiontables", "AIzaSyBMBTE_35LEba7tfghu4QKFa93eHn8WxIk");
        authToken = dataPref.getString("accessToken", "0");
        refreshToken = dataPref.getString("refreshToken", "0");

        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

//        authToken = "kosong"; //Untuk menguji fungsi refresh Token
        credential = new GoogleCredential().setAccessToken(authToken);
        fusiontables = new Fusiontables.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("AkurExtended")
                .build();

        lamaProses = 0;
        akurasiGPS = 0;
    }

    private void aturAlatUkur() {
        LinearLayout layoutJenisAlatUkur = (LinearLayout)findViewById(R.id.layoutEditData);
        switch (jenisAlatUkur) {
            case "slm":
                View layoutSlm = getLayoutInflater().inflate(R.layout.content_editsoundlevelmeter, layoutJenisAlatUkur, true);
                etdata1 = (EditText) findViewById(R.id.dataDesibel);
                etdata2 = (EditText) findViewById(R.id.dataDesibelPerbaikan);
                break;
            case "co":
                View layoutCo = getLayoutInflater().inflate(R.layout.content_editcometer, layoutJenisAlatUkur, true);
                etdata1 = (EditText) findViewById(R.id.dataCo);
                etdata2 = (EditText) findViewById(R.id.dataSuhu);
                break;
            default:
                View layoutDefault = getLayoutInflater().inflate(R.layout.content_editsoundlevelmeter, layoutJenisAlatUkur, true);
                etdata1 = (EditText) findViewById(R.id.dataDesibel);
                break;
        }
        etlat = (EditText) findViewById(R.id.datalatitudetes);
        etlong = (EditText) findViewById(R.id.datalongitudetes);
        auth_text = (TextView) findViewById(R.id.auth_status);
        Access = (TextView) findViewById(R.id.access_token);

        checkBoxSatelit = (CheckBox) findViewById(R.id.modesatelit);
        btnSaveData = (Button) findViewById(R.id.savedatates);
        btnSaveData.setOnClickListener(this);
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, MapsActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        //Set listener
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng dragPosition = marker.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLong = dragPosition.longitude;
        Log.i("info", "on drag end :" + dragLat + " dragLong :" + dragLong);
        //Toast.makeText(getApplicationContext(), "Marker Dragged..!" + dragLat + ", " + dragLong, Toast.LENGTH_LONG).show();
        datalat = Double.toString(dragLat);
        datalong = Double.toString(dragLong);
        etlat.setText(Double.toString(dragLat));
        etlong.setText(Double.toString(dragLong));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);  // LocationListener
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        kasihMarkerDanKoor();
        akurasiGPS = location.getAccuracy();
        Toast.makeText(this, "Akurasi : " + akurasiGPS + " meter", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        //mMessageView.setText("Location = " + location);
    }

//    public void legitlah(View v) {
//        Toast.makeText(this, "Kepencet", Toast.LENGTH_SHORT);
//    }

    public boolean kasihMarkerDanKoor() {
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (markerData == null) {
                markerData = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude)) //location.getLatitude(),location.getLongitude()
                        .title("Location")
                        .draggable(true)
                        .snippet("Data"));
                datalat = Double.toString(latitude);
                datalong = Double.toString(longitude);
                etlat.setText(Double.toString(latitude));
                etlong.setText(Double.toString(longitude));
            } else if (markerData != null) {
                mMap.clear();
                markerData = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude)) //location.getLatitude(),location.getLongitude()
                        .title("Location")
                        .draggable(true)
                        .snippet("Data"));
                datalat = Double.toString(latitude);
                datalong = Double.toString(longitude);
                etlat.setText(Double.toString(latitude));
                etlong.setText(Double.toString(longitude));
            }
        } else {
        }
        return false;
    }

    public void tipeMap(View v) {
        if(checkBoxSatelit.isChecked())
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onClick(View v) {
        switch (jenisAlatUkur) {
            case "slm":
                data1 = etdata1.getText().toString();
                data2 = etdata2.getText().toString();
                break;
            case "co":
                data1 = etdata1.getText().toString();
                data2 = etdata2.getText().toString();
                break;
            default:
                data1 = etdata1.getText().toString();
                break;
        }
        if (datalat != null && datalong != null) {
            new MyAsyncTask().execute(data1, datalat, datalong);
        }
    }

    //Callback for AsyncTask to call when its completed
    public void taskDone(String returnVal) {
        Toast.makeText(getApplicationContext(), returnVal, Toast.LENGTH_SHORT).show();
    }

    private class MyAsyncTask extends AsyncTask<String, Double, Double> {

        private String statusKirim;

        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
//            postData(data1);
            statusKirim = "Time out gan internetnya";
            String stringSQL;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            SimpleDateFormat sdj = new SimpleDateFormat("HH:mm", Locale.US);
            String tanggal = sdf.format(new Date());
            String jam = sdj.format(new Date());

            stringSQL = kenaliTemplate.stringSQLFusionTable(jenisAlatUkur, data1,
                    data2, datalat, datalong, tanggal, jam, lamaProses, akurasiGPS);

            try {
                fusiontables.query().sql(stringSQL).setOauthToken(authToken).execute();
                statusKirim = "Data berhasil dikirim";
                return null;
            } catch (IOException err) {
                Log.d(TAG, "Error IOException: " + err.getMessage());
                GRANT_TYPE = "refresh_token";
                GetAccessToken jParser = new GetAccessToken();
                JSONObject json = jParser.getnewtoken(TOKEN_URL, CLIENT_ID, CLIENT_SECRET,
                        refreshToken, GRANT_TYPE);
                if (json != null) {
                    try {
                        authToken = json.getString("access_token");
                        dataPrefEdit.putString("accessToken", authToken);
                        dataPrefEdit.commit();
                        try {
                            credential = new GoogleCredential().setAccessToken(authToken);
                            fusiontables = new Fusiontables.Builder(new NetHttpTransport(),
                                    JacksonFactory.getDefaultInstance(), credential)
                                    .setApplicationName("AkurExtended")
                                    .build();
                            fusiontables.query().sql(stringSQL).setOauthToken(authToken).execute();
                            statusKirim = "Maaf agak lama, tapi berhasil dikirim kok";
                        } catch (IOException e) {
                            e.printStackTrace();
                            statusKirim = "Gagal unggah gan, silakan otorisasi kembali";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Double result) {
//            pb.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), statusKirim, Toast.LENGTH_SHORT).show();
        }
    }
}
