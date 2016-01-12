package com.luzharif.akurextended;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Camera.AutoFocusCallback,
        Camera.PictureCallback, View.OnClickListener {

    public List<Camera.Size> sizes;
    private List<String> flashModes;
    public List<String> entries; //TODO Cari cara buat taruh char ke Array
    public List<String> entryValues;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("OpenCV Legit", "OpenCV nya gak kemuat je");
        } else {
            Log.i("OpenCV Legit", "OpenCV kemuat dab :D");
        }
    }

    private static final String TAG = "MainActivity";

    //Constant untuk OAUTH 2.0
    private static String CLIENT_ID = "838514003249-4upok8peqlsuqvg3tg7ec67utr2da078.apps.googleusercontent.com";
    //Use your own client id
    private static String CLIENT_SECRET = "k7x9BuOxt-dMAkCaA3lR3M11";
    //Use your own client secret
    private static String REDIRECT_URI = "http://localhost";
    private static String GRANT_TYPE = "authorization_code";
    private static String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    private static String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static String OAUTH_SCOPE = "https://www.googleapis.com/auth/fusiontables";

    private static final int MAPS_ID = 1;
    private static final int AUTH_IT = 2;
    private static final int SETTINGS_ID = 3;
    private static final int ABOUT_ID = 4;

    //    private TessOCR mTessOCR;
    String string1hasilOCR;
    String string2hasilOCR;
    double hasilOCR;

    String SPMetode;
    int Metode;

    int mFrameWidth;
    int mFrameHeight;
    int nilaiukuran;
    String ukuran;
    String tipeFlash;
    boolean lampuFlash;

    int l;
    int t;
    int w;
    int h;
    Rect kotak;

    SharedPreferences.Editor dataPrefEdit;
    SharedPreferences dataPref;
    Dialog auth_dialog;
    WebView web;
    private String accessToken;
    private String refreshToken;

    private int idCamera;
    private Camera mCamera;
    private CameraPreview mPreview;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    //    private ImageView iv;
    private View v;
    FloatingActionButton fabTangkapCitra;
    private String folderCitra = Environment.getExternalStorageDirectory().getPath() +
            "/MeterAkur";
    private String folderDataTemplate = Environment.getExternalStorageDirectory().getPath() +
            "/MeterAkur/Data";
    private int lebarPreview;
    private int tinggiPreview;
    private int statusSettingKamera;
    private String jenisAlat;

    KenaliTemplate kenaliTemplate;
    private int ambangBatasPutih;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataPref = getSharedPreferences("DataPreference", 0);
        dataPrefEdit = dataPref.edit();
        kenaliTemplate = new KenaliTemplate();
//        mTessOCR = new TessOCR();
        mCamera = ambilInstanceKamera();
        setCameraDisplayOrientation();
//        nilaiukuran = 0;
//        string1hasilOCR = "0";

        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
//        iv = (ImageView) this.findViewById(R.id.imageViewBar);
        v = this.findViewById(R.id.viewKotak);
        fabTangkapCitra = (FloatingActionButton) findViewById(R.id.fab);
        fabTangkapCitra.setOnClickListener(this);

        restoreData();
        entries = new ArrayList<String>();
        entryValues = new ArrayList<String>();
        try {
            checkDataTemplate();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (accessToken.equals("kosong"))
            AuthIt();
    }

    public void restoreData() {
        double tinggiViewPotong, lebarViewPotong;
        SharedPreferences sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(getBaseContext());
        dataPref = getSharedPreferences("DataPreference", 0);
        SPMetode = sharedPreferences.getString("metodeolahcitra", "1");
        jenisAlat = sharedPreferences.getString("jenisalatukur", "slm");
        ukuran = sharedPreferences.getString("ukurancitra", "3");
        tipeFlash = sharedPreferences.getString("tipeflash", "0");
        lampuFlash = sharedPreferences.getBoolean("modeflash", true);
        ambangBatasPutih = Integer.parseInt(sharedPreferences.
                getString("nilaiambangbatasputih", "748"));


        //Atur Template:
        //Formatnya masih piksel
        tinggiViewPotong = kenaliTemplate.aturTinggiTemplate(jenisAlat);
        lebarViewPotong = kenaliTemplate.aturLebarTemplate(jenisAlat);

        //Diubah jadi dp
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) tinggiViewPotong, getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) lebarViewPotong, getResources().getDisplayMetrics());

        //Ubah ukuran
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        v.setLayoutParams(layoutParams);

