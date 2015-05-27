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
