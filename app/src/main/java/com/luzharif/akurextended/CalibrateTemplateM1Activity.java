package com.luzharif.akurextended;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by LuZharif on 09/08/2015.
 */
public class CalibrateTemplateM1Activity extends Activity implements SeekBar.OnSeekBarChangeListener, CvCameraViewListener2 {

    static String TAG = "CalibrateActivity";

    //Deklarasi nilai
    public int threshhigh = 255;
    public int blocksize = 1;
    public int constantC = 1;

    SeekBar sbconstantc;
    SeekBar sbthreshhigh;
    SeekBar sbblocksize;
    TextView tvconstantc;
    TextView tvthreshhigh;
    TextView tvblocksize;
    Button btnTest;
    Button btnSave;

    SharedPreferences SPCal;
    SharedPreferences.Editor SPCaledit;

    private CameraBridgeViewBase mCameraCalibration;
    private Mat mCitraOlah;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraCalibration.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SPCal = getSharedPreferences("DataPreferenceCalibrate",0);
        SPCaledit = SPCal.edit();
        setContentView(R.layout.activity_calibratetemplatem);

//        prefscal = getPreferences(0);
//        dataPrefEdit = prefscal.edit();

        //Deklarasi widget
        sbconstantc = (SeekBar)findViewById(R.id.constantc);
        sbthreshhigh = (SeekBar)findViewById(R.id.thresholdhigh);
        sbblocksize = (SeekBar)findViewById(R.id.blocksize);

        tvconstantc = (TextView)findViewById(R.id.constantcview);
        tvthreshhigh = (TextView)findViewById(R.id.thresholdhighview);
        tvblocksize = (TextView)findViewById(R.id.blocksizeview);

        btnTest = (Button)findViewById(R.id.testcal);
        btnSave = (Button)findViewById(R.id.savecal);

        sbconstantc.setProgress(constantC);
        sbthreshhigh.setProgress(threshhigh);
        sbblocksize.setProgress(blocksize);

        sbconstantc.setOnSeekBarChangeListener(this);
        sbthreshhigh.setOnSeekBarChangeListener(this);
        sbblocksize.setOnSeekBarChangeListener(this);

        mCameraCalibration = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mCameraCalibration.setVisibility(SurfaceView.VISIBLE);
        mCameraCalibration.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mCameraCalibration != null)
            mCameraCalibration.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mCameraCalibration != null)
            mCameraCalibration.disableView();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.constantc:
                tvconstantc.setText(Integer.toString(progress));
                constantC = progress;
                //olahcitra();
                break;
            case R.id.thresholdhigh:
                tvthreshhigh.setText(Integer.toString(progress));
                threshhigh = progress;
                //olahcitra();
                break;
            case R.id.blocksize:
                tvblocksize.setText(Integer.toString((2*progress)+3));
                blocksize = progress;
                //olahcitra();
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onCameraViewStarted(int width, int height) {
        mCitraOlah = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mCitraOlah != null)
            mCitraOlah.release();

        mCitraOlah = null;
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();

        Imgproc.cvtColor(rgba, mCitraOlah, Imgproc.COLOR_BGRA2GRAY,4);
        Imgproc.adaptiveThreshold(mCitraOlah, rgba, threshhigh, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, (2*blocksize)+3, constantC+1);



        return rgba;
//        return inputFrame.rgba();
    }

    public void testCalTem(View v) {

    }

    public void saveCalTem(View v) {
        SPCaledit.putInt("constantc",constantC);
        SPCaledit.putInt("threshigh1",threshhigh);
        SPCaledit.putInt("blocksize",blocksize);
        SPCaledit.commit();
    }
}

