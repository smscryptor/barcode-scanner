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

import android.annotation.SuppressLint;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class CameraObservable extends CameraDevice.StateCallback implements ObservableOnSubscribe<CameraDevice> {

    public static Observable<CameraDevice> create(CameraManager cameraManager) {
        return Observable.create(new CameraObservable(cameraManager));
    }

    private final CameraManager cameraManager;
    private volatile CameraDevice cameraDevice;
    private volatile ObservableEmitter<CameraDevice> observableEmitter;

    private CameraObservable(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void subscribe(ObservableEmitter<CameraDevice> emitter) throws Exception {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(() -> {
                if (cameraDevice != null) {
                    cameraDevice.close();
                }
            });
            cameraManager.openCamera(cameraManager.getCameraIdList()[0], this, null);

        }
    }

    @Override
    public void onOpened(CameraDevice cameraDevice) {
        this.cameraDevice = cameraDevice;
        observableEmitter.onNext(cameraDevice);
        if (observableEmitter != null && observableEmitter.isDisposed()) {
            cameraDevice.close();
        }
    }

    @Override
    public void onDisconnected(CameraDevice cameraDevice) {
        observableEmitter.onComplete();
    }

    @Override
    public void onError(CameraDevice cameraDevice, int i) {
        observableEmitter.tryOnError(new Exception("Unable to open camera, return code: " + i));
        cameraDevice.close();
    }
}
