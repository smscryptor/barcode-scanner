package com.aevi.barcode.scanner;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import net.sourceforge.zbar.*;

public class CameraScanner implements ScanningView {

    private static final String TAG = CameraScanner.class.getSimpleName();

    private static final int AUTOFOCUS_UPDATE_RATE = 1000;
    private static final int SCAN_QUALITY_THRESHOLD = 4;

    private final Context context;

    private FrameLayout cameraPreviewHolder;
    private Handler autoFocusHandler = new Handler();
    private ImageScanner scanner;
    private Camera camera;
    private CameraPreview cameraPreview;
    private boolean previewing = false;
    private OnBarcodeListener barcodeListener;

    public CameraScanner(Context context) {
        this.context = context;
    }

    public void setupCamera(FrameLayout preview) throws CameraSetupException {

        cameraPreviewHolder = preview;

        getCameraInstance();
    }

    public void getCameraInstance() throws CameraSetupException {
        int num = Camera.getNumberOfCameras();
        if (num > 0) {
            safeCameraOpen(0);
            return;
        }

        throw new CameraSetupException("No cameras found");
    }

    private void safeCameraOpen(final int id) throws CameraSetupException {

        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);

            if (camera != null) {
                scanner = new ImageScanner();
                scanner.setConfig(0, Config.X_DENSITY, 3);
                scanner.setConfig(0, Config.Y_DENSITY, 3);
            }

            cameraPreview = new CameraPreview(context, camera, previewCb, autoFocusCB);
            cameraPreviewHolder.addView(cameraPreview);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to connect to camera", e);
            throw new CameraSetupException("Failed to connect to camera", e);
        }
    }

    private void releaseCameraAndPreview() {
        if (cameraPreview != null) {
            cameraPreview.setCamera(null);
            scanner = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void releaseCamera() {
        if (camera != null) {
            previewing = false;
            camera.stopPreview();
            camera.setPreviewCallback(null);
            releaseCameraAndPreview();
        }
    }

    protected boolean notifyListener(String data) {
        if (barcodeListener != null) {
            return barcodeListener.gotBarcode(data);
        }
        return false;
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing) {
                camera.autoFocus(autoFocusCB);
            }
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    if (sym.getType() == Symbol.QRCODE) {
                        if (notifyListener(sym.getData())) {
                            previewing = false;
                            camera.setPreviewCallback(null);
                            camera.stopPreview();
                        }
                    }
                }
            }
        }
    };


    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, AUTOFOCUS_UPDATE_RATE);
        }
    };

    public void setBarcodeListener(OnBarcodeListener listener) {
        barcodeListener = listener;
    }
}
