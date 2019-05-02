package com.aevi.barcode.scanner;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import com.aevi.barcode.scanner.SurfaceTextureObservable.Callback;
import com.aevi.barcode.scanner.Tuple.Tuple2;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;

public class SurfaceObservable implements ObservableOnSubscribe<Surface> {

    public interface SurfaceFactory {
        Surface create(SurfaceTexture surfaceTexture);
    }

    public static Observable<Surface> create(
            Observable<Tuple2<Callback, SurfaceTexture>> surfaceTextureObservable, SurfaceFactory surfaceFactory) {
        return Observable.create(new SurfaceObservable(surfaceTextureObservable, surfaceFactory));
    }

    private final Observable<Tuple2<Callback, SurfaceTexture>> surfaceTextureObservable;
    private final SurfaceFactory surfaceFactory;
    private volatile ObservableEmitter<? super Surface> observableEmitter;
    private volatile Disposable disposable;
    private volatile Surface surface;

    public SurfaceObservable(
            Observable<Tuple2<Callback, SurfaceTexture>> surfaceTextureObservable, SurfaceFactory surfaceFactory) {
        this.surfaceTextureObservable = surfaceTextureObservable;
        this.surfaceFactory = surfaceFactory;
    }

    @Override
    public void subscribe(ObservableEmitter<Surface> emitter) {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(() -> {
                if (surface != null) {
                    surface.release();
                }
                disposable.dispose();
            });

            disposable = surfaceTextureObservable
                    .filter(tuple -> Callback.AVAILABLE.equals(tuple.t1))
                    .map(tuple -> tuple.t2)
                    .subscribe(surfaceTexture -> observableEmitter.onNext(createSurface(surfaceTexture)),
                            throwable -> observableEmitter.onError(throwable));
        }
    }

    private Surface createSurface(SurfaceTexture surfaceTexture) {
        surface = surfaceFactory.create(surfaceTexture);
        return surface;
    }
}
