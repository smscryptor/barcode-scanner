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
