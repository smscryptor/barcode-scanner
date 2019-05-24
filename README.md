# Barcode scanner
A wrapper around the zbar library to easily integrate QR code scanning into your Android applications. The zbar sources are based on the most up to date revision found at: http://zbar.hg.sourceforge.net:8000/hgroot/zbar/zbar (changeset _362:38e78368283d_)

## Build
In order to build this library, the following Android SDK components are required:
* ndk-bundle
* cmake

The project can be built using the standard gradle build process:
`./gradlew assembleRelease`

## Usage
In your main gradle.build you'll need to include the following public bintray in your main repositories section:
```
repositories {
    maven {
        url "http://dl.bintray.com/aevi/aevi-uk"
    }
}
```

then, add the dependency to your relevant module _build.gradle_ file:
```implementation 'com.aevi.barcode:barcode-scanner:<version>'```

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
    disposable = camera2Preview.start(BarcodeScanner.IMAGE_FORMAT)
                    .compose(new BarcodeScanner())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(content -> Log.d("QrActivity", "Scanned QR code: " + content));
}

@Override
public void onPause() {
    super.onPause();
    disposable.dispose();
}
```

## License
This library is licensed under the [LGPL license](LICENSE)

Copyright (c) 2019 AEVI International GmbH. All rights reserved