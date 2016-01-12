package com.luzharif.akurextended;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Vector;

/**
 * Created by LuZharif on 31/08/2015.
 */
public class UjiActivity extends AppCompatActivity {

    private Mat MatCitra;
    private ImageView ivUji;
    private ImageView ivOlah;
    private Button btnUji;
    private static int RESULT_LOAD_IMG = 1;
    private String folderDataTemplate = Environment.getExternalStorageDirectory().getPath() + "/MeterAkur/Data";

    int l;
    int t;
    int w;
    int h;

    Bitmap bmp;
    String imgDecodableString;
    private Mat MatCitra2;
    private String string1hasilOCR;
    private TextView teksHasil;
    private Mat MatCitraOlah;
    private Mat MatCitraS;
    private Mat MatCitraV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uji);
        btnUji = (Button) findViewById(R.id.button_ambilgambar);
        ivOlah = (ImageView) findViewById(R.id.olahgambar);
        ivUji = (ImageView) findViewById(R.id.cobaGambar);
        teksHasil = (TextView) findViewById(R.id.tekshasil);
    }

    public void CobaOlahCitra(View v) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
// Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                // Set the Image in ImageView after decoding the String

                bmp = BitmapFactory.decodeFile(imgDecodableString);
//                Utils.bitmapToMat(bmp, MatCitra);

//                prosesOlahCitra();

