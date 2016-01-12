package com.luzharif.akurextended;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by LuZharif on 21/10/2015.
 */
public class KenaliTemplate {

    public double aturTinggiTemplate(String jenisAlatUkur) {
        double tinggiView = 0.00;
        switch (jenisAlatUkur) {
            case "co":
                tinggiView = 250;
                break;
            case "slm":
                tinggiView = 175; //Awalnya 225
                break;
            default:
                tinggiView = 225;
        }
        return tinggiView;
    }

    public double aturLebarTemplate(String jenisAlatUkur) {
        double lebarView = 0.00;
        switch (jenisAlatUkur) {
            case "co":
                lebarView = 206.25;
                break;
            case "slm":
                lebarView = 204.87;  //Awalnya 263.41
                break;
            default:
                lebarView = 263.41;
        }
        return lebarView;
    }

    public Object olahDeteksiCitra(Mat CitraPotong, String jenisAlatUkur,
                                   String folderDataTemplate, int ambangBatasPutih) {
        int l = 0, t = 0, w = 0, h = 0;
        int[] korelasi;
        Mat[] frameDigit, frameDigitF, frameDigitTemplate, frameDigitTemplateF;
        Mat CitraOlah, matKorelasi;
        Size ukuranFrame, ukuranFrameKecil;
        String string1hasilOCR = "", string2hasilOCR = "";
        switch (jenisAlatUkur) {
            case "co":
                Imgproc.adaptiveThreshold(CitraPotong, CitraPotong, 255.0,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,
                        199, 0.0);
                frameDigit = new Mat[6];
                frameDigitF = new Mat[6];
                frameDigitTemplate = new Mat[22];
                frameDigitTemplateF = new Mat[22];
                ukuranFrame = new Size(30, 58);
                matKorelasi = new Mat();
                korelasi = new int[6];
                for (int i = 0; i < 6; i++) {
                    //Ini layout yang baru
                    if (i < 3) {
                        l = (int) ((0.235 + (0.182 * i)) * CitraPotong.cols());
                        t = (int) (0.194 * CitraPotong.rows());
                        w = (int) (0.182 * CitraPotong.cols());
                        h = (int) (0.289 * CitraPotong.rows());
                    } else if (i >= 3 && i < 5) {
                        l = (int) ((0.355 + (0.152 * (i - 3))) * CitraPotong.cols());
                        t = (int) (0.561 * CitraPotong.rows());
                        w = (int) (0.152 * CitraPotong.cols());
                        h = (int) (0.228 * CitraPotong.rows()); // awalnya 0.211
                    } else if (i == 5) {
                        l = (int) ((0.355 + (0.152 * (i - 3))) * CitraPotong.cols());
                        t = (int) (0.625 * CitraPotong.rows());
                        w = (int) (0.152 * CitraPotong.cols());
                        h = (int) (0.164 * CitraPotong.rows());
                    }
                    Imgproc.rectangle(CitraPotong, new Point(l, t), new Point(l + w, t + h),
                            new Scalar(0, 255, 255), +1);
                    Rect kotakDigit = new Rect(l, t, w, h);
                    frameDigit[i] = new Mat(CitraPotong, kotakDigit);
                    Imgproc.resize(frameDigit[i], frameDigit[i], ukuranFrame, 0.0, 0.0,
                            Imgproc.INTER_CUBIC); //Dulu Size(30,45) sekarang menyesuaikan
                    Imgproc.equalizeHist(frameDigit[i], frameDigit[i]);
                    frameDigitF[i] = new Mat();
                    frameDigit[i].convertTo(frameDigitF[i], CvType.CV_32F);
                }

                for (int d = 0; d <= 21; d++) {
                    frameDigitTemplate[d] = Imgcodecs.imread(folderDataTemplate + "/" + d +
                            ".jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                    Imgproc.equalizeHist(frameDigitTemplate[d], frameDigitTemplate[d]);
                    frameDigitTemplateF[d] = new Mat();
                    frameDigitTemplate[d].convertTo(frameDigitTemplateF[d], CvType.CV_32F);
                }

                for (int i = 0; i < 6; i++) {
                    double kmax = -1;
                    for (int d = 0; d <= 21; d++) {
                        Imgproc.matchTemplate(frameDigitF[i], frameDigitTemplateF[d],
                                matKorelasi, Imgproc.TM_CCOEFF_NORMED);
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
                        Integer.toString(korelasi[2]);
                string2hasilOCR = Integer.toString(korelasi[3]) + Integer.toString(korelasi[4]) +
                        "." + Integer.toString(korelasi[5]);

                break;

            case "slm":
                frameDigit = new Mat[4];
                frameDigitF = new Mat[4];
                frameDigitTemplate = new Mat[22];
                frameDigitTemplateF = new Mat[22];
                ukuranFrame = new Size(30, 71);
                ukuranFrameKecil = new Size(12, 71);
                CitraOlah = new Mat();
                matKorelasi = new Mat();
                korelasi = new int[4];

                //Ambil citra template
                for (int d = 0; d <= 19; d++) {
                    frameDigitTemplate[d] = Imgcodecs.imread(folderDataTemplate + "/s" + d +
                            ".jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                    Imgproc.equalizeHist(frameDigitTemplate[d], frameDigitTemplate[d]);
                    frameDigitTemplateF[d] = new Mat();
                    frameDigitTemplate[d].convertTo(frameDigitTemplateF[d], CvType.CV_32F);
                }

                for (int i = 0; i < 4; i++) {
                    if (i == 0) {
                        l = (int) (0.153 * CitraPotong.cols());
                        t = (int) (0.190 * CitraPotong.rows());
                        w = (int) (0.085 * CitraPotong.cols());
                        h = (int) (0.490 * CitraPotong.rows());
                    } else if (i == 1) {
                        l = (int) (0.257 * CitraPotong.cols());
                        t = (int) (0.190 * CitraPotong.rows());
                        w = (int) (0.178 * CitraPotong.cols());
                        h = (int) (0.490 * CitraPotong.rows());
                    } else if (i == 2) {
                        l = (int) (0.458 * CitraPotong.cols());
                        t = (int) (0.190 * CitraPotong.rows());
                        w = (int) (0.178 * CitraPotong.cols());
                        h = (int) (0.490 * CitraPotong.rows());
                    } else if (i == 3) {
                        l = (int) (0.670 * CitraPotong.cols());
                        t = (int) (0.190 * CitraPotong.rows());
                        w = (int) (0.178 * CitraPotong.cols());
                        h = (int) (0.490 * CitraPotong.rows());
                    }
                    Imgproc.equalizeHist(CitraPotong,CitraPotong);
                    Imgproc.adaptiveThreshold(CitraOlah,CitraOlah, 255.0,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 499, 0.0);
                    Rect kotakDigit = new Rect(l, t, w, h);
                    frameDigit[i] = new Mat(CitraPotong, kotakDigit);

                    if (i == 0)
                        Imgproc.resize(frameDigit[i], frameDigit[i], ukuranFrameKecil, 0.0, 0.0,
                                Imgproc.INTER_CUBIC); //Untuk digit 1
                    else {
                        Imgproc.resize(frameDigit[i], frameDigit[i], ukuranFrame, 0.0, 0.0,
                                Imgproc.INTER_CUBIC); //Untuk digit 2-4
                    }
//                    Imgproc.equalizeHist(frameDigit[i], frameDigit[i]);
                    frameDigitF[i] = new Mat();
                    frameDigit[i].convertTo(frameDigitF[i], CvType.CV_32F);
                }

                //Lakukan proses pengenalan teks
                for (int i = 0; i < 4; i++) {
                    double kmax = -1;
                    if (i == 0) {
                        int pikselPutih = Core.countNonZero(frameDigit[i]);
                        if (pikselPutih < ambangBatasPutih)
                            korelasi[i] = 1;
                        else
                            korelasi[i] = 0;
                    }
                    else {
                        for (int d = 0; d <= 19; d++) {
                            Imgproc.matchTemplate(frameDigitF[i], frameDigitTemplateF[d],
                                    matKorelasi, Imgproc.TM_CCOEFF_NORMED);
                            double k = matKorelasi.get(0, 0)[0];
                            if (k > kmax) {
                                kmax = k;
                                korelasi[i] = d % 10;
                            }
                        }
                    }
                }
                String digit1;
                if (korelasi[0] == 0)
                    digit1 = "";
                else
                    digit1 = "1";
                string1hasilOCR = digit1 + Integer.toString(korelasi[1]) +
                        Integer.toString(korelasi[2]) + "." + Integer.toString(korelasi[3]);
        }
        return new Object[]{string1hasilOCR, string2hasilOCR};
    }

    public String stringSQLFusionTable(String jenisAlatUkur, String dataHasilOCR1,
                                       String dataHasilOCR2,String dataLat, String dataLong,
                                       String tanggal, String jam, long lamaProses,
                                       float akurasiGPS) {
        String stringSQL = "";
        String tableId = "";
        switch (jenisAlatUkur) {
            case "co":
                tableId = "107twe1B43zY6TyNDo_jP_ygq4PEkBWb7hSva4wTo";
                stringSQL = "INSERT INTO " + tableId + " (CO, Suhu, Lokasi, Tanggal, Jam, AkurasiGPS) "
                        + "VALUES ('" + dataHasilOCR1 + "', '" + dataHasilOCR2 + "', '" +
                        dataLat + ", " + dataLong + "', '" + tanggal + "', '" + jam +
                        "', '" + akurasiGPS + "')";
                break;
            case "slm":
                tableId = "1bG3l1PmAe9WxZuOcpmp3tu_Y9zGmKgWn_tQsnEGY";
                stringSQL = "INSERT INTO " + tableId +
                        " (Desibel, DesibelAsli, Lokasi, Tanggal, Jam, LamaProses, AkurasiGPS) "
                        + "VALUES ('" + dataHasilOCR1 + "', '" + dataHasilOCR2 +
                        "', '" + dataLat + ", " + dataLong  + "', '" + tanggal + "', '" + jam +
                        "', '" + lamaProses + "', '" + akurasiGPS + "')";
                break;
            default:
                tableId = "1bG3l1PmAe9WxZuOcpmp3tu_Y9zGmKgWn_tQsnEGY";
                stringSQL = "INSERT INTO " + tableId + " (Desibel, Lokasi, Tanggal, Jam) "
                        + "VALUES ('" + dataHasilOCR1 + "', '" + dataLat + ", " + dataLong
                        + "', '" + tanggal + "', '" + jam + "')";
        }
        return stringSQL;
    }
}