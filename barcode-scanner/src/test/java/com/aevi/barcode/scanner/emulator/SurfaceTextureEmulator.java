package com.aevi.barcode.scanner.emulator;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.aevi.barcode.scanner.SurfaceObservable;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class SurfaceTextureEmulator {

    private final TextureView textureView = Mockito.mock(TextureView.class);
    private final SurfaceTexture surfaceTexture = Mockito.mock(SurfaceTexture.class);
    private final Surface surface = Mockito.mock(Surface.class);
    private final ArgumentCaptor<TextureView.SurfaceTextureListener> captor = ArgumentCaptor.forClass(TextureView.SurfaceTextureListener.class);
    private final SurfaceObservable.SurfaceFactory surfaceFactory = Mockito.spy(new SurfaceObservable.SurfaceFactory() {
        @Override
        public Surface create(SurfaceTexture surfaceTexture) {
            return surface;
        }
    });


    public TextureView getTextureView() {
        return textureView;
    }

    public SurfaceObservable.SurfaceFactory getSurfaceFactory() {
        return surfaceFactory;
    }

    public Surface getSurface() {
        return surface;
    }

    public void setAvailability(boolean value) {
        Mockito.doReturn(value).when(textureView).isAvailable();
    }

    public void onSurfaceTextureAvailable() {
        Mockito.verify(textureView).setSurfaceTextureListener(captor.capture());
        captor.getValue().onSurfaceTextureAvailable(surfaceTexture, 100, 100);
    }

    public void onSurfaceTextureDestroyed() {
        captor.getValue().onSurfaceTextureDestroyed(surfaceTexture);
    }

    public void assertNoSurfaceCreated() {
        Mockito.verify(surfaceFactory, Mockito.never()).create(surfaceTexture);
    }
}
