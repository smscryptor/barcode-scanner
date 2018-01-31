package com.aevi.barcode.scanner;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.util.Arrays;

public class Camera2Preview extends TextureView implements CameraWrapper.CallBack {

    static final String TAG = Camera2Preview.class.getSimpleName();

    private CameraWrapper cameraWrapper;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener onImageAvailableListener;
    private Surface surface;
    private HandlerThread thread;
    private Handler handler;

    SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            startCaptureSession();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };


    CameraCaptureSession.StateCallback captureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            try {
                if (cameraWrapper.isAvailable()) {
                    captureSession = cameraCaptureSession;
                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                }
            } catch (CameraAccessException e) {
                Log.e(TAG, "An error occured while capturing", e);
                stop();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.e(TAG, "Failed to configure the capture session");
            stop();
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
        cameraWrapper = CameraWrapper.getInstance(getContext());
        setSurfaceTextureListener(surfaceTextureListener);
        thread = new HandlerThread(Camera2Preview.class.getSimpleName());
        thread.start();
        handler = new Handler(thread.getLooper());
        cameraWrapper.start(this);
    }

    public void stop() {
        setSurfaceTextureListener(null);
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraWrapper != null) {
            cameraWrapper.stop();
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

    @Override
    public void onCameraOpened() {
        startCaptureSession();
    }

    @Override
    public void onCameraClosed() {
        stop();
    }

    private void startCaptureSession() {
        if (isAvailable() && cameraWrapper.isAvailable()) {
            try {
                Size imageReaderSize = cameraWrapper.findOptimaleSize(1080, 720, 0d);
                imageReader = ImageReader.newInstance(imageReaderSize.getWidth(), imageReaderSize.getHeight(), ImageFormat.YUV_420_888, 2);
                imageReader.setOnImageAvailableListener(onImageAvailableDelegator, handler);

                Size textureSize = cameraWrapper.findOptimaleSize(getWidth() / 2, getHeight() / 2, 0d);
                surface = new Surface(getSurfaceTexture());
                getSurfaceTexture().setDefaultBufferSize(textureSize.getWidth(), textureSize.getHeight());
                captureRequestBuilder = cameraWrapper.getCamera().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);
                captureRequestBuilder.addTarget(imageReader.getSurface());
                cameraWrapper.getCamera().createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), captureSessionStateCallback, null);
                transform();
            } catch (CameraAccessException e) {
                Log.e(TAG, "An error occured while attempting to start the capture session.", e);
                stop();
            }
        }
    }

    private void transform() {
        int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        matrix.postRotate(-90 * rotation, getWidth() / 2f, getHeight() / 2f);
        setTransform(matrix);
    }
}
