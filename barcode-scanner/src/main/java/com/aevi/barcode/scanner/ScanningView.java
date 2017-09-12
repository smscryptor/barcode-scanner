package com.aevi.barcode.scanner;

import android.widget.FrameLayout;

/**
 * An interface that objects must implement in order to perform the role of scanning barcodes
 * as well as displaying a camera preview in the given {@link FrameLayout} view object.
 */
public interface ScanningView {

    class CameraSetupException extends Exception {
        public CameraSetupException(String message) {
            super(message);
        }
        public CameraSetupException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    interface OnBarcodeListener {
        boolean gotBarcode(String data);
    }

    /**
     * Start the camera and preview running
     *
     * @param preview A {@link FrameLayout} where the camera scanning view should be displayed
     *
     * @throws CameraSetupException Thrown if anything goes wrong during setup
     */
    void setupCamera(FrameLayout preview) throws CameraSetupException;

    /**
     * Release the camera and all resources associated with it
     */
    void releaseCamera();

    /**
     * Add a listener that is informed of the result of any barcode data detected in the cameras view
     *
     * @param listener A listener that will be called back with the barcode data
     */
    void setBarcodeListener(OnBarcodeListener listener);

}
