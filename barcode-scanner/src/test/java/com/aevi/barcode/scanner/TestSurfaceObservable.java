package com.aevi.barcode.scanner;

import android.view.Surface;
import android.view.WindowManager;

import com.aevi.barcode.scanner.emulator.SurfaceTextureEmulator;

import org.junit.Test;
import org.mockito.Mockito;

import io.reactivex.observers.TestObserver;

public class TestSurfaceObservable extends BaseTest {

    private final WindowManager windowManager = Mockito.mock(WindowManager.class);
    private final SurfaceTextureEmulator emulator = new SurfaceTextureEmulator();

    @Test
    public void doEmitSurfaceAvailable() {
        TestObserver<Surface> observer = SurfaceObservable.create(
                windowManager, emulator.getTextureView(), mainHandler, emulator.getSurfaceFactory()).test();

        emulator.onSurfaceTextureAvailable();
        observer.dispose();

        observer.assertValue(emulator.getSurface());
        Mockito.verify(emulator.getSurface()).release();
    }

    @Test
    public void doEmitSurfaceIfAlreadyAvailable() {
        emulator.setAvailability(true);
        TestObserver<Surface> observer = SurfaceObservable.create(
                windowManager, emulator.getTextureView(), mainHandler, emulator.getSurfaceFactory()).test();

        observer.dispose();

        observer.assertValue(emulator.getSurface());
        Mockito.verify(emulator.getSurface()).release();
    }

    @Test
    public void doHandleDisposeBeforeSurfaceAvailability() {
        TestObserver<Surface> observer =
                SurfaceObservable.create(windowManager, emulator.getTextureView(), mainHandler, emulator.getSurfaceFactory()).test();

        observer.dispose();

        observer.assertNoValues();
        emulator.assertNoSurfaceCreated();
    }

    @Test
    public void doHandleNullSurfaceTexture() {
        TestObserver<Surface> observer = SurfaceObservable.create(
                windowManager, emulator.getTextureView(), mainHandler, emulator.getSurfaceFactory()).test();

        emulator.onNullSurfaceTextureAvailable();
        observer.dispose();

        observer.assertNoValues();
    }

    @Test
    public void doCompleteUponSurfaceDestruction() {
        TestObserver<Surface> observer =
                SurfaceObservable.create(windowManager, emulator.getTextureView(), mainHandler, emulator.getSurfaceFactory()).test();

        emulator.onSurfaceTextureAvailable();
        emulator.onSurfaceTextureDestroyed();

        observer.assertValue(emulator.getSurface());
        Mockito.verify(emulator.getSurface()).release();
        observer.assertComplete();
    }
}
