package mk.ukim.finki.is.gui;

import mk.ukim.finki.is.ShootingTargetDetection;

class ImageOpener extends Thread {

    String path;
    ImageOpeningFinished iof;

    public ImageOpener(String path, ImageOpeningFinished iof) {
        this.path = path;
        this.iof = iof;
    }

    @Override
    public void run() {
        ShootingTargetDetection std = new ShootingTargetDetection(path);
        iof.imageOpeningFinished(std);
    }
}

