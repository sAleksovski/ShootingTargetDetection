package mk.ukim.finki.is;

import org.opencv.core.Point;

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

    public boolean hit(Hit h) {
        return dist(center, h.center) < radius;
    }

    private double dist(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
    }

    @Override
    public int compareTo(Circle o) {
        return Integer.compare(radius, o.radius);
    }

    @Override
    public String toString() {
        return "Circle{" +
                "center=" + center +
                ", radius=" + radius +
                '}';
    }
}
