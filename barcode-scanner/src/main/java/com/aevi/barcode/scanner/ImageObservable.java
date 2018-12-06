package com.aevi.barcode.scanner;

import android.media.Image;
import android.media.ImageReader;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class ImageObservable implements ImageReader.OnImageAvailableListener, ObservableOnSubscribe<ImageReader> {

    public static Observable<Image> create(ImageReader imageReader, long intervalMillis, Scheduler scheduler) {
        Observable<ImageReader> observable = Observable.create(new ImageObservable(imageReader, intervalMillis));
        if (scheduler != null) {
            observable = observable.observeOn(scheduler);
        }
        return observable.concatMap(new Function<ImageReader, ObservableSource<Image>>() {
            @Override
            public ObservableSource<Image> apply(ImageReader imageReader) throws Exception {
                Image image = imageReader.acquireLatestImage();
                return image == null ? Observable.<Image>empty() : Observable.just(image);
            }
        }).doAfterNext(new Consumer<Image>() {
            @Override
            public void accept(Image image) throws Exception {
                image.close();
            }
        });
    }

    public final ImageReader imageReader;
    public final long intervalMillis;
    public long lastEmitted = 0;
    private volatile ObservableEmitter<ImageReader> observableEmitter;

    private ImageObservable(ImageReader imageReader, long intervalMillis) {
        this.imageReader = imageReader;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void subscribe(ObservableEmitter<ImageReader> emitter) throws Exception {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() throws Exception {
                    imageReader.getSurface().release();
                    imageReader.close();
                }
            });
            imageReader.setOnImageAvailableListener(this, null);
        }
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        long millis = System.currentTimeMillis();
        if (millis - lastEmitted > intervalMillis) {
            observableEmitter.onNext(imageReader);
            lastEmitted = millis;
        } else {
            Image image = imageReader.acquireLatestImage();
            if (image != null) {
                image.close();
            }
        }

    }
}
