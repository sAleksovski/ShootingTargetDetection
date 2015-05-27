package mk.ukim.finki.is.gui;

import mk.ukim.finki.is.ShootingTargetDetection;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class GUI extends JFrame implements ActionListener, ImageOpeningFinished {

    JFileChooser jFileChooser;
    JMenuBar jMenuBar;
    JMenu jMenu;
    JMenuItem jMenuItemOpen;
    JMenuItem jMenuItemSave;
    JMenuItem jMenuItemExit;

    JLabel lblImage;
    Mat outputImageFullResolution;
    boolean imageOpened;

    public GUI() {
        super("GUI");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(null);

        jFileChooser = new JFileChooser();
        jFileChooser.setFileFilter(new FileFilter() {
            //Accept all directories and all gif, jpg, tiff, or png files.
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                String extension = Utils.getExtension(f);
                return extension != null && (extension.equals(Utils.tiff)
                        || extension.equals(Utils.tif)
                        || extension.equals(Utils.gif)
                        || extension.equals(Utils.jpeg)
                        || extension.equals(Utils.jpg)
                        || extension.equals(Utils.png));

            }

            //The description of this filter
            public String getDescription() {
                return "Image";
            }
        });

        jMenuBar = new JMenuBar();

        jMenu = new JMenu("File");

        jMenuItemOpen = new JMenuItem("Open");
        jMenuItemOpen.addActionListener(this);
        jMenu.add(jMenuItemOpen);

        jMenuItemSave = new JMenuItem("Save");
        jMenuItemSave.addActionListener(this);
        jMenu.add(jMenuItemSave);

        jMenuItemExit = new JMenuItem("Exit");
        jMenuItemExit.addActionListener(this);
        jMenu.add(jMenuItemExit);

        jMenuBar.add(jMenu);
        setJMenuBar(jMenuBar);

        lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblImage);

        imageOpened = false;
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        JFrame gui = new GUI();
        gui.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == jMenuItemOpen) {
            int returnVal = jFileChooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                System.out.println("Opening: " + file.getAbsolutePath());

                openImage(file.getAbsolutePath());
            }
        }

        if (e.getSource() == jMenuItemSave) {
            if (!imageOpened) {
                System.out.println("Image is not opened!");
                return;
            }

            int returnVal = jFileChooser.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                System.out.println("Writing: " + file.getAbsolutePath());

                saveImage(file.getAbsolutePath());
            }
        }

        if (e.getSource() == jMenuItemExit) {
            System.exit(0);
        }

    }

    public void openImage(String path) {
        lblImage.setText("Loading...");
        ImageOpener imageOpener = new ImageOpener(path, this);
        imageOpener.start();
    }

    public void saveImage(String path) {
        Highgui.imwrite(path, outputImageFullResolution);
    }

    @Override
    public void imageOpeningFinished(ShootingTargetDetection shootingTargetDetection) {
        outputImageFullResolution = shootingTargetDetection.getOutput();
        Mat img = outputImageFullResolution.clone();
        Imgproc.resize(img, img, new Size(500, 500));
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            lblImage.setText("");
            lblImage.setIcon(new ImageIcon(bufImage));
            pack();
            setLocationRelativeTo(null);
            imageOpened = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
