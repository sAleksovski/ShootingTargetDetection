package mk.ukim.finki.is;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        long start = System.currentTimeMillis();
        Mat source = Highgui.imread("img/DSC_0279.jpg");
        source = resize(source);

        DetectCircles dc = new DetectCircles();
        long dmc = System.currentTimeMillis();
        dc.detectMultipleCircles(source);
        System.out.println("detectMultipleCircles: " + (System.currentTimeMillis() - dmc) + " ms");
        List<Circle> circleList = dc.circlesList;
        double dist = dc.findDistanceCircles(circleList);
        Point center = dc.center;
        dmc = System.currentTimeMillis();
        List<Hit> hitsList = DetectHit.detect(source);
        System.out.println("detectHit: " + (System.currentTimeMillis() - dmc) + " ms");

        int points = 0;

        System.out.println("DISTANCE: " + dist);

        Collections.sort(circleList);
        Collections.reverse(circleList);

        System.out.println(circleList.get(0).radius - circleList.get(1).radius);

        for (Hit h : hitsList) {
            double distanceFromCenter = dist(h.center, center);
            int circle = (int) (distanceFromCenter / dist);
            System.out.println("d: " + distanceFromCenter + ", c:" + circle);
            int p = 10 - circle;
            h.setPoints(p);
            points += p;
        }

        Scalar[] colors = new Scalar[]{
                new Scalar(0, 0, 0),
                new Scalar(0, 0, 0),
                new Scalar(0, 0, 0),
                new Scalar(0, 0, 0),
                new Scalar(255, 0, 255),
                new Scalar(0, 255, 255),
                new Scalar(255, 255, 0),
                new Scalar(255, 0, 0),
                new Scalar(0, 255, 0),
                new Scalar(0, 0, 255),
        };

        Circle max = Collections.max(circleList);
        Core.circle(source, center, 3, new Scalar(0, 0, 255), 3);
        for (int i = 0; i < 10; i++) {
            Core.circle(source, center, max.radius - (i * (int) Math.round(dist)), new Scalar(0, 255, 0), 3);
        }

        for (Hit h : hitsList) {
            Core.circle(source, h.center, h.radius, colors[h.points-1], 3);
        }

        System.out.println("Points: " + points);

        System.out.println(System.currentTimeMillis() - start + " ms");

        DetectHit.showResult(source);
    }

    public static Mat resize(Mat source) {
        if (Math.min(source.cols(), source.rows()) > 1000) {
            double scale  = Math.min(source.cols(), source.rows()) / 1000.0;
            long new_width = Math.round((source.cols() / scale));
            long new_height = Math.round((source.rows() / scale));
            System.out.printf("resize to: %d x %d\n", new_width, new_height);
            Imgproc.resize(source, source, new Size(new_width, new_height));
        }
        return source;
    }

    public static double dist(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
    }

}
