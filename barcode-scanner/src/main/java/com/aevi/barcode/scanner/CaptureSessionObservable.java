/*
 * Copyright (c) 2019 AEVI International GmbH. All rights reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
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
