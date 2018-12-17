package com.aevi.barcode.scanner;

import android.graphics.ImageFormat;
import android.media.Image;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.nio.ByteBuffer;

public class BarcodeObservable implements ObservableOnSubscribe<String>, Consumer<Image> {

    public static final int IMAGE_FORMAT = ImageFormat.YUV_420_888;

    public static Observable<String> create(Observable<Image> imageObservable) {
        return Observable.create(new BarcodeObservable(imageObservable));
    }

    private final ImageScanner scanner;
    private final Observable<Image> imageObservable;
    private volatile ObservableEmitter<String> observableEmitter;
    private volatile Disposable disposable;
    private byte[] buffer;

    private BarcodeObservable(Observable<Image> imageObservable) {
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        this.imageObservable = imageObservable;
    }

    @Override
    public void subscribe(ObservableEmitter<String> emitter) {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() {
                    disposable.dispose();
                }
            });
            disposable = imageObservable.subscribe(this);
        }
    }

    @Override
    public void accept(Image image) {
        final String data = scan(image);
        if (data != null) {
            observableEmitter.onNext(data);
        }
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
