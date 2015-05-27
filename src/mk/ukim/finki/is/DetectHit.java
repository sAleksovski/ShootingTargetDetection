package mk.ukim.finki.is;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DetectHit {

    static int SHOT_DIAMETER_MIN_THRESHOLD = 15;   // shot diameter min threshold [pixels]
    static int SHOT_DIAMETER_MAX_THRESHOLD = 45;   // shot diameter max threshold [pixels]

    static int SHOT_SIDE_DIFFERENCE_THRESHOLD = 10; // maximum difference of the sides of a hit

    static int BLACK_THRESHOLD = 128;

    Mat image;

    public DetectHit(Mat image) {
        this.image = image.clone();
    }

    public List<Hit> detect() {
        Mat greyscale = toBlackAndWhite(image);
        Mat mask = generateMask(greyscale);
        return detectHits(mask);
    }

    private Mat toBlackAndWhite(Mat in) {
        Mat out = in.clone();
        Imgproc.cvtColor(in, out, Imgproc.COLOR_RGB2GRAY);
        for (int i = 0; i < in.rows(); i++) {
            for (int j = 0; j < in.cols(); j++) {
                double d = (in.get(i, j)[0] + in.get(i, j)[1] + in.get(i, j)[2]) / 3.0;
                if (d < BLACK_THRESHOLD) {
                    out.put(i, j, 0);
                } else {
                    out.put(i, j, 255);
                }
            }
        }
        return out;
    }

    private Mat generateMask(Mat greyscale) {
        Mat mask = new Mat(greyscale.rows(), greyscale.cols(), CvType.CV_32F);
        mask.setTo(new Scalar(255, 255, 255));

        for (int y = SHOT_DIAMETER_MAX_THRESHOLD; y < greyscale.rows() - SHOT_DIAMETER_MAX_THRESHOLD - 1; y++) {
            for (int x = SHOT_DIAMETER_MAX_THRESHOLD; x < greyscale.cols() - SHOT_DIAMETER_MAX_THRESHOLD - 1; x++) {

                int wy = 1;
                for (int i = 1; i <= SHOT_DIAMETER_MAX_THRESHOLD; i++) {
                    if (greyscale.get(y-i, x)[0] == greyscale.get(y, x)[0]) wy++;
                    else break;
                }

                for (int i = 1; i <= SHOT_DIAMETER_MAX_THRESHOLD ; i++) {
                    if (greyscale.get(y+i, x)[0] == greyscale.get(y, x)[0]) wy++;
                    else break;
                }

                int wx = 1;
                for (int i = 1; i <= SHOT_DIAMETER_MAX_THRESHOLD; i++) {
                    if (greyscale.get(y, x-i)[0] == greyscale.get(y, x)[0]) wx++;
                    else break;
                }

                for (int i = 1; i <= SHOT_DIAMETER_MAX_THRESHOLD; i++) {
                    if (greyscale.get(y, x+i)[0] == greyscale.get(y, x)[0]) wx++;
                    else break;
                }

                if (greyscale.get(y, x)[0] == 255.00) {
                    if ( (wx>SHOT_DIAMETER_MIN_THRESHOLD) && (wy>SHOT_DIAMETER_MIN_THRESHOLD) ) {
                        if ((wx<SHOT_DIAMETER_MAX_THRESHOLD)&&(wy<SHOT_DIAMETER_MAX_THRESHOLD)) {
                            mask.put(y, x, 1);
                        }
                    }
                }

            }
        }

        return mask;
    }

    private List<Hit> detectHits(Mat mask) {
        mask = mask.clone();
        List<Hit> hits = new ArrayList<>();
        List<Boundaries> refineBoundaries = new ArrayList<>();

        for (int y = 1; y < mask.rows()-1; y++) {
            for (int x = 1; x < mask.cols()-1; x++) {
                if (mask.get(y, x)[0] == 1) {
                    Boundaries boundaries = new Boundaries(y, x);
                    floodFill(mask, y, x, boundaries);
                    if (boundaries.smallerSide() >= SHOT_DIAMETER_MIN_THRESHOLD
                            && boundaries.biggerSide() <= SHOT_DIAMETER_MAX_THRESHOLD
                            && boundaries.isHit(SHOT_SIDE_DIFFERENCE_THRESHOLD)) {
                        hits.add(boundaries.getHit());
                    } else if (!boundaries.isLine()){
                        refineBoundaries.add(boundaries);
                    }
                }
            }
        }

        for (int i = 0; i < refineBoundaries.size(); i++) {
            for (int j = i + 1; j < refineBoundaries.size(); j++) {
                Boundaries b1 = refineBoundaries.get(i);
                Boundaries b2 = refineBoundaries.get(j);
                if (canMergeBoundaries(b1, b2)) {
                    Boundaries b3 = new Boundaries(b1.minY, b1.minX);
                    b3.set(b1.maxY, b1.maxX);
                    b3.set(b2.minY, b2.minX);
                    b3.set(b2.maxY, b2.maxX);
                    hits.add(b3.getHit());
                }
            }
        }

        List<Hit> r = new ArrayList<>();
        r.addAll(hits);

        for (int i = 0; i < hits.size(); i++) {
            for (int j = i + 1; j < hits.size(); j++) {
                if (hits.get(i).intersect(hits.get(j))) {
                    r.remove(hits.get(i));
                    r.remove(hits.get(j));
                }
            }
        }

        return r;
    }

    private static int[] ffx = new int[]{0, +1, 0, -1};
    private static int[] ffy = new int[]{-1, 0, +1, 0};

    private void floodFill(Mat mask, int row, int col, Boundaries boundaries) {
        boundaries.set(row, col);

        for (int i = 0; i < ffx.length; i++) {
            if (mask.get(row + ffx[i], col + ffy[i])[0] == 1) {
                mask.put(row + ffx[i], col + ffy[i], 0);
                floodFill(mask, row + ffx[i], col + ffy[i], boundaries);
            }
        }

    }

    private boolean canMergeBoundaries(Boundaries b1, Boundaries b2) {
        Boundaries b3 = new Boundaries(b1.minY, b1.minX);
        b3.set(b1.maxY, b1.maxX);
        b3.set(b2.minY, b2.minX);
        b3.set(b2.maxY, b2.maxX);

        return b3.smallerSide() >= SHOT_DIAMETER_MIN_THRESHOLD
                && b3.biggerSide() <= SHOT_DIAMETER_MAX_THRESHOLD
                && b3.isHit(SHOT_SIDE_DIFFERENCE_THRESHOLD);
    }

}
