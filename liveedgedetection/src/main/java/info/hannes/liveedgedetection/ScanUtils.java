package info.hannes.liveedgedetection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.hardware.Camera;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import info.hannes.liveedgedetection.view.Quadrilateral;
import timber.log.Timber;

public class ScanUtils {

    public static boolean compareFloats(double left, double right) {
        double epsilon = 0.00000001;
        return Math.abs(left - right) < epsilon;
    }

    public static Camera.Size determinePictureSize(Camera camera, Camera.Size previewSize) {
        if (camera == null) return null;
        Camera.Parameters cameraParams = camera.getParameters();
        List<Camera.Size> pictureSizeList = cameraParams.getSupportedPictureSizes();
        Collections.sort(pictureSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size1, Camera.Size size2) {
                Double h1 = Math.sqrt(size1.width * size1.width + size1.height * size1.height);
                Double h2 = Math.sqrt(size2.width * size2.width + size2.height * size2.height);
                return h2.compareTo(h1);
            }
        });
        Camera.Size retSize = null;

        // if the preview size is not supported as a picture size
        float reqRatio = ((float) previewSize.width) / previewSize.height;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        for (Camera.Size size : pictureSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
            if (ScanUtils.compareFloats(deltaRatio, 0)) {
                break;
            }
        }

        return retSize;
    }

    public static Camera.Size getOptimalPreviewSize(Camera camera, int w, int h) {
        if (camera == null) return null;
        final double targetRatio = (double) h / w;
        Camera.Parameters cameraParams = camera.getParameters();
        List<Camera.Size> previewSizeList = cameraParams.getSupportedPreviewSizes();
        Collections.sort(previewSizeList, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size1, Camera.Size size2) {
                double ratio1 = (double) size1.width / size1.height;
                double ratio2 = (double) size2.width / size2.height;
                Double ratioDiff1 = Math.abs(ratio1 - targetRatio);
                double ratioDiff2 = Math.abs(ratio2 - targetRatio);
                if (ScanUtils.compareFloats(ratioDiff1, ratioDiff2)) {
                    Double h1 = Math.sqrt(size1.width * size1.width + size1.height * size1.height);
                    Double h2 = Math.sqrt(size2.width * size2.width + size2.height * size2.height);
                    return h2.compareTo(h1);
                }
                return ratioDiff1.compareTo(ratioDiff2);
            }
        });

        return previewSizeList.get(0);
    }

    public static int configureCameraAngle(Activity activity) {
        int cameraOrientationAngle;

        Display display = activity.getWindowManager().getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_90:
                cameraOrientationAngle = 0;
                break;
            case Surface.ROTATION_180:
                cameraOrientationAngle = 270;
                break;
            case Surface.ROTATION_270:
                cameraOrientationAngle = 180;
                break;
            case Surface.ROTATION_0:
            default:
                cameraOrientationAngle = 90;
                break;
        }

        return cameraOrientationAngle;
    }


    public static double getMaxCosine(double maxCosine, Point[] approxPoints) {
        for (int i = 2; i < 5; i++) {
            double cosine = Math.abs(angle(approxPoints[i % 4], approxPoints[i - 2], approxPoints[i - 1]));
            Timber.i(String.valueOf(cosine));
            maxCosine = Math.max(cosine, maxCosine);
        }
        return maxCosine;
    }

    private static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    private static Point[] sortPoints(Point[] src) {
        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));
        Point[] result = {null, null, null, null};

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.compare(lhs.y + lhs.x, rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.compare(lhs.y - lhs.x, rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);
        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);
        // top-right corner = minimal difference
        result[1] = Collections.min(srcPoints, diffComparator);
        // bottom-left corner = maximal difference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    private static Mat morph_kernel = new Mat(new Size(ScanConstants.KSIZE_CLOSE, ScanConstants.KSIZE_CLOSE), CvType.CV_8UC1, new Scalar(255));

    public static Quadrilateral detectLargestQuadrilateral(Mat originalMat) {
        Imgproc.cvtColor(originalMat, originalMat, Imgproc.COLOR_BGR2GRAY, 4);

        // Just OTSU/Binary thresholding is not enough.
        //Imgproc.threshold(mGrayMat, mGrayMat, 150, 255, THRESH_BINARY + THRESH_OTSU);

        /*
         *  1. We shall first blur and normalize the image for uniformity,
         *  2. Truncate light-gray to white and normalize,
         *  3. Apply canny edge detection,
         *  4. Cutoff weak edges,
         *  5. Apply closing(morphology), then proceed to finding contours.
         */

        // step 1.
        Imgproc.blur(originalMat, originalMat, new Size(ScanConstants.KSIZE_BLUR, ScanConstants.KSIZE_BLUR));
        Core.normalize(originalMat, originalMat, 0, 255, Core.NORM_MINMAX);
        // step 2.
        // As most papers are bright in color, we can use truncation to make it uniformly bright.
        Imgproc.threshold(originalMat, originalMat, ScanConstants.TRUNC_THRESH, 255, Imgproc.THRESH_TRUNC);
        Core.normalize(originalMat, originalMat, 0, 255, Core.NORM_MINMAX);
        // step 3.
        // After above preprocessing, canny edge detection can now work much better.
        Imgproc.Canny(originalMat, originalMat, ScanConstants.CANNY_THRESH_U, ScanConstants.CANNY_THRESH_L);
        // step 4.
        // Cutoff the remaining weak edges
        Imgproc.threshold(originalMat, originalMat, ScanConstants.CUTOFF_THRESH, 255, Imgproc.THRESH_TOZERO);
        // step 5.
        // Closing - closes small gaps. Completes the edges on canny image; AND also reduces stringy lines near edge of paper.
        Imgproc.morphologyEx(originalMat, originalMat, Imgproc.MORPH_CLOSE, morph_kernel, new Point(-1, -1), 1);

        // Get only the 10 largest contours (each approximated to their convex hulls)
        List<MatOfPoint> largestContour = findLargestContours(originalMat, 10);
        if (null != largestContour) {
            return findQuadrilateral(largestContour);
        }
        return null;
    }

    private static MatOfPoint hull2Points(MatOfInt hull, MatOfPoint contour) {
        List<Integer> indexes = hull.toList();
        List<Point> points = new ArrayList<>();
        List<Point> ctrList = contour.toList();
        for (Integer index : indexes) {
            points.add(ctrList.get(index));
        }
        MatOfPoint point = new MatOfPoint();
        point.fromList(points);
        return point;
    }

    private static List<MatOfPoint> findLargestContours(Mat inputMat, int NUM_TOP_CONTOURS) {
        Mat mHierarchy = new Mat();
        List<MatOfPoint> mContourList = new ArrayList<>();
        //finding contours - as we are sorting by area anyway, we can use RETR_LIST - faster than RETR_EXTERNAL.
        Imgproc.findContours(inputMat, mContourList, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Convert the contours to their Convex Hulls i.e. removes minor nuances in the contour
        List<MatOfPoint> mHullList = new ArrayList<>();
        MatOfInt tempHullIndices = new MatOfInt();
        for (int i = 0; i < mContourList.size(); i++) {
            Imgproc.convexHull(mContourList.get(i), tempHullIndices);
            mHullList.add(hull2Points(tempHullIndices, mContourList.get(i)));
        }
        // Release mContourList as its job is done
        for (MatOfPoint c : mContourList)
            c.release();
        tempHullIndices.release();
        mHierarchy.release();

        if (mHullList.size() != 0) {
            Collections.sort(mHullList, new Comparator<MatOfPoint>() {
                @Override
                public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                    return Double.compare(Imgproc.contourArea(rhs), Imgproc.contourArea(lhs));
                }
            });
            return mHullList.subList(0, Math.min(mHullList.size(), NUM_TOP_CONTOURS));
        }
        return null;
    }

    private static Quadrilateral findQuadrilateral(List<MatOfPoint> mContourList) {
        for (MatOfPoint c : mContourList) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);
            Point[] points = approx.toArray();
            // select biggest 4 angles polygon
            if (approx.rows() == 4) {
                Point[] foundPoints = sortPoints(points);
                return new Quadrilateral(approx, foundPoints);
            }
        }
        return null;
    }

    public static Bitmap enhanceReceipt(Bitmap image, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight) {
        int resultWidth = (int) (topRight.x - topLeft.x);
        int bottomWidth = (int) (bottomRight.x - bottomLeft.x);
        if (bottomWidth > resultWidth)
            resultWidth = bottomWidth;

        int resultHeight = (int) (bottomLeft.y - topLeft.y);
        int bottomHeight = (int) (bottomRight.y - topRight.y);
        if (bottomHeight > resultHeight)
            resultHeight = bottomHeight;

        Mat inputMat = new Mat(image.getHeight(), image.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(image, inputMat);
        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC1);

        List<Point> source = new ArrayList<>();
        source.add(topLeft);
        source.add(topRight);
        source.add(bottomLeft);
        source.add(bottomRight);
        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(resultWidth, 0);
        Point ocvPOut3 = new Point(0, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, resultHeight);
        List<Point> dest = new ArrayList<>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight));

        Bitmap output = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, output);
        return output;
    }

    /*
     * This method converts the dp value to px
     * @param context context
     * @param dp value in dp
     * @return px value
     */
    public static int dp2px(Context context, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return Math.round(px);
    }

    @Deprecated
    public static Bitmap loadEfficientBitmap(byte[] data, int width, int height) {
        Bitmap bmp;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return bmp;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }

            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    public static Bitmap resizeToScreenContentSize(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public static ArrayList<PointF> getPolygonDefaultPoints(Bitmap bitmap) {
        ArrayList<PointF> points;
        points = new ArrayList<>();
        points.add(new PointF(bitmap.getWidth() * (0.14f), (float) bitmap.getHeight() * (0.13f)));
        points.add(new PointF(bitmap.getWidth() * (0.84f), (float) bitmap.getHeight() * (0.13f)));
        points.add(new PointF(bitmap.getWidth() * (0.14f), (float) bitmap.getHeight() * (0.83f)));
        points.add(new PointF(bitmap.getWidth() * (0.84f), (float) bitmap.getHeight() * (0.83f)));
        return points;
    }

    public static boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }
}