//        v.setMinimumHeight((int) tinggiViewPotong);
//        v.setMinimumWidth((int) lebarViewPotong);

        Metode = Integer.parseInt(SPMetode);
        accessToken = dataPref.getString("accessToken", "kosong");
        refreshToken = dataPref.getString("refreshToken", "kosong");
    }

    public void saveData() {
        dataPrefEdit = dataPref.edit();
        dataPrefEdit.putInt("dataolah1", 0);
        dataPrefEdit.putString("data1hasilOCR", string1hasilOCR);
        dataPrefEdit.putString("data2hasilOCR", string2hasilOCR);
    }

    public void checkDataTemplate() throws IOException {
        FileOutputStream outStream = null;
        File file = new File(folderDataTemplate);
        if (!file.exists()) {
            if (!file.mkdirs())
                Log.e(TAG, "Gagal membuat folder");
            for (int d = 0; d <= 21; d++) {
                String legit = "t" + d + ".jpg";
                Bitmap citraTemplate = BitmapFactory.decodeStream(getAssets().open(legit));
                file = new File(folderDataTemplate, "t" + d + ".jpg");
                outStream = new FileOutputStream(file);
                citraTemplate.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            }
            for (int d = 0; d <= 21; d++) {
                String legit = "s" + d + ".jpg";
                Bitmap citraTemplate = BitmapFactory.decodeStream(getAssets().open(legit));
                file = new File(folderDataTemplate, "s" + d + ".jpg");
                outStream = new FileOutputStream(file);
                citraTemplate.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            }
        }
    }

    /**
     * A basic Camera preview class
     */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        public Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // supported preview sizes
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            for (Camera.Size str : mSupportedPreviewSizes)
                Log.e(TAG, str.width + "/" + str.height);
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            if (mHolder.getSurface() == null) {
                return;
            }
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
            }
            lebarPreview = w;
            tinggiPreview = h;

            // start preview with new settings
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                sizes = parameters.getSupportedPictureSizes();
                flashModes = parameters.getSupportedFlashModes();
                if (flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    if (lampuFlash)
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    else
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                int ukuranCitra = Integer.parseInt(ukuran);
                mFrameWidth = sizes.get(ukuranCitra).width;
                mFrameHeight = sizes.get(ukuranCitra).height;
                parameters.setPictureSize(mFrameWidth, mFrameHeight);
                mCamera.setParameters(parameters);
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

                Toast toast = Toast.makeText(getApplicationContext(), mFrameWidth +
                        "x" + mFrameHeight, Toast.LENGTH_SHORT);
                toast.show();

            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }

            float ratio;
            if (mPreviewSize.height >= mPreviewSize.width)
                ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
            else
                ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

            // One of these methods should be used, second method squishes preview slightly
            setMeasuredDimension(width, (int) (width * ratio));
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.1;
            double targetRatio = (double) h / w;

            if (sizes == null)
                return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            int targetHeight = h;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.height / size.width;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;

                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }

            return optimalSize;
        }
    }

    /**
     * Cek apakah gadget ini memiliki kamera
     */
    public boolean cekHardwareKamera() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // gadget ini memiliki kamera
            Log.d(TAG, "Gadget memiliki kamera");
            return true;
        } else {
            // gadget ini tidak memiliki kamera
            // tampilkan tulisan splash
            Toast toast = Toast.makeText(getApplicationContext(), "Gadget tidak memiliki kamera",
                    Toast.LENGTH_LONG);
            toast.show();
            // keluar dari aplikasi
            finish();
            return false;
        }
    }

    public void ambilIdKameraBelakang() {
        if (cekHardwareKamera()) {
            int BanyaknyaKamera = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < BanyaknyaKamera; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    idCamera = i;
                    Log.d(TAG, "Id kamera belakang: " + idCamera);
                }
            }
            if (BanyaknyaKamera == 0) {
                Toast toast = Toast.makeText(getApplicationContext(), "Tidak ada kamera belakang",
                        Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        }
    }

    /**
     * Ambil instance kamera belakang
     */
    public Camera ambilInstanceKamera() {
        Camera c = null;
        try {
            ambilIdKameraBelakang();
            c = Camera.open(idCamera); // attempt to get a Camera instance
        } catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Kamera belakang sedang digunakan aplikasi lain", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        return c; // returns null if camera is unavailable
    }

    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(idCamera, info);
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }


    @Override
    public void onClick(View v) {
        mCamera.autoFocus(this);
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        long startTime = System.nanoTime();
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY); //Edit di sini
        double ratiow = (double) mat.cols() / (double) tinggiPreview;
        double ratioh = (double) mat.rows() / (double) lebarPreview;
        l = (int) Math.round(v.getTop() * ratiow);
        t = (int) Math.round(v.getLeft() * ratioh);
        w = (int) Math.round(v.getHeight() * ratiow);
        h = (int) Math.round(v.getWidth() * ratioh);
        kotak = new Rect(l, t, w, h);
        Mat matCropped = new Mat(mat, kotak);
        int cols = (int) Math.round(700.0 / h * w); //Edit dimari
        Mat matResized = new Mat(700, cols, CvType.CV_8UC1);
        Imgproc.resize(matCropped, matResized, matResized.size(), 0, 0, Imgproc.INTER_CUBIC);
