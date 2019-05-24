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

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import com.aevi.barcode.scanner.SurfaceTextureObservable.Callback;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Camera2Preview extends TextureView {

    private CameraManager cameraManager;
    private WindowManager windowManager;
    private int sensorOrientation = 0;


    public Camera2Preview(Context context) {
        super(context);
    }

    public Camera2Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Camera2Preview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Camera2Preview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Observable<Image> start(int imageFormat) {
        cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Observable<Tuple.Tuple2<Callback, SurfaceTexture>>
                surfaceTextureObservable = SurfaceTextureObservable.create(this, new Handler(Looper.getMainLooper()));

        return CameraFrameObservable.create(cameraManager,
                CameraObservable.create(cameraManager).doOnNext(cameraDevice -> onCameraOpened(cameraDevice)),
                SurfaceObservable.create(surfaceTextureObservable.doOnNext(tuple -> transform(tuple.t1)), surfaceTexture -> new Surface(surfaceTexture)),
                (width, height, maxImages) -> ImageReader.newInstance(width, height, imageFormat, maxImages), Schedulers.computation());
    }

    private void onCameraOpened(CameraDevice cameraDevice) throws CameraAccessException {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) - 90;
        if (sensorOrientation < 0) {
            sensorOrientation = 0;
        }
        transform(Callback.SIZE_CHANGED);
    }

    private void transform(Callback callback) {
        if (Callback.AVAILABLE.equals(callback) || Callback.SIZE_CHANGED.equals(callback)) {
            int rotation = sensorOrientation - 90 * windowManager.getDefaultDisplay().getRotation();
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation, getWidth() / 2f, getHeight() / 2f);
            setTransform(matrix);
        }
    }
}
