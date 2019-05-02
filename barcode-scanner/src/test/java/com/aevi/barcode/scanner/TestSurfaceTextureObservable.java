package com.aevi.barcode.scanner;

import android.graphics.SurfaceTexture;
import com.aevi.barcode.scanner.SurfaceTextureObservable.Callback;
import com.aevi.barcode.scanner.emulator.SurfaceTextureEmulator;
import io.reactivex.observers.TestObserver;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class TestSurfaceTextureObservable extends BaseTest {

    private final SurfaceTextureEmulator emulator = new SurfaceTextureEmulator();

    @Test
    public void doEmitSurfaceAvailable() {
        TestObserver<Tuple.Tuple2<Callback, SurfaceTexture>>
                observer = SurfaceTextureObservable.create(emulator.getTextureView(), mainHandler).test();

        emulator.onSurfaceTextureAvailable();
        observer.dispose();

        assertSurfaceAvailable(observer);
    }

    @Test
    public void doEmitSurfaceIfAlreadyAvailable() {
        emulator.setAvailability(true);
        TestObserver<Tuple.Tuple2<Callback, SurfaceTexture>>
                observer = SurfaceTextureObservable.create(emulator.getTextureView(), mainHandler).test();

        observer.dispose();

        assertSurfaceAvailable(observer);
    }

    @Test
    public void doHandleDisposeBeforeSurfaceAvailability() {
        TestObserver<Tuple.Tuple2<Callback, SurfaceTexture>>
                observer = SurfaceTextureObservable.create(emulator.getTextureView(), mainHandler).test();

        observer.dispose();

        observer.assertNoValues();
    }

    @Test
    public void doHandleNullSurfaceTexture() {
        TestObserver<Tuple.Tuple2<Callback, SurfaceTexture>>
                observer = SurfaceTextureObservable.create(emulator.getTextureView(), mainHandler).test();

        emulator.onNullSurfaceTextureAvailable();
        observer.dispose();

        observer.assertNoValues();
    }

    @Test
    public void doCompleteUponSurfaceDestruction() {
        TestObserver<Tuple.Tuple2<Callback, SurfaceTexture>>
                observer = SurfaceTextureObservable.create(emulator.getTextureView(), mainHandler).test();

        emulator.onSurfaceTextureAvailable();
        emulator.onSurfaceTextureDestroyed();

        assertSurfaceAvailable(observer);
        observer.assertComplete();
    }

    private void assertSurfaceAvailable(
            TestObserver<Tuple.Tuple2<Callback, SurfaceTexture>> testObserver) {
        Assert.assertThat(testObserver.valueCount(), CoreMatchers.is(1));
        Tuple.Tuple2<Callback, SurfaceTexture> tuple = testObserver.values().get(0);
        Assert.assertThat(tuple.t1, CoreMatchers.is(Callback.AVAILABLE));
        Assert.assertThat(tuple.t2, CoreMatchers.is(emulator.getSurfaceTexture()));
    }
}