//        Mat matRotatedSebelumFlip = new Mat();
        Mat matRotated = new Mat();
        Core.transpose(matResized, matRotated);
        Core.flip(matRotated, matRotated, 1);
//        Imgproc.equalizeHist(matRotated, matRotated);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String waktu = sdf.format(new Date());
        Log.d(TAG, waktu);

        String filename = folderCitra + "/" + waktu + ".jpg";
        if (Imgcodecs.imwrite(folderCitra + "/" + waktu + ".jpg", matRotated))
            Log.d(TAG, "Bitmap saved");
        Object[] hasilDeteksi = (Object[]) kenaliTemplate.olahDeteksiCitra(matRotated,
                jenisAlat, folderDataTemplate, ambangBatasPutih);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        Toast.makeText(getApplicationContext(), "Waktu Proses: " + duration,
                Toast.LENGTH_SHORT).show();
        string1hasilOCR = (String) hasilDeteksi[0];
        string2hasilOCR = (String) hasilDeteksi[1];
        saveData();

        Bundle info = new Bundle();
        info.putString("filename", filename);
        info.putString("string1hasilocr", string1hasilOCR);
        info.putString("string2hasilocr", string2hasilOCR);
        info.putLong("lamaproses", duration);
        Intent editdata = new Intent(this, EditData.class);
        editdata.putExtras(info);

        startActivity(editdata);
        Log.d(TAG, "Get bitmap: " + matRotated.cols() + "x" + matRotated.rows());
        Log.d(TAG, lebarPreview + "x" + tinggiPreview);
        Log.d(TAG, mat.cols() + "x" + mat.rows());
        Log.d(TAG, getWindowManager().getDefaultDisplay().getWidth() + "x" +
                getWindowManager().getDefaultDisplay().getHeight());
        camera.startPreview();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, MAPS_ID, Menu.NONE, "Uji Kirim");
        menu.add(2, AUTH_IT, Menu.NONE, "Otorisasi");
        menu.add(3, SETTINGS_ID, Menu.NONE, "Pengaturan");
        menu.add(Menu.NONE, ABOUT_ID, Menu.NONE, "Tentang");
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SETTINGS_ID:
                if (nilaiukuran == 0) {
                    for (Camera.Size size : sizes) {
                        entries.add(String.format("%d x %d", size.width, size.height));
                        entryValues.add(String.format("%d", nilaiukuran));
                        nilaiukuran++;
                    }
                }

                Bundle cameraSpec = new Bundle();
                cameraSpec.putStringArrayList("entries", (ArrayList<String>) entries);
                cameraSpec.putStringArrayList("entryValues", (ArrayList<String>) entryValues);

                //Memberi tanda bahwa sudah pernah deteksi ukuran kamera
                dataPrefEdit.putInt("statusSettingKamera", 1);
                dataPrefEdit.apply();

                Intent settings = new Intent(this, SettingsActivity.class);
                settings.putExtras(cameraSpec);
                startActivity(settings);
                return (true);
            case MAPS_ID:
                startActivity(new Intent(this, MapsActivity.class));
                return (true);
            case AUTH_IT:
                AuthIt();
                return (true);
            case ABOUT_ID:
                AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(this);
                TextView aboutText = new TextView(this);
                aboutText.setText("Program Akur (Alat Ukur) Extended \n" +
                        "Dibuat oleh :\n" + "Luthfi Zharif (11/319494/TK/38622) \n" +
                        "Diciptakan sebagai langkah kontribusi kepada umat manusia pada umumnya, " +
                        "dan untuk memenuhi skripsi khususnya :3");
                aboutText.setGravity(Gravity.CENTER_HORIZONTAL);
                aboutBuilder.setTitle("Tentang Program Ini");
                aboutBuilder.setView(aboutText);
                aboutBuilder.setPositiveButton("Ok", null);

                //Tunjukkan dialog
                AlertDialog helpDialog = aboutBuilder.show();
                helpDialog.show();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    private void AuthIt() {
        final Dialog auth_dialog = new Dialog(MainActivity.this);
        auth_dialog.setContentView(R.layout.auth_dialog);
        WebView web = (WebView) auth_dialog.findViewById(R.id.webv);
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl(OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id="
                + CLIENT_ID + "&scope=" + OAUTH_SCOPE);
        web.setWebViewClient(new WebViewClient() {
            boolean authComplete = false;
            Intent resultIntent = new Intent();

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            String authCode;

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    authComplete = true;
                    resultIntent.putExtra("code", authCode);
                    MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);

                    dataPrefEdit.putString("Code", authCode);
                    dataPrefEdit.commit();
                    auth_dialog.dismiss();
                    new TokenGet().execute();
                    Toast.makeText(getApplicationContext(), "Authorization Code is: " +
                            authCode, Toast.LENGTH_SHORT).show();
                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured",
                            Toast.LENGTH_SHORT).show();
                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize Fusion Tables");
        auth_dialog.setCancelable(true);
    }

    public void onResume() {
        super.onResume();
        restoreData();
        mCamera.startPreview();
    }


    private class TokenGet extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;
        String Code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting Google ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = dataPref.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetAccessToken jParser = new GetAccessToken();
            JSONObject json = jParser.gettoken(TOKEN_URL, Code, CLIENT_ID, CLIENT_SECRET,
                    REDIRECT_URI, GRANT_TYPE);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null) {
                try {
                    accessToken = json.getString("access_token");
                    refreshToken = json.getString("refresh_token");
                    dataPrefEdit = dataPref.edit();
                    dataPrefEdit.putString("accessToken", accessToken);
                    dataPrefEdit.putString("refreshToken", refreshToken);
                    dataPrefEdit.commit();
                    Toast.makeText(getApplicationContext(), "Berhasil", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }
}