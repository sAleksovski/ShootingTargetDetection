package mk.ukim.finki.is;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by Stefan on 24/5/2015.
 */
public class DetectHit {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

//        Mat pic0 = Highgui.imread("img/21.04.2015-Blaze-Tofilovski_Page_03.jpg");   // Best
        Mat pic0 = Highgui.imread("img/21.03.2015_Page_1.jpg");
        Mat pic1 = pic0.clone();
        Imgproc.cvtColor(pic1, pic1, Imgproc.COLOR_RGB2GRAY);

        pic1 = toBlackAndWhite(pic1);
        pic1 = processImage(pic0, pic1);

        showResult(pic1);
    }

    public static Mat toBlackAndWhite(Mat in) {
        for (int i = 0; i < in.rows(); i++) {
            for (int j = 0; j < in.cols(); j++) {
                double d = in.get(i, j)[0];
                if (d < 128) {
                    in.put(i, j, 0);
                } else {
                    in.put(i, j, 255);
                }
            }
        }
        return in;
    }

    public static Mat processImage(Mat original, Mat grayscale) {
        Mat mask = new Mat(grayscale.rows(), grayscale.cols(), CvType.CV_32F);
        mask.setTo(new Scalar(255, 255, 255));

        int r1a = 15;   // shot diameter min threshold [pixels]
        int r1b = 45;   // shot diameter max threshold [pixels]

        for (int y = r1b; y < grayscale.rows() - r1b - 1; y++) {
            for (int x = r1b; x < grayscale.cols() - r1b - 1; x++) {

                int wy = 1;
                for (int i = 1; i <= r1b; i++) {
                    if (grayscale.get(y-i, x)[0] == grayscale.get(y, x)[0]) wy++;
                    else break;
                }

                for (int i = 1; i <= r1b ; i++) {
                    if (grayscale.get(y+i, x)[0] == grayscale.get(y, x)[0]) wy++;
                    else break;
                }

                int wx = 1;
                for (int i = 1; i <= r1b; i++) {
                    if (grayscale.get(y, x-i)[0] == grayscale.get(y, x)[0]) wx++;
                    else break;
                }

                for (int i = 1; i <= r1b; i++) {
                    if (grayscale.get(y, x+i)[0] == grayscale.get(y, x)[0]) wx++;
                    else break;
                }

                if (grayscale.get(y, x)[0] == 255.00) {
                    if ( (wx>r1a) && (wy>r1a) ) {
                        if ((wx<r1b)&&(wy<r1b)) {
                            mask.put(y, x, 3);
                        }
                    }
                }

            }
        }

        double[] color = new double[]{0, 0, 255};

        Mat result = original.clone();

        for (int y = 0; y < grayscale.rows(); y++) {
            for (int x = 0; x < grayscale.cols(); x++) {
                if (mask.get(y, x)[0] == 3) {
                    result.put(y, x, color);
                }
            }
        }

        return result;
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
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
