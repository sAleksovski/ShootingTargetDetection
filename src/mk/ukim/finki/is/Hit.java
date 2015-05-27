package mk.ukim.finki.is;

import org.opencv.core.Point;

public class Hit {

    Point center;
    int radius;
    int points;

    public Hit(Point center, int radius) {
        this.center = center;
        this.radius = radius;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean intersect(Hit h) {
        return dist(center, h.center) < radius + h.radius;
    }

    private double dist(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
    }

    @Override
    public String toString() {
        return "Hit{" +
                "center=" + center +
                ", radius=" + radius +
                '}';
    }
}
