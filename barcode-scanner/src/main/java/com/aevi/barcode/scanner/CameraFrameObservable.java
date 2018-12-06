package com.aevi.barcode.scanner;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class CameraFrameObservable implements ObservableOnSubscribe<Image> {

    public static Observable<Image> create(CameraManager cameraManager, WindowManager windowManager, TextureView textureView) {
        return Observable.create(new CameraFrameObservable(cameraManager, windowManager, textureView));
    }

    private final CameraManager cameraManager;
    private final WindowManager windowManager;
    private final TextureView textureView;
    private volatile ObservableEmitter<Image> observableEmitter;
    private volatile Disposable disposable;

    public CameraFrameObservable(CameraManager cameraManager, WindowManager windowManager, TextureView textureView) {
        this.cameraManager = cameraManager;
        this.windowManager = windowManager;
        this.textureView = textureView;
    }

    @Override
    public void subscribe(final ObservableEmitter<Image> emitter) throws Exception {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(new Cancellable() {
                @Override
                public void cancel() throws Exception {
                    disposable.dispose();
                }
            });

            Observable<CameraDevice> cameraObservable = CameraObservable.create(cameraManager);
            Observable<Surface> surfaceTextureObservable = SurfaceObservable.create(windowManager, textureView);

            disposable = cameraObservable.zipWith(surfaceTextureObservable, new BiFunction<CameraDevice, Surface, Pair<CameraDevice, Surface>>() {
                @Override
                public Pair<CameraDevice, Surface> apply(CameraDevice cameraDevice, Surface surface) throws Exception {
                    return Pair.create(cameraDevice, surface);
                }
            }).concatMap(new Function<Pair<CameraDevice, Surface>, ObservableSource<ImageReader>>() {
                @Override
                public ObservableSource<ImageReader> apply(final Pair<CameraDevice, Surface> pair) throws Exception {
                    Size imageReaderSize = findOptimaleSize(pair.first, 1080, 720, 0d);
                    final ImageReader imageReader =
                            ImageReader.newInstance(imageReaderSize.getWidth(), imageReaderSize.getHeight(), ImageFormat.YUV_420_888, 2);
                    return CaptureSessionObservable.create(pair.first, Arrays.asList(pair.second, imageReader.getSurface())).concatMap(
                            new Function<CameraCaptureSession, ObservableSource<ImageReader>>() {
                                @Override
                                public ObservableSource<ImageReader> apply(CameraCaptureSession cameraCaptureSession) throws Exception {
                                    CaptureRequest.Builder captureRequestBuilder =
                                            cameraCaptureSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    captureRequestBuilder.addTarget(pair.second);
                                    captureRequestBuilder.addTarget(imageReader.getSurface());
                                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                    return Observable.just(imageReader);
                                }
                            }
                    );
                }
            }).flatMap(new Function<ImageReader, ObservableSource<Image>>() {
                @Override
                public ObservableSource<Image> apply(ImageReader imageReader) throws Exception {
                    return ImageObservable.create(imageReader, 500, Schedulers.computation());
                }
            }).subscribe(new Consumer<Image>() {
                @Override
                public void accept(Image image) throws Exception {
                    observableEmitter.onNext(image);
                }
            });
        }
    }

    private Size findOptimaleSize(CameraDevice cameraDevice, int width, int height, double ratioDelta) throws CameraAccessException {
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
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
