import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/*
 * code from: https://github.com/IsaacChanghau/OptimizedImageEnhance/blob/master/src/main/java/com/isaac/estimate/TransmissionEstimate.java
 */

public class TransmissionEstimate {

  public static Mat transEstimate(Mat img, int patchSz, double[] airLight, double lambda, double fTrans) {
    int rows = img.rows();
    int cols = img.cols();
    List<Mat> bgr = new ArrayList<>();
    Core.split(img, bgr);
    int type = bgr.getFirst().type();
    // calculate the transmission map
    return computeTrans(img, patchSz, rows, cols, type, airLight, lambda, fTrans);
  }

  public static Mat transEstimateEachChannel(Mat img, int patchSz, double airLight, double lambda, double fTrans) {
    int rows = img.rows();
    int cols = img.cols();
    Mat T = new Mat(rows, cols, img.type());
    for (int i = 0; i < rows; i += patchSz) {
      for (int j = 0; j < cols; j += patchSz) {
        int endRow = Math.min(i + patchSz, rows);
        int endCol = Math.min(j + patchSz, cols);
        Mat blkIm = img.submat(i, endRow, j, endCol);
        double Trans = BlkTransEstimate.blkEstimateEachChannel(blkIm, airLight, lambda, fTrans);
        for (int m = i; m < endRow; m++)
          for (int n = j; n < endCol; n++)
            T.put(m, n, Trans);
      }
    }
    return T;
  }

  public static Mat transEstimate(Mat img, int patchSz, double[] airLight, double lambda, double fTrans,
      int r, double eps, double gamma) {
    int rows = img.rows();
    int cols = img.cols();
    List<Mat> bgr = new ArrayList<>();
    Core.split(img, bgr);
    int type = bgr.getFirst().type();
    // calculate the transmission map
    Mat T = computeTrans(img, patchSz, rows, cols, type, airLight, lambda, fTrans);
    // refine the transmission map
    img.convertTo(img, CvType.CV_8UC1);
    Mat gray = new Mat();
    Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
    gray.convertTo(gray, CvType.CV_32F);
    Core.divide(gray, new Scalar(255.0), gray);
    T = Filters.GuidedImageFilter(gray, T, r, eps);
    Mat tSmooth = new Mat();
    Imgproc.GaussianBlur(T, tSmooth, new Size(81, 81), 40);
    Mat tDetails = new Mat();
    Core.subtract(T, tSmooth, tDetails);
    Core.multiply(tDetails, new Scalar(gamma), tDetails);
    Core.add(tSmooth, tDetails, T);
    return T;
  }

  private static Mat computeTrans(Mat img, int patchSz, int rows, int cols, int type, double[] airLight, double lambda,
      double fTrans) {
    Mat T = new Mat(rows, cols, type);
    for (int i = 0; i < rows; i += patchSz) {
      for (int j = 0; j < cols; j += patchSz) {
        int endRow = Math.min(i + patchSz, rows);
        int endCol = Math.min(j + patchSz, cols);
        Mat blkIm = img.submat(i, endRow, j, endCol);
        double Trans = BlkTransEstimate.blkEstimate(blkIm, airLight, lambda, fTrans);
        for (int m = i; m < endRow; m++)
          for (int n = j; n < endCol; n++)
            T.put(m, n, Trans);
      }
    }
    return T;
  }

}