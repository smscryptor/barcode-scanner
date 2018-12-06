package com.aevi.barcode.scanner;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.view.Surface;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

public class CaptureSessionObservable extends CameraCaptureSession.StateCallback implements ObservableOnSubscribe<CameraCaptureSession> {

    public static Observable<CameraCaptureSession> create(CameraDevice cameraDevice, List<Surface> surfaces) {
        return Observable.create(new CaptureSessionObservable(cameraDevice, surfaces));
    }

    private final CameraDevice cameraDevice;
    private final List<Surface> surfaces;
    private volatile CameraCaptureSession captureSession;
    private volatile ObservableEmitter<CameraCaptureSession> observableEmitter;

    private CaptureSessionObservable(CameraDevice cameraDevice, List<Surface> surfaces) {
        this.cameraDevice = cameraDevice;
        this.surfaces = surfaces;
    }

    @Override
    public void subscribe(ObservableEmitter<CameraCaptureSession> emitter) throws Exception {
        observableEmitter = emitter;
        if (!emitter.isDisposed()) {
            observableEmitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() throws Exception {
                    if (captureSession != null) {
                        captureSession.close();
                    }
                }
            });

            cameraDevice.createCaptureSession(surfaces, this, null);
        }
    }

    @Override
    public void onConfigured(CameraCaptureSession cameraCaptureSession) {
        captureSession = cameraCaptureSession;
        if (this.observableEmitter != null && !this.observableEmitter.isDisposed()) {
            observableEmitter.onNext(captureSession);
        } else {
            captureSession.close();
        }
    }

    @Override
    public void onClosed(CameraCaptureSession session) {
        observableEmitter.onComplete();
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
        observableEmitter.onError(new Exception("Failed to configure capture session"));
    }
}
