package com.aevi.barcode.scanner;

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
import android.util.Size;
import android.view.Surface;

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class CameraFrameObservable implements ObservableOnSubscribe<Image> {

    public interface ImageReaderFactory {

        ImageReader create(int width, int height, int maxImages);
    }

    public static Observable<Image> create(CameraManager cameraManager, Observable<CameraDevice> cameraObservable,
                                           Observable<Surface> surfaceTextureObservable, ImageReaderFactory imageReaderFactory, Scheduler scheduler) {
        return Observable.create(new CameraFrameObservable(cameraManager, cameraObservable, surfaceTextureObservable, imageReaderFactory, scheduler));
    }

    private class Params {

        final CameraDevice cameraDevice;
        final Surface surface;

        public Params(CameraDevice cameraDevice, Surface surface) {
            this.cameraDevice = cameraDevice;
            this.surface = surface;
        }
    }

    private final CameraManager cameraManager;
    private final Observable<CameraDevice> cameraObservable;
    private final Observable<Surface> surfaceTextureObservable;
    private final ImageReaderFactory imageReaderFactory;
    private final Scheduler scheduler;
    private volatile ObservableEmitter<? super Image> observableEmitter;
    private volatile Disposable disposable;

    public CameraFrameObservable(CameraManager cameraManager, Observable<CameraDevice> cameraObservable, Observable<Surface> surfaceTextureObservable,
                                 ImageReaderFactory imageReaderFactory, Scheduler scheduler) {
        this.cameraManager = cameraManager;
        this.cameraObservable = cameraObservable;
        this.surfaceTextureObservable = surfaceTextureObservable;
        this.imageReaderFactory = imageReaderFactory;
        this.scheduler = scheduler;
    }

    @Override
    public void subscribe(final ObservableEmitter<Image> emitter) {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(() -> disposable.dispose());

            disposable = cameraObservable.zipWith(surfaceTextureObservable, (cameraDevice, surface) -> new Params(cameraDevice, surface))
                    .concatMap((Function<Params, ObservableSource<ImageReader>>) params -> {
                        Size imageReaderSize = findOptimalSize(params.cameraDevice, 1080, 720, 0d);
                        final ImageReader imageReader = imageReaderFactory.create(imageReaderSize.getWidth(), imageReaderSize.getHeight(), 3);
                        return CaptureSessionObservable.create(params.cameraDevice, Arrays.asList(params.surface, imageReader.getSurface()))
                                .concatMap((Function<CameraCaptureSession, ObservableSource<ImageReader>>) cameraCaptureSession -> {
                                            CaptureRequest.Builder captureRequestBuilder =
                                                    cameraCaptureSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                            captureRequestBuilder.addTarget(params.surface);
                                            captureRequestBuilder.addTarget(imageReader.getSurface());
                                            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                            return Observable.just(imageReader);
                                        }
                                );
                    }).flatMap((Function<ImageReader, ObservableSource<Image>>) imageReader
                            -> ImageObservable.create(imageReader, 500, scheduler))
                    .subscribe(image -> observableEmitter.onNext(image), throwable -> observableEmitter.onError(throwable));
        }
    }

    private Size findOptimalSize(CameraDevice cameraDevice, int width, int height, double ratioDelta) throws CameraAccessException {
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
