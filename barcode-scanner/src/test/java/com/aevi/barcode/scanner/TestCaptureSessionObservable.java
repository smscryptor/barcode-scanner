package com.aevi.barcode.scanner;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.view.Surface;

import com.aevi.barcode.scanner.emulator.CameraEmulator;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import io.reactivex.observers.TestObserver;

public class TestCaptureSessionObservable extends BaseTest {

    private final CameraDevice cameraDevice = Mockito.mock(CameraDevice.class);
    private final List<Surface> surfaces = Arrays.asList(Mockito.mock(Surface.class));
    private final CameraEmulator emulator = new CameraEmulator();

    @Test
    public void doEmitCaptureSession() {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        CameraCaptureSession cameraCaptureSession = emulator.onConfigured(cameraDevice, surfaces);
        observer.dispose();

        observer.assertValue(cameraCaptureSession);
        Mockito.verify(cameraCaptureSession).close();
    }

    @Test
    public void doHandleDisposeBeforeCaptureSessionConfigured() {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        observer.dispose();
        CameraCaptureSession cameraCaptureSession = emulator.onConfigured(cameraDevice, surfaces);

        observer.assertNoValues();
        Mockito.verify(cameraCaptureSession).close();
    }

    @Test
    public void doCompleteUponCaptureSessionClosure() {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        CameraCaptureSession cameraCaptureSession = emulator.onConfigured(cameraDevice, surfaces);
        emulator.onClosed(cameraCaptureSession);

        observer.assertValue(cameraCaptureSession);
        observer.onComplete();
        Mockito.verify(cameraCaptureSession).close();
    }

    @Test
    public void doEmitErrorUponConfigurationFailure() {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        /*CameraCaptureSession cameraCaptureSession = emulator.onConfigured(null, surfaces);
        emulator.onConfigureFailed(null, cameraCaptureSession);*/

        emulator.onConfigureFailed(cameraDevice, surfaces);

        observer.assertNoValues();
        observer.assertFailure(Exception.class);
        observer.assertError(Exception.class);
        // Mockito.verify(cameraCaptureSession).close();
    }
}
