package com.aevi.barcode.scanner;

import android.hardware.camera2.CameraDevice;

import com.aevi.barcode.scanner.emulator.CameraEmulator;

import org.junit.Test;
import org.mockito.Mockito;

import io.reactivex.observers.TestObserver;

public class TestCameraObservable extends BaseTest {

    private final CameraEmulator emulator = new CameraEmulator(CameraEmulator.CAMERA_LIST_SAMPLE);

    @Test
    public void doEmitCameraOpening() {
        TestObserver<CameraDevice> observer = CameraObservable.create(emulator.getCameraManager()).test();

        CameraDevice cameraDevice = emulator.onOpened(CameraEmulator.CAMERA_LIST_SAMPLE[0]);
        observer.dispose();

        observer.assertValue(cameraDevice);
        Mockito.verify(cameraDevice).close();
    }

    @Test
    public void doHandleDisposeBeforeCameraOpening() {
        TestObserver<CameraDevice> observer = CameraObservable.create(emulator.getCameraManager()).test();

        observer.dispose();
        CameraDevice cameraDevice = emulator.onOpened(CameraEmulator.CAMERA_LIST_SAMPLE[0]);

        observer.assertNoValues();
        Mockito.verify(cameraDevice).close();
    }

    @Test
    public void doCompleteUponCameraDisconnection() {
        TestObserver<CameraDevice> observer = CameraObservable.create(emulator.getCameraManager()).test();

        CameraDevice cameraDevice = emulator.onOpened(CameraEmulator.CAMERA_LIST_SAMPLE[0]);
        emulator.onDisconnected(cameraDevice);

        observer.assertValues(cameraDevice);
        observer.assertComplete();
        Mockito.verify(cameraDevice).close();
    }

    @Test
    public void doEmitErrorUponCameraError() {
        TestObserver<CameraDevice> observer = CameraObservable.create(emulator.getCameraManager()).test();

        CameraDevice cameraDevice = emulator.onOpened(CameraEmulator.CAMERA_LIST_SAMPLE[0]);
        emulator.onError(cameraDevice, 1);

        observer.assertValues(cameraDevice);
        observer.assertError(Exception.class);
        Mockito.verify(cameraDevice, Mockito.times(2)).close();
    }

    @Test
    public void doHandleDisposeBeforeCameraError() {
        TestObserver<CameraDevice> observer = CameraObservable.create(emulator.getCameraManager()).test();

        CameraDevice cameraDevice = emulator.onOpened(CameraEmulator.CAMERA_LIST_SAMPLE[0]);
        observer.dispose();
        emulator.onError(cameraDevice, 1);

        observer.assertValues(cameraDevice);
        Mockito.verify(cameraDevice, Mockito.times(2)).close();
    }
}
