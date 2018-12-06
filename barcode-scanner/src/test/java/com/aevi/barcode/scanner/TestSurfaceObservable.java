package com.aevi.barcode.scanner;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.reactivex.observers.TestObserver;

@RunWith(MockitoJUnitRunner.class)
public class TestSurfaceObservable {

    @Mock
    WindowManager windowManager;
    @Mock
    TextureView textureView;
    @Mock
    SurfaceTexture surfaceTexture;
    @Mock
    Surface surface;
    @Mock
    Handler mainHandler;
    @Captor
    ArgumentCaptor<TextureView.SurfaceTextureListener> captor;

    final RxErrorHandler rxErrorHandler = new RxErrorHandler();

    private final SurfaceObservable.SurfaceFactory surfaceFactory = Mockito.spy(new SurfaceObservable.SurfaceFactory() {
        @Override
        public Surface create(SurfaceTexture surfaceTexture) {
            return surface;
        }
    });

    @Before
    public void setup() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(mainHandler).post(Mockito.any(Runnable.class));
    }

    @After
    public void tearDown() {
        rxErrorHandler.assertNoErrors();
    }

    @Test
    public void doEmitSurfaceAvailable() {
        TestObserver<Surface> observer = SurfaceObservable.create(windowManager, textureView, mainHandler, surfaceFactory).test();

        Mockito.verify(textureView).setSurfaceTextureListener(captor.capture());
        captor.getValue().onSurfaceTextureAvailable(surfaceTexture, 100, 100);
        observer.dispose();

        observer.assertValue(surface);
        Mockito.verify(surface).release();
    }

    @Test
    public void doEmitSurfaceIfAlreadyAvailable() {
        Mockito.doReturn(true).when(textureView).isAvailable();
        TestObserver<Surface> observer = SurfaceObservable.create(windowManager, textureView, mainHandler, surfaceFactory).test();

        Mockito.verify(textureView).setSurfaceTextureListener(captor.capture());
        observer.dispose();

        observer.assertValue(surface);
        Mockito.verify(surface).release();
    }

    @Test
    public void doHandleDisposeBeforeSurfaceAvailability() {
        TestObserver<Surface> observer = SurfaceObservable.create(windowManager, textureView, mainHandler, surfaceFactory).test();

        Mockito.verify(textureView).setSurfaceTextureListener(captor.capture());
        observer.dispose();
        captor.getValue().onSurfaceTextureAvailable(surfaceTexture, 100, 100);

        observer.assertNoValues();
        Mockito.verify(surfaceFactory, Mockito.never()).create(surfaceTexture);
    }

    @Test
    public void doCompleteUponSurfaceDestruction() {
        TestObserver<Surface> observer = SurfaceObservable.create(windowManager, textureView, mainHandler, surfaceFactory).test();

        Mockito.verify(textureView).setSurfaceTextureListener(captor.capture());
        ;
        captor.getValue().onSurfaceTextureAvailable(surfaceTexture, 100, 100);
        captor.getValue().onSurfaceTextureDestroyed(surfaceTexture);

        observer.assertValue(surface);
        Mockito.verify(surface).release();
        observer.assertComplete();
    }
}
