package com.aevi.barcode.scanner;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;

public class CameraWrapper extends CameraDevice.StateCallback {

    static final String TAG = CameraWrapper.class.getSimpleName();

    interface CallBack {
        void onCameraOpened();

        void onCameraClosed();
    }

    private static CameraWrapper singleton;

    public static synchronized CameraWrapper getInstance(Context context) {
        if (singleton == null) {
            singleton = new CameraWrapper(context);
        }
        return singleton;
    }

    private final CameraManager cameraManager;
    private CallBack callBack;
    private CameraDevice camera;
    private boolean opening = false;
    private boolean shouldClose = false;

    private CameraWrapper(Context context) {
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public void onOpened(CameraDevice cameraDevice) {
        opening = false;
        this.camera = cameraDevice;
        if (shouldClose) {
            stop();
        } else {
            callBack.onCameraOpened();
        }
    }

    @Override
    public void onDisconnected(CameraDevice cameraDevice) {
        opening = false;
        this.camera = cameraDevice;
        stop();
    }

    @Override
    public void onError(CameraDevice cameraDevice, int i) {
        opening = false;
        this.camera = cameraDevice;
        stop();
    }

    public void start(CallBack callBack) {
        shouldClose = false;
        this.callBack = callBack;
        if (camera == null && !opening) {
            try {
                cameraManager.openCamera(cameraManager.getCameraIdList()[0], this, null);
                opening = true;
            } catch (CameraAccessException e) {
                Log.e(TAG, "An error occured while opening the camera");
                callBack.onCameraClosed();
            }
        } else {
            onOpened(camera);
        }
    }

    public void stop() {
        if (camera != null) {
            camera.close();
            camera = null;
            shouldClose = false;
            callBack.onCameraClosed();
        } else {
            shouldClose = true;
        }
    }

    public boolean isAvailable() {
        return camera != null;
    }

    public CameraDevice getCamera() {
        return camera;
    }

    public Size findOptimaleSize(int width, int height, double ratioDelta) throws CameraAccessException {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(camera.getId());
        StreamConfigurationMap configuration = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = configuration.getOutputSizes(SurfaceTexture.class);
        Size optimal = sizes[0];
        for (Size size : sizes) {
            if (size.getWidth() >= width && size.getHeight() >= height) {
                // Choose the smallest size which matches our criteria
                if (optimal.getWidth() * optimal.getHeight() > size.getWidth() * size.getHeight()) {
                    optimal = size;
                }
                // Fallback to the one with the more pixels
            } else if (optimal.getWidth() * optimal.getHeight() < size.getWidth() * size.getHeight()) {
                optimal = size;
            }
        }
        return optimal;
    }
}
