package com.aevi.barcode.scanner;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

public class SurfaceObservable implements TextureView.SurfaceTextureListener, ObservableOnSubscribe<Surface> {

    public interface SurfaceFactory {

        Surface create(SurfaceTexture surfaceTexture);
    }

    public static Observable<Surface> create(WindowManager windowManager, TextureView textureView) {
        return create(windowManager, textureView, new Handler(Looper.getMainLooper()), new SurfaceFactory() {
            @Override
            public Surface create(SurfaceTexture surfaceTexture) {
                return new Surface(surfaceTexture);
            }
        });
    }

    protected static Observable<Surface> create(WindowManager windowManager, TextureView textureView,
                                                Handler mainHandler, SurfaceFactory surfaceFactory) {
        return Observable.create(new SurfaceObservable(windowManager, textureView, mainHandler, surfaceFactory));
    }

    private final WindowManager windowManager;
    private final TextureView textureView;
    private final SurfaceFactory surfaceFactory;
    private final Handler mainHandler;
    private volatile Surface surface;
    private volatile ObservableEmitter<Surface> observableEmitter;
    private int currentRotation = 0;

    private SurfaceObservable(WindowManager windowManager, TextureView textureView, Handler mainHandler, SurfaceFactory surfaceFactory) {
        this.windowManager = windowManager;
        this.textureView = textureView;
        this.mainHandler = mainHandler;
        this.surfaceFactory = surfaceFactory;
    }

    @Override
    public void subscribe(ObservableEmitter<Surface> emitter) {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() {
                    textureView.setSurfaceTextureListener(null);
                    if (surface != null) {
                        surface.release();
                    }
                }
            });
            textureView.setSurfaceTextureListener(this);
            if (textureView.isAvailable()) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSurfaceTextureAvailable(textureView.getSurfaceTexture(), 0, 0);
                    }
                });
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (this.observableEmitter != null && !this.observableEmitter.isDisposed()) {
            surface = surfaceFactory.create(surfaceTexture);
            observableEmitter.onNext(surface);
        } else {
            textureView.setSurfaceTextureListener(null);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        observableEmitter.onComplete();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if (rotation != currentRotation) {
            currentRotation = rotation;
            Matrix matrix = new Matrix();
            matrix.postRotate(-90 * currentRotation, textureView.getWidth() / 2f, textureView.getHeight() / 2f);
            textureView.setTransform(matrix);
        }
    }
}
