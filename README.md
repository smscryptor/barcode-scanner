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
`compile 'com.aevi.barcode:barcode-scanner:2.0.0'`

### Camera preview
To create a camera preview in your activity / fragment, simply use the dedicated `Camera2Preview` view, you can embed it directly in your layout file:
```xml
<com.aevi.barcode.scanner.Camera2Preview
        android:id="@+id/camera_preview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```
In order for the preview to start, you will still need to explictly call the `start()` method of `Camera2Preview` and subscribe to the returned `Observable`. This will return a `Disposable` which you will then be able to use in order to stop the preview. For a concrete example, refer to the next section.

### Barcode scanning
Once you have started the camera preview as described in the previous section, scanning QR codes is relatively simple. Just call `BarcodeObservable.create()` passing the `Observable` returned by the `Camera2Preview.start()` method and you will get notified whenever a valid QR code is scanned.

```java
@Override
public void onResume() {
    super.onResume();
    disposable = BarcodeObservable.create(camera2Preview.start(BarcodeObservable.IMAGE_FORMAT))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String content) throws Exception {
                Log.d("QrActivity", "Scanned QR code: " + content);
            }
        });
}

@Override
public void onPause() {
    super.onPause();
    disposable.dispose();
}
```