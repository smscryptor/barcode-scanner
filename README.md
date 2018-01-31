# Barcode scanner
A wrapper around the zbar library to easily integrate QR code scanning into your Android applications. The zbar sources are based on the most up to date revision found at: http://zbar.hg.sourceforge.net:8000/hgroot/zbar/zbar (changeset _362:38e78368283d_)

## Build
In order to build this library, the following Android SDK components are required:
* ndk-bundle
* cmake

The project can be built using the standard gradle build process:
`./gradlew assembleRelease`

## Usage
Start by defining the dependency in your _build.gradle_ file:
`compile 'com.aevi.barcode:barcode-scanner:1.0.0'`

### Camera preview
To create a camera preview in your activity / fragment, simply use the dedicated view, you can even embed it directly in your layout file:
```xml
<com.aevi.barcode.scanner.Camera2Preview
        android:id="@+id/camera_preview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

In order to get the preview working you will still need to start it manually and also stop it when not required:
```java
@Override
public void onResume() {
    super.onResume();
    camera2Preview.start();
}
@Override
public void onPause() {
    super.onPause();
    camera2Preview.stop();
}
```

### Barcode scanning
Once you have a camere preview, it is pretty straightforward to scan barcodes, just create a barcode scanner object , passing the preview you have created earlier along with a barcode listener:
```java
BarcodeScanner barcodeScanner = new BarcodeScanner(};
OnBarcodeScannedListener listener = new OnBarcodeScannedListener() {
        @Override
        boolean onBarcodeScanned(String data) {
            // runs in the main thread, return true if the scanned information
            // is valid and no subsequent call should be made to this listener
        }
});

@Override
public void onResume() {
    super.onResume();
    // startScanning will automatically start the camera preview
    barcodeScanner.startScanning(camera2Preview, listener);
}

@Override
public void onPause() {
    super.onPause();
    // stopScanning will automatically stop the camera preview
    barcodeScanner.stopScanning();
}
```
