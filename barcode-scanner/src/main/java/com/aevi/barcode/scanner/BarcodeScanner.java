package com.aevi.barcode.scanner;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.nio.ByteBuffer;

public class BarcodeScanner implements ImageReader.OnImageAvailableListener {

    public interface OnBarcodeScannedListener {
        boolean onBarcodeScanned(String data);
    }

    private final ImageScanner scanner;
    private final Handler mainHandler;
    private Camera2Preview preview;
    private OnBarcodeScannedListener listener;
    private byte[] buffer;


    public BarcodeScanner() {
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        mainHandler = new Handler(Looper.getMainLooper());

    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        Image image = imageReader.acquireLatestImage();
        if (image != null) {
            final String data = checkForBarcodeData(fetchMonochromeData(image), image.getWidth(), image.getHeight());
            image.close();
            if (data != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null && listener.onBarcodeScanned(data)) {
                            stopScanning();
                        }
                    }
                });
            }
        }
    }

    public void startScanning(Camera2Preview preview, OnBarcodeScannedListener listener) {
        this.preview = preview;
        this.listener = listener;
        preview.setOnImageAvailableListener(this);
        preview.start();
    }

    public void stopScanning() {
        if (preview != null) {
            preview.stop();
            preview = null;
        }
        listener = null;
        mainHandler.removeCallbacksAndMessages(null);
    }

    private byte[] fetchMonochromeData(Image image) {
        int size = image.getWidth() * image.getHeight();
        if (buffer == null || buffer.length < size) {
            buffer = new byte[size];
        }
        Image.Plane plane = image.getPlanes()[0];
        ByteBuffer planeBuffer = plane.getBuffer();
        for (int y = 0; y < image.getHeight(); ++y) {
            planeBuffer.position(y * plane.getRowStride());
            planeBuffer.get(buffer, y * image.getWidth(), image.getWidth());
        }
        return buffer;
    }

    private String checkForBarcodeData(byte[] pixels, int width, int height) {
        net.sourceforge.zbar.Image barcode = new net.sourceforge.zbar.Image(width, height, "Y800");
        barcode.setData(pixels);
        int result = scanner.scanImage(barcode);

        if (result != 0) {
            SymbolSet syms = scanner.getResults();
            for (Symbol sym : syms) {
                if (sym.getType() == Symbol.QRCODE) {
                    return sym.getData();
                }
            }
        }
        return null;
    }
}
