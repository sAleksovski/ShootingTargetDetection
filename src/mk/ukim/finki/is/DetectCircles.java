package mk.ukim.finki.is;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetectCircles {

    private Point center;
    private Mat image;

    public DetectCircles(Mat image) {
        this.image = image.clone();
    }

    public Point getCenter() {
        return center;
    }

    /**
     * Detects multiple circle on given image
     * @return List<Circle> of circles
     */
    public List<Circle> detectMultipleCircles() {
        Mat src_grey = new Mat();
        Imgproc.cvtColor(image, src_grey, Imgproc.COLOR_RGB2GRAY );
        Imgproc.GaussianBlur(src_grey, src_grey, new Size(3, 3), 2, 2);

        Mat circles = new Mat();
        double cannyUpperThreshold = 100;
        int iMinRadius = 0;
        int iMaxRadius = 0;
        double accumulator = 200;
        List<Circle> circlesList = new ArrayList<>();

        Imgproc.HoughCircles(src_grey, circles, Imgproc.CV_HOUGH_GRADIENT,
                1.0, 0.01, cannyUpperThreshold, accumulator,
                iMinRadius, iMaxRadius);

        center = null;

        for (int x = 0; x < circles.cols(); x++) {
            double vCircle[] = circles.get(0,x);

            if (vCircle == null) {
                break;
            }

            Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
            if (center == null) {
                center = pt;
            }

            int radius = (int)Math.round(vCircle[2]);
            Circle c = new Circle(pt, radius);

            if (dist(center, pt) < 10 && !alreadyFound(circlesList, c)) {
                circlesList.add(c);
            }
        }

        return circlesList;
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
     * Returns probable distance between circles by comparing radius of circles
     * @param circles List of circles
     * @return Distance between circles
     */
    public double findDistanceCircles(List<Circle> circles) {
        int maxRadius = Collections.max(circles).radius;
        double averageDist = 0;
        for (Circle circle : circles) {
            if (circle.radius == maxRadius)
                continue;
            int innerRadius = circle.radius;

            double approximate = maxRadius / 10.0;
            double diff = Double.POSITIVE_INFINITY;
            double distanceCircles = 0;
            for (int j = 1; j <= 10; j++) {
                double dist = (maxRadius - innerRadius) / (double) j;
                if (Math.abs(dist - approximate) < diff) {
                    distanceCircles = dist;
                    diff = Math.abs(dist - approximate);
                }
            }
            return distanceCircles;
        }

        return averageDist / (circles.size() - 1);
    }

}
