package com.aevi.barcode.scanner;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Handler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.observers.TestObserver;

@RunWith(MockitoJUnitRunner.class)
public class TestCameraObservable {

    private static final String[] CAMERA_LIST = {
            "backCamera",
            "frontCamera"
    };

    @Mock
    CameraManager cameraManager;
    @Mock
    CameraDevice cameraDevice;
    @Captor
    ArgumentCaptor<CameraDevice.StateCallback> captor;

    final RxErrorHandler rxErrorHandler = new RxErrorHandler();

    @Before
    public void setup() throws CameraAccessException {
        Mockito.doReturn(CAMERA_LIST).when(cameraManager).getCameraIdList();

    }

    @After
    public void tearDown() {
        Mockito.verify(cameraDevice).close();
        rxErrorHandler.assertNoErrors();
    }

    @Test
    public void doEmitCameraOpening() throws CameraAccessException {
        TestObserver<CameraDevice> observer = CameraObservable.create(cameraManager).test();

        Mockito.verify(cameraManager).openCamera(Mockito.eq(CAMERA_LIST[0]), captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onOpened(cameraDevice);
        observer.dispose();

        observer.assertValue(cameraDevice);
    }

    @Test
    public void doHandleDisposeBeforeCameraOpening() throws CameraAccessException {
        TestObserver<CameraDevice> observer = CameraObservable.create(cameraManager).test();

        Mockito.verify(cameraManager).openCamera(Mockito.eq(CAMERA_LIST[0]), captor.capture(), Mockito.nullable(Handler.class));
        observer.dispose();
        captor.getValue().onOpened(cameraDevice);

        observer.assertNoValues();
    }

    @Test
    public void doCompleteUponCameraDisconnection() throws CameraAccessException {
        TestObserver<CameraDevice> observer = CameraObservable.create(cameraManager).test();

        Mockito.verify(cameraManager).openCamera(Mockito.eq(CAMERA_LIST[0]), captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onOpened(cameraDevice);
        captor.getValue().onDisconnected(cameraDevice);

        observer.assertValues(cameraDevice);
        observer.assertComplete();
    }

    @Test
    public void doEmitErrorUponCameraError() throws CameraAccessException {
        TestObserver<CameraDevice> observer = CameraObservable.create(cameraManager).test();

        Mockito.verify(cameraManager).openCamera(Mockito.eq(CAMERA_LIST[0]), captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onOpened(cameraDevice);
        captor.getValue().onError(cameraDevice, 1);

        observer.assertValues(cameraDevice);
        observer.assertError(Exception.class);
    }
}
