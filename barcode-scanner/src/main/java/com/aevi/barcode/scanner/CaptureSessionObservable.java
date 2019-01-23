package com.aevi.barcode.scanner;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.view.Surface;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

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
            observableEmitter.setCancellable(() -> {
                if (captureSession != null) {
                    captureSession.close();
                }
            });

            cameraDevice.createCaptureSession(surfaces, this, null);
        }
    }

    @Override
    public void onConfigured(CameraCaptureSession cameraCaptureSession) {
        captureSession = cameraCaptureSession;
        if (observableEmitter != null && !observableEmitter.isDisposed()) {
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
        observableEmitter.tryOnError(new Exception("Failed to configure capture session"));
    }
}
