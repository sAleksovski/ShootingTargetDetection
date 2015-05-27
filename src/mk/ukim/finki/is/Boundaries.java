package mk.ukim.finki.is;

import org.opencv.core.Point;

public class Boundaries {

    int minX;
    int maxX;
    int minY;
    int maxY;

    public Boundaries(int y, int x) {
        minX = x;
        maxX = x;
        minY = y;
        maxY = y;
    }

    public void set(int y, int x) {
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
    }

    public boolean isLine() {
        return maxX - minX < 3 || maxY - minY < 3;
    }

    public int smallerSide() {
        return Math.min((maxX - minX), (maxY - minY));
    }

    public int biggerSide() {
        return Math.max((maxX - minX), (maxY - minY));
    }

    public boolean isHit(int threshold) {
        return Math.abs((maxX - minX) - (maxY - minY)) < threshold;
    }

    public Hit getHit() {
        int x = (minX + maxX) / 2;
        int y = (minY + maxY) / 2;
        int radius = Math.min((maxX - minX), (maxY - minY)) / 2;
        return new Hit(new Point(x, y), radius);
    }

    @Override
    public String toString() {
        return "Boundaries{" +
                "minX=" + minX +
                ", maxX=" + maxX +
                ", minY=" + minY +
                ", maxY=" + maxY +
                ", sizeX=" + (maxX - minX) +
                ", sizeY=" + (maxY - minY) +
                '}';
    }

}
