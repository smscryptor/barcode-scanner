package com.aevi.barcode.scanner;

import android.graphics.ImageFormat;
import android.media.Image;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.nio.ByteBuffer;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class BarcodeScanner implements ObservableTransformer<Image, String> {

    public static final int IMAGE_FORMAT = ImageFormat.YUV_420_888;

    private final ImageScanner scanner;
    private byte[] buffer;

    public BarcodeScanner() {
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
    }

    @Override
    public ObservableSource<String> apply(Observable<Image> upstream) {
        return upstream.flatMap(image -> {
            final String data = scan(image);
            return data != null ? Observable.just(data) : Observable.never();
        });
    }

    private String scan(Image image) {
        return checkForBarcodeData(fetchMonochromeData(image), image.getWidth(), image.getHeight());
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
