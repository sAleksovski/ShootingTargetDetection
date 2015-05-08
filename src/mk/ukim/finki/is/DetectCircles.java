package mk.ukim.finki.is;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class DetectCircles {

    /**
     * Finds center for a given image
     * @param src Input image
     * @return Center of the image if it exists, null otherwise
     */
    @Deprecated
    public Point detectCenter(Mat src) {
        Mat src_grey = new Mat();
        Imgproc.cvtColor(src, src_grey, Imgproc.COLOR_RGB2GRAY );
        Imgproc.GaussianBlur(src_grey, src_grey, new Size(3, 3), 2, 2);

        Mat circles = new Mat();
        double cannyUpperThreshold = 200;
        int iMinRadius = 0;
        int iMaxRadius = 0;
        double accumulator = 300;
        int lineThickness = 3;

        Imgproc.HoughCircles(src_grey, circles, Imgproc.CV_HOUGH_GRADIENT,
                1.0, 100, cannyUpperThreshold, accumulator,
                iMinRadius, iMaxRadius);

        if (circles.cols() > 0)
            for (int x = 0; x < circles.cols(); x++)
            {
                double vCircle[] = circles.get(0,x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                System.out.print("Point: " + pt);
                int radius = (int)Math.round(vCircle[2]);
                System.out.println(" Radius: " + radius);
                return pt;
            }

        return null;
    }

    /**
     * Draws circle on given image
     * @param src Input image
     * @return Image with draw circles
     */
    public Mat detectMultipleCircles(Mat src) {
        Mat src_grey = new Mat();
        Imgproc.cvtColor(src, src_grey, Imgproc.COLOR_RGB2GRAY );
        Imgproc.GaussianBlur(src_grey, src_grey, new Size(3, 3), 2, 2);

        Mat circles = new Mat();
        double cannyUpperThreshold = 100;
        int iMinRadius = 0;
        int iMaxRadius = 0;
        double accumulator = 200;
        int lineThickness = 3;
        List<Circle> circlesList = new ArrayList<Circle>();

        Imgproc.HoughCircles(src_grey, circles, Imgproc.CV_HOUGH_GRADIENT,
                1.0, 0.01, cannyUpperThreshold, accumulator,
                iMinRadius, iMaxRadius);

        //Point center = detectCenter(src);
        Point center = null;

        if (circles.cols() > 0)
            for (int x = 0; x < circles.cols(); x++)
            {
                double vCircle[] = circles.get(0,x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                if (center == null)
                    center = pt;

                System.out.print("Point: " + pt);
                int radius = (int)Math.round(vCircle[2]);
                System.out.println(" Radius: " + radius);
                Circle c = new Circle(pt, radius);

                if (dist(center, pt) < 10 && !alreadyFound(circlesList, c)) {
                    Core.circle(src_grey, pt, radius, new Scalar(0, 255, 0), lineThickness);
                    Core.circle(src_grey, pt, 3, new Scalar(100, 100, 255), lineThickness);
                    circlesList.add(c);
                }

            }

        double dist = findDistanceCircles(circlesList);

        Circle max = Collections.max(circlesList);
        Core.circle(src_grey, center, 3, new Scalar(100, 100, 255), lineThickness);
        for (int i = 0; i < 10; i++) {
            Core.circle(src_grey, center, max.radius - (i * (int) Math.round(dist)), new Scalar(94,206,165,255), lineThickness);
        }

        return src_grey;
    }

    /**
     * Draws edge on a given image
     * @param src Input imaage
     * @return Image with only edges
     */
    public Mat canny_edge(Mat src) {
        int edgeThresh = 1;
        int lowThreshold = 100;
        int max_lowThreshold = 100;
        int ratio = 3;
        int kernel_size = 3;
        Mat detected_edges = new Mat();

        /// Reduce noise with a kernel 3x3
        Imgproc.blur(src, detected_edges, new Size(3,3));

        /// Canny detector
        Imgproc.Canny(detected_edges, detected_edges, lowThreshold, lowThreshold * ratio, kernel_size, false);
        Mat dst = Mat.zeros(src.size(), 0);
        src.copyTo(dst, detected_edges);
        return dst;
    }

    /**
     * Changes contrast of a given image
     * @param src Input image
     * @return Image with changed contrast
     */
    public Mat contrast(Mat src) {
        Mat res = src.clone();
        double alpha = 1.4;
        double beta = 20;
        for (int y = 0; y < src.rows(); y++) {
            for (int x = 0; x < src.cols(); x++) {
                double[] newPixel = new double[3];
                for (int c = 0; c < 3; c++) {
                    newPixel[c] = alpha * (src.get(y, x)[c]+beta);
                }
                res.put(y, x, newPixel);
            }
        }
        return res;
    }

    /**
     * Finds distance between two points
     * @param p1 Input point 1
     * @param p2 Input point 2
     * @return Distance between those points
     */
    private double dist(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
    }

    /**
     * Check if new circle is already in list of circles by comparing their radius
     * @param circles List of circles
     * @param testCircle Circle to be tested
     * @return does list of circles contain test circle
     */
    private boolean alreadyFound(List<Circle> circles, Circle testCircle) {
        for(Circle c : circles)
            if (c.similar(testCircle))
                return true;
        return false;
    }

    /**
     * Draws contour of given image in random color for each contour
     * @param src Input image
     * @return Image with contours only
     */
    public Mat findContour(Mat src) {
        int edgeThresh = 1;
        int lowThreshold = 100;
        int max_lowThreshold = 100;
        int ratio = 2;
        int kernel_size = 3;
        Mat detected_edges = new Mat();
        Mat src_grey = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();


        /// Reduce noise with a kernel 3x3
        Imgproc.blur(src, src_grey, new Size(3,3));
        Imgproc.cvtColor(src_grey, src_grey, Imgproc.COLOR_RGB2GRAY );

        /// Canny detector
        Imgproc.Canny(src_grey, detected_edges, lowThreshold, lowThreshold * ratio, kernel_size, false);
        Imgproc.findContours (detected_edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0 , 0));

        Mat dst = Mat.zeros(detected_edges.size(), CvType.CV_8UC3);
        Random r = new Random();
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(r.nextInt(255), r.nextInt(255), r.nextInt(255));
            Imgproc.drawContours(dst, contours, i, color, 2, 8, hierarchy, 0, new Point(0, 0));
        }
        return dst;
    }

    /**
     * Returns probable distance between circles by comparing radius of circles
     * @param circles List of circles
     * @return Distance between circles
     */
    private double findDistanceCircles(List<Circle> circles) {
        int maxRadius = Collections.max(circles).radius;
        double averageDist = 0;
        System.out.println("Size: " + circles.size());
        for (int i = 0; i < circles.size(); i++) {
            if (circles.get(i).radius == maxRadius)
                continue;
            int innerRadius = circles.get(i).radius;

            double aprox = maxRadius / 10.0;
            double diff = Double.POSITIVE_INFINITY;
            double distanceCircles = 0;
            for (int j = 1; j <= 10; j++) {
                double dist = (maxRadius - innerRadius) / (double)j;
                if (Math.abs(dist - aprox) < diff) {
                    distanceCircles = dist;
                    diff = Math.abs(dist - aprox);
                }
            }
            return distanceCircles;
            //averageDist += distanceCircles;
            //System.out.println(distanceCircles);
        }

        return averageDist / (circles.size() - 1);
    }

    public static void main( String[] args )
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src_grey = new Mat();
        Scanner scanner = new Scanner(System.in);

        Mat src = Highgui.imread("img/21.03.2015_Page_1.jpg");

        if (src == null)
            return;

        DetectCircles dc = new DetectCircles();
        /*Point center = dc.detectCenter(src);
        Mat detection = dc.detectMultipleCircles(src);
        Core.circle(detection, center, 3, new Scalar(100, 100, 255), 2);*/

        String filename = "img-out/detected-21.03.2015_Page_1.jpg";
        System.out.println(String.format("Writing %s", filename));
        Highgui.imwrite(filename, dc.detectMultipleCircles(src));

    }
}

class Circle implements Comparable<Circle> {
    int radius;
    Point center;

    public Circle(Point c, int radius) {
        this.radius = radius;
        center = c;
    }

    public boolean similar(Circle c) {
        return Math.abs(radius - c.radius) <= 15.0;
    }

    @Override
    public int compareTo(Circle o) {
        return Integer.compare(radius, o.radius);
    }
}
