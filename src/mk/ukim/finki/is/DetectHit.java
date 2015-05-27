package mk.ukim.finki.is;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DetectHit {

    static int SHOT_DIAMETER_MIN_THRESHOLD = 15;   // shot diameter min threshold [pixels]
    static int SHOT_DIAMETER_MAX_THRESHOLD = 45;   // shot diameter max threshold [pixels]

    static int SHOT_SIDE_DIFFERENCE_THRESHOLD = 10; // maximum difference of the sides of a hit

    static int BLACK_THRESHOLD = 128;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

//        Mat source = Highgui.imread("img/21.03.2015_Page_1.jpg");
//        Mat source = Highgui.imread("img/Baze 11.03.2015_Page_5.jpg");
//        Mat source = Highgui.imread("img/21.04.2015-Blaze-Tofilovski_Page_01.jpg");
//        Mat source = Highgui.imread("img/21.04.2015-Blaze-Tofilovski_Page_09.jpg");
//        Mat source = Highgui.imread("img/DSC_0202.JPG");
        Mat source = Highgui.imread("img/DSC_0279.JPG");
//        Mat source = Highgui.imread("img/Trening 01.02.2014_Page_8.jpg");

        if (Math.min(source.cols(), source.rows()) > 1000) {
            double scale  = Math.min(source.cols(), source.rows()) / 1000.0;
            long new_width = Math.round((source.cols() / scale));
            long new_height = Math.round((source.rows() / scale));
            System.out.printf("resize to: %d x %d\n", new_width, new_height);
            Imgproc.resize(source, source, new Size(new_width, new_height));
        }

        Mat greyscale = toBlackAndWhite(source);
        Mat mask = generateMask(greyscale);
        List<Hit> hits = detectHits(mask);

        Mat detectedHits = source.clone();

        for (Hit h : hits) {
            Core.circle(detectedHits, h.center, h.radius, new Scalar(0, 0, 255), 3);
        }

        Highgui.imwrite("img-out/mask.jpg", mask);
        Highgui.imwrite("img-out/greyscale.jpg", greyscale);

        showResult(detectedHits);
    }

    public static List<Hit> detect(Mat source) {
        Mat greyscale = toBlackAndWhite(source);
        Mat mask = generateMask(greyscale);
        return detectHits(mask);
    }

    public static Mat toBlackAndWhite(Mat in) {
        Mat out = in.clone();
        Imgproc.cvtColor(in, out, Imgproc.COLOR_RGB2GRAY);
        for (int i = 0; i < in.rows(); i++) {
            for (int j = 0; j < in.cols(); j++) {
//                double d = in.get(i, j)[0];
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

    public static Mat generateMask(Mat greyscale) {
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

    public static List<Hit> detectHits(Mat mask) {
        mask = mask.clone();
        List<Hit> hits = new ArrayList<Hit>();
        List<Boundaries> refineBoundaries = new ArrayList<Boundaries>();

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

        List<Hit> r = new ArrayList<Hit>();
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

    private static void floodFill(Mat mask, int row, int col, Boundaries boundaries) {
        boundaries.set(row, col);

        for (int i = 0; i < ffx.length; i++) {
            if (mask.get(row + ffx[i], col + ffy[i])[0] == 1) {
                mask.put(row + ffx[i], col + ffy[i], 0);
                floodFill(mask, row + ffx[i], col + ffy[i], boundaries);
            }
        }

    }

    private static boolean canMergeBoundaries(Boundaries b1, Boundaries b2) {
        Boundaries b3 = new Boundaries(b1.minY, b1.minX);
        b3.set(b1.maxY, b1.maxX);
        b3.set(b2.minY, b2.minX);
        b3.set(b2.maxY, b2.maxX);

        if (b3.smallerSide() >= SHOT_DIAMETER_MIN_THRESHOLD
                && b3.biggerSide() <= SHOT_DIAMETER_MAX_THRESHOLD
                && b3.isHit(SHOT_SIDE_DIFFERENCE_THRESHOLD)) {
            return true;
        }

        return false;
    }

    public static void showResult(Mat img) {
        Imgproc.resize(img, img, new Size(500, 500));
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
