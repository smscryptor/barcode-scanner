package com.aevi.barcode.scanner;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.Arrays;

public class Camera2Preview extends TextureView {

    static final String TAG = Camera2Preview.class.getSimpleName();

    private CameraDevice camera;
    private Size[] sizes;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener onImageAvailableListener;
    private Surface surface;
    private HandlerThread thread;
    private Handler handler;

    SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            start();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            try {
                camera = cameraDevice;
                thread = new HandlerThread(Camera2Preview.class.getSimpleName());
                thread.start();
                handler = new Handler(thread.getLooper());
                Size imageReaderSize = findOptimaleSize(sizes, 1080, 720, 0d);
                imageReader = ImageReader.newInstance(imageReaderSize.getWidth(), imageReaderSize.getHeight(), ImageFormat.YUV_420_888, 2);
                imageReader.setOnImageAvailableListener(onImageAvailableDelegator, handler);

                Size textureSize = findOptimaleSize(sizes, getWidth() / 2, getHeight() / 2, 0d);
                surface = new Surface(getSurfaceTexture());
                getSurfaceTexture().setDefaultBufferSize(textureSize.getWidth(), textureSize.getHeight());
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);
                captureRequestBuilder.addTarget(imageReader.getSurface());
                cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), captureSessionStateCallback, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "An error occured while opening a capture session", e);
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {

        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {

        }
    };

    CameraCaptureSession.StateCallback captureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            try {
                captureSession = cameraCaptureSession;
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "An error occured while capturing", e);
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

        }
    };

    ImageReader.OnImageAvailableListener onImageAvailableDelegator = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            if (onImageAvailableListener != null) {
                onImageAvailableListener.onImageAvailable(imageReader);
            }
        }
    };

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

    public void setOnImageAvailableListener(ImageReader.OnImageAvailableListener onImageAvailableListener) {
        this.onImageAvailableListener = onImageAvailableListener;
    }

    public void start() {
        if (isAvailable()) {
            CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap configuration = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                sizes = configuration.getOutputSizes(SurfaceTexture.class);
                cameraManager.openCamera(cameraId, cameraCallback, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "An error occured while opening the camera", e);
            }
        } else {
            setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    public void stop() {
        setSurfaceTextureListener(null);
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (camera != null) {
            camera.close();
            camera = null;
        }
        if (imageReader != null) {
            imageReader.getSurface().release();
            imageReader.close();
            imageReader = null;
        }
        if (surface != null) {
            surface.release();
            surface = null;
        }
        if (thread != null) {
            thread.quit();
            thread = null;
        }
    }

    private Size findOptimaleSize(Size[] sizes, int width, int height, double ratioDelta) {
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
