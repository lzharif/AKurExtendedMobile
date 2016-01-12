package com.luzharif.akurextended;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditData extends FragmentActivity implements
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMyLocationButtonClickListener,
        LocationListener,
        OnMapReadyCallback, View.OnClickListener {

    static String TAG = "EditData";

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

    EditText etdata1;
    EditText etdata2;
    EditText etlat;
    EditText etlong;
    Button btnSaveData;
    ImageView imgOlah;

    Bitmap citraolah;

    SharedPreferences dataPref;
    SharedPreferences.Editor dataPrefEdit;
    String data1hasilocr;
    String data2hasilocr;
    String datalat;
    String datalong;
    String stringSQL;
    String authToken;
    String refreshToken;
    GoogleCredential credential;
    Fusiontables fusiontables;
    KenaliTemplate kenaliTemplate;

    //Constant untuk OAUTH 2.0
    private static String CLIENT_ID = "360891658854-2743aho3je1dmqajqthblouogotvcbv2.apps.googleusercontent.com";
    //Use your own client id
    private static String CLIENT_SECRET = "8TRIIi_37RUQODEvVDoPpTIp";
    //Use your own client secret
    private static String REDIRECT_URI = "http://localhost";
    private String GRANT_TYPE = "authorization_code";
    private static String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static String OAUTH_SCOPE = "https://www.googleapis.com/auth/fusiontables";
    private String jenisAlatUkur;
    float akurasiGPS;
    private CheckBox checkBoxSatelit;
    private long lamaProses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editdata);

        SharedPreferences sharedPreferenceSettings = PreferenceManager.
                getDefaultSharedPreferences(getBaseContext());
        jenisAlatUkur = sharedPreferenceSettings.getString("jenisalatukur", "slm");

        kenaliTemplate = new KenaliTemplate();

        dataPref = getSharedPreferences("DataPreference", 0);
        dataPrefEdit = dataPref.edit();
        authToken = dataPref.getString("accessToken", "0");
        refreshToken = dataPref.getString("refreshToken", "0");

        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Deklarasi widget
        aturWidget();
        credential = new GoogleCredential().setAccessToken(authToken);
        fusiontables = new Fusiontables.Builder(new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("MyProject")
                .build();
        akurasiGPS = 0;
    }

    private void aturWidget() {
        Bundle ambilinfo = getIntent().getExtras();
        String filename = ambilinfo.getString("filename");
        data1hasilocr = ambilinfo.getString("string1hasilocr");
        data2hasilocr = ambilinfo.getString("string2hasilocr");
        lamaProses = ambilinfo.getLong("lamaproses");

        LinearLayout layoutJenisAlatUkur = (LinearLayout)findViewById(R.id.layoutEditData);
        switch (jenisAlatUkur) {
            case "slm":
                View layoutSlm = getLayoutInflater().inflate(R.layout.content_editsoundlevelmeter,
                        layoutJenisAlatUkur, true);
                etdata1 = (EditText) findViewById(R.id.dataDesibel);
                etdata2 = (EditText) findViewById(R.id.dataDesibelPerbaikan);
                etdata1.setText(data1hasilocr);
                etdata2.setText(data2hasilocr);
                break;
            case "co":
                View layoutCo = getLayoutInflater().inflate(R.layout.content_editcometer,
                        layoutJenisAlatUkur, true);
                etdata1 = (EditText) findViewById(R.id.dataCo);
                etdata2 = (EditText) findViewById(R.id.dataSuhu);
                etdata1.setText(data1hasilocr);
                etdata2.setText(data2hasilocr);
                break;
            default:
                View layoutDefault = getLayoutInflater().inflate(R.layout.content_editsoundlevelmeter,
                        layoutJenisAlatUkur, true);
                etdata1 = (EditText) findViewById(R.id.dataDesibel);
                etdata1.setText(data1hasilocr);
                break;
        }
        etlat = (EditText) findViewById(R.id.datalatitude);
        etlong = (EditText) findViewById(R.id.datalongitude);
        imgOlah = (ImageView) this.findViewById(R.id.citraolah);
        checkBoxSatelit = (CheckBox) findViewById(R.id.gantimodesatelit);
        btnSaveData = (Button) findViewById(R.id.savedata);
        btnSaveData.setOnClickListener(this);

        citraolah = BitmapFactory.decodeFile(filename);
        imgOlah.setImageBitmap(citraolah);
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
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onClick(View v) {
        switch (jenisAlatUkur) {
            case "slm":
                data1hasilocr = etdata1.getText().toString();
                data2hasilocr = etdata2.getText().toString();
                break;
            case "co":
                data1hasilocr = etdata1.getText().toString();
                data2hasilocr = etdata2.getText().toString();
                break;
            default:
                data1hasilocr = etdata1.getText().toString();
                break;
        }
        if (datalat != null && datalong != null) {
             new MyAsyncTask().execute(data1hasilocr, datalat, datalong);
        }
    }

    public void gantiTipeMap(View v) {
        if(checkBoxSatelit.isChecked())
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public boolean kasihMarkerDanKoor () {
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (markerData == null) {
                markerData = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude,longitude))
                        .title("Location")
                        .draggable(true)
                        .snippet("Data"));
                datalat = Double.toString(latitude);
                datalong = Double.toString(longitude);
                etlat.setText(Double.toString(latitude));
                etlong.setText(Double.toString(longitude));
            }
            else if (markerData != null) {
                mMap.clear();
                markerData = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude,longitude))
                        .title("Location")
                        .draggable(true)
                        .snippet("Data"));
                datalat = Double.toString(latitude);
                datalong = Double.toString(longitude);
                etlat.setText(Double.toString(latitude));
                etlong.setText(Double.toString(longitude));
            }
        }
        else {
        }
        return false;
    }

    private class MyAsyncTask extends AsyncTask<String, Double, Double> {

        private String statusKirim;

        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
//            postData(data1);
            statusKirim = "Time out gan internetnya";
            String stringSQL;
            String tableId;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            SimpleDateFormat sdj = new SimpleDateFormat("HH:mm", Locale.US);
            String tanggal = sdf.format(new Date());
            String jam = sdj.format(new Date());

            stringSQL = kenaliTemplate.stringSQLFusionTable(jenisAlatUkur, data1hasilocr,
                    data2hasilocr, datalat, datalong, tanggal, jam, lamaProses, akurasiGPS);
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
            Toast.makeText(getApplicationContext(), statusKirim, Toast.LENGTH_SHORT).show();
        }
    }
}
