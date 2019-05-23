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
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;

public class CameraFrameObservable {

    public interface ImageReaderFactory {

        ImageReader create(int width, int height, int maxImages);
    }

    public static Observable<Image> create(CameraManager cameraManager, Observable<CameraDevice> cameraObservable,
                                           Observable<Surface> surfaceTextureObservable, ImageReaderFactory imageReaderFactory, Scheduler scheduler) {
        return cameraObservable.zipWith(surfaceTextureObservable, (cameraDevice, surface) -> Tuple.of(cameraDevice, surface))
                .concatMap(params -> {
                    CameraDevice camera = params.t1;
                    Surface surface = params.t2;
                    Size imageReaderSize = findOptimalSize(cameraManager, camera, 1080, 720, 0d);
                    final ImageReader imageReader = imageReaderFactory.create(imageReaderSize.getWidth(), imageReaderSize.getHeight(), 3);
                    return CaptureSessionObservable.create(camera, Arrays.asList(surface, imageReader.getSurface()))
                            .concatMap((Function<CameraCaptureSession, ObservableSource<ImageReader>>) cameraCaptureSession -> {
                                        CaptureRequest.Builder captureRequestBuilder =
                                                cameraCaptureSession.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                        captureRequestBuilder.addTarget(surface);
                                        captureRequestBuilder.addTarget(imageReader.getSurface());
                                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                        return Observable.just(imageReader);
                                    }
                            );
                }).flatMap((Function<ImageReader, ObservableSource<Image>>) imageReader -> ImageObservable.create(imageReader, 500, scheduler));
    }

    private static Size findOptimalSize(CameraManager cameraManager, CameraDevice cameraDevice, int width, int height, double ratioDelta)
            throws CameraAccessException {
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
