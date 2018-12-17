package com.aevi.barcode.scanner;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.view.Surface;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.observers.TestObserver;

@RunWith(MockitoJUnitRunner.class)
public class TestCaptureSessionObservable {

    @Mock
    CameraDevice cameraDevice;
    @Mock
    CameraCaptureSession cameraCaptureSession;
    @Mock
    Surface surface;
    @Captor
    ArgumentCaptor<CameraCaptureSession.StateCallback> captor;

    final List<Surface> surfaces = Arrays.asList(surface);

    final RxErrorHandler rxErrorHandler = new RxErrorHandler();

    @After
    public void tearDown() {
        rxErrorHandler.assertNoErrors();
    }

    @Test
    public void doEmitCaptureSession() throws CameraAccessException {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        Mockito.verify(cameraDevice).createCaptureSession(Mockito.eq(surfaces), captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onConfigured(cameraCaptureSession);
        observer.dispose();

        observer.assertValue(cameraCaptureSession);
        Mockito.verify(cameraCaptureSession).close();
    }

    @Test
    public void doHandleDisposeBeforeCaptureSessionConfigured() throws CameraAccessException {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        Mockito.verify(cameraDevice).createCaptureSession(Mockito.eq(surfaces), captor.capture(), Mockito.nullable(Handler.class));
        observer.dispose();
        captor.getValue().onConfigured(cameraCaptureSession);

        observer.assertNoValues();
        Mockito.verify(cameraCaptureSession).close();
    }

    @Test
    public void doCompleteUponCaptureSessionClosure() throws CameraAccessException {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        Mockito.verify(cameraDevice).createCaptureSession(Mockito.eq(surfaces), captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onConfigured(cameraCaptureSession);
        captor.getValue().onClosed(cameraCaptureSession);

        observer.assertValue(cameraCaptureSession);
        observer.onComplete();
        Mockito.verify(cameraCaptureSession).close();
    }

    @Test
    public void doEmitErrorUponConfigurationFailure() throws CameraAccessException {
        TestObserver<CameraCaptureSession> observer = CaptureSessionObservable.create(cameraDevice, surfaces).test();

        Mockito.verify(cameraDevice).createCaptureSession(Mockito.eq(surfaces), captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onConfigured(cameraCaptureSession);
        captor.getValue().onConfigureFailed(cameraCaptureSession);

        observer.assertValue(cameraCaptureSession);
        observer.assertError(Exception.class);
        Mockito.verify(cameraCaptureSession).close();
    }
}
