package com.aevi.barcode.scanner;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Camera2Preview extends TextureView {

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
        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        return CameraFrameObservable.create(cameraManager,
                CameraObservable.create(cameraManager),
                SurfaceObservable.create(windowManager, this, new Handler(Looper.getMainLooper()), surfaceTexture -> new Surface(surfaceTexture)),
                (width, height, maxImages) -> ImageReader.newInstance(width, height, imageFormat, maxImages), Schedulers.computation());
    }
}