//                ivUji.setImageBitmap(BitmapFactory
//                        .decodeFile(imgDecodableString));

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
        prosesPotongDanOlah();
        //prosesOlahCitra();
    }

    private void prosesPotongDanOlah() {
        int colsCitra;
        int rowsCitra;
        int ratio;

        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY);
        colsCitra = mat.cols();
        rowsCitra = mat.rows();
        ratio = Math.round(colsCitra / rowsCitra);
        int cols = 400; //Edit dimari
        int rows = (int) Math.round(cols / ratio);
        Mat matResized = new Mat(rows, cols, CvType.CV_8UC1);
        Imgproc.resize(mat, matResized, matResized.size(), 0, 0, Imgproc.INTER_CUBIC);
        Mat matRotated = new Mat();
        Core.transpose(matResized, matRotated);
        Core.flip(matRotated, matRotated, 1);

        //Mulai olah
        Imgproc.adaptiveThreshold(matRotated, matRotated, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
                199, 0.0);

        //Pemotongan digit-digit citra pada layout CO
        Mat[] frameDigit = new Mat[6];
        Mat[] frameDigitF = new Mat[6];
        Size ukuranFrame = new Size(30,58); //TODO : Edit-edit di sini, cari yang optimal
        // Variasi adalah (30,58), (30,52), dan (30,39)
        //Ini layout yang baru
        for (int i = 0; i < 6; i++) {
            if (i < 3) {
                l = (int) ((0.235 + (0.182 * i)) * matRotated.cols());
                t = (int) (0.194 * matRotated.rows());
                w = (int) (0.182 * matRotated.cols());
                h = (int) (0.289 * matRotated.rows());
            } else if (i >= 3 && i < 5) {
                l = (int) ((0.355 + (0.152 * (i - 3))) * matRotated.cols());
                t = (int) (0.561 * matRotated.rows());
                w = (int) (0.152 * matRotated.cols());
                h = (int) (0.228 * matRotated.rows()); // awalnya 0.211
            } else if (i == 5) {
                l = (int) ((0.355 + (0.152 * (i - 3))) * matRotated.cols());
                t = (int) (0.625 * matRotated.rows());
                w = (int) (0.152 * matRotated.cols());
                h = (int) (0.164 * matRotated.rows());
            }
            Imgproc.rectangle(matRotated, new Point(l, t), new Point(l + w, t + h), new Scalar(0, 255, 255), +1);
            Rect kotakDigit = new Rect(l, t, w, h);
            frameDigit[i] = new Mat(matRotated, kotakDigit);
            Imgproc.resize(frameDigit[i], frameDigit[i], ukuranFrame, 0.0, 0.0, Imgproc.INTER_CUBIC); //Dulu Size(30,45) sekarang menyesuaikan
            Imgproc.equalizeHist(frameDigit[i], frameDigit[i]);
            frameDigitF[i] = new Mat();
            frameDigit[i].convertTo(frameDigitF[i], CvType.CV_32F);
        }

        Mat[] frameDigitTemplate = new Mat[22];
        Mat[] frameDigitTemplateF = new Mat[22];
        for (int d = 0; d <= 21; d++) {
            frameDigitTemplate[d] = Imgcodecs.imread(folderDataTemplate + "/" + d + ".jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Imgproc.equalizeHist(frameDigitTemplate[d], frameDigitTemplate[d]);
            frameDigitTemplateF[d] = new Mat();
            frameDigitTemplate[d].convertTo(frameDigitTemplateF[d], CvType.CV_32F);
        }
        Mat matKorelasi = new Mat();
        int[] korelasi = new int[6]; //Nilai tiap digit
        for (int i = 0; i < 6; i++) {
            double kmax = -1;
            for (int d = 0; d <= 21; d++) {
                Imgproc.matchTemplate(frameDigitF[i], frameDigitTemplateF[d], matKorelasi, Imgproc.TM_CCOEFF_NORMED);
                double k = matKorelasi.get(0, 0)[0];
                if (k > kmax) {
                    kmax = k;
                    if (d > 19)
                        korelasi[i] = d % d;
                    else
                        korelasi[i] = d % 10;
                }
            }
        }

        string1hasilOCR = Integer.toString(korelasi[0]) + Integer.toString(korelasi[1]) +
                Integer.toString(korelasi[2]) + ", " + Integer.toString(korelasi[3]) +
                Integer.toString(korelasi[4]) + "." + Integer.toString(korelasi[5]);
        teksHasil.setText(string1hasilOCR);
        Bitmap bmpKotak = Bitmap.createBitmap(MatCitra.width(), MatCitra.height(), Bitmap.Config.ARGB_8888);
        Bitmap bmpOlah = Bitmap.createBitmap(MatCitraOlah.width(), MatCitraOlah.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(MatCitra, bmpKotak);
        Utils.matToBitmap(MatCitraOlah, bmpOlah);
        ivUji.setImageBitmap(bmpKotak);
        ivOlah.setImageBitmap(bmpOlah);

    }

    public void prosesOlahCitra() {
        MatCitra = new Mat(); //Buat ubah ke Hitam putih
        MatCitra2 = new Mat(); //Buat ubah ke HSV
        MatCitraS = new Mat();
        MatCitraV = new Mat();
        MatCitraOlah = new Mat(); //Buat ubah ke channel S saja

        Utils.bitmapToMat(bmp, MatCitra);
        Utils.bitmapToMat(bmp, MatCitra2);
        Utils.bitmapToMat(bmp, MatCitraOlah);

        //Coba dengan HSV
        Imgproc.cvtColor(MatCitra2, MatCitra2, Imgproc.COLOR_BGR2HSV);
        Vector<Mat> CitraSplit = new Vector<>(3);
        Core.split(MatCitra2, CitraSplit);
        MatCitraS = CitraSplit.get(1);
        MatCitraV = CitraSplit.get(2);
        MatCitraOlah = MatCitraS;
        //Core.subtract(MatCitraV,MatCitraS, MatCitraOlah);

        Imgproc.cvtColor(MatCitra2, MatCitra2, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(MatCitra2, MatCitra2, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
                199, 0.0); //Blocksize = 2*(bsval)+3, C = ccval
        Imgproc.adaptiveThreshold(MatCitraOlah, MatCitraOlah, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 499, 0.0);

//        Core.bitwise_not(MatCitraOlah,MatCitraOlah);

        int cols = MatCitraOlah.cols();
        int rows = MatCitraOlah.rows();
        int[] jumlahKolom = new int[cols];
        int[] jumlahBaris = new int[rows];

        for (int kolom = 0; kolom < cols; kolom++) {
            for (int baris = 0; baris < rows; baris++) {
                if (MatCitraOlah.get(baris, kolom)[0] > 0)
                    jumlahKolom[kolom]++;
                if (MatCitraOlah.get(baris, kolom)[0] > 0)
                    jumlahBaris[baris]++;
            }
        }
        int kiri = 0, kanan = 1000, atas = 0, bawah = 1000;
        for (int kolom = cols / 2; kolom >= 0; kolom--)
            if (jumlahKolom[kolom] > rows * 0.7) {
                kiri = kolom + 1;
                break;
            }
        for (int kolom = cols / 2; kolom < cols; kolom++)
            if (jumlahKolom[kolom] > rows * 0.7) {
                kanan = kolom - 1;
                break;
            }
        for (int baris = rows / 2; baris >= 0; baris--)
            if (jumlahBaris[baris] > cols * 0.7) {
                atas = baris + 1;
                break;
            }
        for (int baris = rows / 2; baris < rows; baris++)
            if (jumlahBaris[baris] > cols * 0.7) {
                bawah = baris - 1;
                break;
            }
        Rect kotak = new Rect(kiri, atas, kanan - kiri + 1, bawah - atas + 1);
        Mat frameKotak = new Mat(MatCitra2, kotak);
        Mat[] frameDigit = new Mat[6];
        Mat[] frameDigitF = new Mat[6];

        //Ini layout yang baru
        for (int i = 0; i < 6; i++) {
            if (i < 3) {
                l = (int) ((0.235 + (0.182 * i)) * kotak.width);
                t = (int) (0.194 * kotak.height);
                w = (int) (0.182 * kotak.width);
                h = (int) (0.289 * kotak.height);
            } else if (i >= 3 && i < 5) {
                l = (int) ((0.355 + (0.152 * (i - 3))) * kotak.width);
                t = (int) (0.561 * kotak.height);
                w = (int) (0.152 * kotak.width);
                h = (int) (0.211 * kotak.height);
            } else if (i == 5) {
                l = (int) ((0.355 + (0.152 * (i - 3))) * kotak.width);
                t = (int) (0.625 * kotak.height);
                w = (int) (0.152 * kotak.width);
                h = (int) (0.164 * kotak.height);
            }
            Imgproc.rectangle(MatCitra, new Point(l, t), new Point(l + w, t + h), new Scalar(0, 255, 255), +1);
            Rect kotakDigit = new Rect(l, t, w, h);
            frameDigit[i] = new Mat(frameKotak, kotakDigit);
            Imgproc.resize(frameDigit[i], frameDigit[i], new Size(30, 45), 0.0, 0.0, Imgproc.INTER_CUBIC);
            Imgproc.equalizeHist(frameDigit[i], frameDigit[i]);
            frameDigitF[i] = new Mat();
            frameDigit[i].convertTo(frameDigitF[i], CvType.CV_32F);


            //Ini contoh sebelumnya
//        Imgproc.cvtColor(MatCitra,MatCitra,Imgproc.COLOR_BGR2GRAY);
//        Imgproc.adaptiveThreshold(MatCitra, MatCitra, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
//                199, 0.0); //Blocksize = 2*(bsval)+3, C = ccval

//        //Layout untuk CO Meter
//        Mat[] frameDigit = new Mat[6];
//        Mat[] frameDigitF = new Mat[6];
//
//        //Ini layout yang baru
//        for (int i = 0; i < 6; i++) {
//            if (i < 3) {
//                l = (int) ((0.235 + (0.182 * i)) * MatCitra.cols());
//                t = (int) (0.194 * MatCitra.rows());
//                w = (int) (0.182 * MatCitra.cols());
//                h = (int) (0.289 * MatCitra.rows());
//            } else if (i >= 3 && i < 5) {
//                l = (int) ((0.355 + (0.152 * (i - 3))) * MatCitra.cols());
//                t = (int) (0.561 * MatCitra.rows());
//                w = (int) (0.152 * MatCitra.cols());
//                h = (int) (0.211 * MatCitra.rows());
//            } else if (i == 5) {
//                l = (int) ((0.355 + (0.152 * (i - 3))) * MatCitra.cols());
//                t = (int) (0.625 * MatCitra.rows());
//                w = (int) (0.152 * MatCitra.cols());
//                h = (int) (0.164 * MatCitra.rows());
//            }
//            Imgproc.rectangle(MatCitra, new Point(l, t), new Point(l + w, t + h), new Scalar(0, 255, 255), +1);
//            Rect kotakDigit = new Rect(l, t, w, h);
//            frameDigit[i] = new Mat(MatCitra, kotakDigit);
//            Imgproc.resize(frameDigit[i], frameDigit[i], new Size(30, 45), 0.0, 0.0, Imgproc.INTER_CUBIC);
//            Imgproc.equalizeHist(frameDigit[i], frameDigit[i]);
//            frameDigitF[i] = new Mat();
//            frameDigit[i].convertTo(frameDigitF[i], CvType.CV_32F);


            //Ini layout yang lama, mboh bener atau gak
//            for (int i = 0; i < 6; i++) {
//                if (i < 3) {
//                    l = (int) (0.065 + 0.19 * i) * kotakPPM.width;
//                    t = (int) (0.3 * kotakPPM.height);
//                    w = (int) (0.11 * kotakPPM.width);
//                    h = (int) (kotakPPM.height);
//                }
//                else if (i >= 3 && i < 5) {
//                    l = (int) (0.065 + 0.19 * i) * kotakSuhu.width;
//                    t = (int) (0.3 * kotakSuhu.height);
//                    w = (int) (0.11 * kotakSuhu.width);
//                    h = (int) (kotakSuhu.height);
//                }
//                else if (i == 5) {
//                    l = (int) (0.065 + 0.19 * i) * kotakSuhu.width;
//                    t = (int) (0.3 * kotakSuhu.height);
//                    w = (int) (0.11 * kotakSuhu.width);
//                    h = (int) (kotakSuhu.height);
//                }
        }

        Mat[] frameDigitTemplate = new Mat[22];
        Mat[] frameDigitTemplateF = new Mat[22];
        for (int d = 0; d <= 21; d++) {
            frameDigitTemplate[d] = Imgcodecs.imread(folderDataTemplate + "/" + d + ".jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Imgproc.equalizeHist(frameDigitTemplate[d], frameDigitTemplate[d]);
            frameDigitTemplateF[d] = new Mat();
            frameDigitTemplate[d].convertTo(frameDigitTemplateF[d], CvType.CV_32F);
        }
        Mat matKorelasi = new Mat();
        int[] korelasi = new int[6];
        for (int i = 0; i < 6; i++) {
            double kmax = -1;
            for (int d = 0; d <= 21; d++) {
                Imgproc.matchTemplate(frameDigitF[i], frameDigitTemplateF[d], matKorelasi, Imgproc.TM_CCOEFF_NORMED);
                double k = matKorelasi.get(0, 0)[0];
                if (k > kmax) {
                    kmax = k;
                    if (d > 19)
                        korelasi[i] = d % d;
                    else
                        korelasi[i] = d % 10;
                }
            }
        }
        string1hasilOCR = Integer.toString(korelasi[0]) + Integer.toString(korelasi[1]) +
                Integer.toString(korelasi[2]) + ", " + Integer.toString(korelasi[3]) +
                Integer.toString(korelasi[4]) + "." + Integer.toString(korelasi[5]);
        teksHasil.setText(string1hasilOCR);
        Bitmap bmpKotak = Bitmap.createBitmap(MatCitra.width(), MatCitra.height(), Bitmap.Config.ARGB_8888);
        Bitmap bmpOlah = Bitmap.createBitmap(MatCitraOlah.width(), MatCitraOlah.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(MatCitra, bmpKotak);
        Utils.matToBitmap(MatCitraOlah, bmpOlah);
        ivUji.setImageBitmap(bmpKotak);
        ivOlah.setImageBitmap(bmpOlah);
    }
}

