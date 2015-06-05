package mk.ukim.finki.is;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class ShootingTargetDetection {

    static Scalar[] colors = new Scalar[]{
            new Scalar(0, 0, 0),
            new Scalar(255, 255, 255),
            new Scalar(195, 255, 1),
            new Scalar(70, 78, 161),
            new Scalar(195, 26, 252),
            new Scalar(255, 132, 132),
            new Scalar(0, 212, 253),
            new Scalar(46, 0, 0),
            new Scalar(252, 3, 0),
            new Scalar(0, 0, 254),
    };

    private Mat source;
    private List<Circle> circles;
    private double distanceBetweenCircles;
    private List<Hit> hits;
    private int points;
    private Mat output;
    private Point center;

    public ShootingTargetDetection(String image) {

        File f = new File(image);

        if (!f.exists()) {
            System.out.println("The file doesn't exist!");
            return;
        }

        source = Highgui.imread(image);
        source = resize(source);

        DetectCircles detectCircles = new DetectCircles(source);
        DetectHit detectHit = new DetectHit(source);

        circles = detectCircles.detectMultipleCircles();
        distanceBetweenCircles = detectCircles.findDistanceCircles(circles);
        center = detectCircles.getCenter();

        hits = detectHit.detect();

        points = calculatePoints();
        output = generateImage();
    }

    public Mat getOutput() {
        return output;
    }

    public int getPoints() {
        return points;
    }

    public int[] getPointsArray() {
        int[] res = new int[hits.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = hits.get(i).points;
        }
        return res;
    }

    private Mat generateImage() {
        Mat out = source.clone();
        Circle max = Collections.max(circles);
        Core.circle(out, center, 3, new Scalar(0, 0, 255), 3);
        for (int i = 0; i < 10; i++) {
            Core.circle(out, center, max.radius - (i * (int) Math.round(distanceBetweenCircles)), new Scalar(0, 255, 0), 3);
        }

        for (Hit h : hits) {
            if (h.points > 0) {
                Core.circle(out, h.center, h.radius, colors[h.points-1], 3);
            }
        }

        Core.putText(out, String.valueOf(points), new Point(40, 100), Core.FONT_HERSHEY_PLAIN, 6, new Scalar(0, 0, 255), 3);
        return out;
    }

    private int calculatePoints() {
        int points = 0;

        for (Hit h : hits) {
            double distanceFromCenter = dist(h.center, center) - h.radius;
            System.out.println(h + " " + distanceFromCenter / distanceBetweenCircles);
            int circle = (int) Math.floor(distanceFromCenter / distanceBetweenCircles);
            int p = 10 - circle;
            if (p > 0) {
                h.setPoints(p);
                points += p;
            }
        }

        return points;
    }

    private static Mat resize(Mat source) {
        if (Math.min(source.cols(), source.rows()) > 1000) {
            double scale  = Math.min(source.cols(), source.rows()) / 1000.0;
            long new_width = Math.round((source.cols() / scale));
            long new_height = Math.round((source.rows() / scale));
            Imgproc.resize(source, source, new Size(new_width, new_height));
        }
        return source;
    }

    private static double dist(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
    }

}
