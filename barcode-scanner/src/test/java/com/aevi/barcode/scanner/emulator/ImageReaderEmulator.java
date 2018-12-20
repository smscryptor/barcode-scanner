package com.aevi.barcode.scanner.emulator;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.view.Surface;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ImageReaderEmulator {

    private final ImageReader imageReader = Mockito.mock(ImageReader.class);
    private final Image image = Mockito.mock(Image.class);
    private final Surface surface = Mockito.mock(Surface.class);
    private final ArgumentCaptor<ImageReader.OnImageAvailableListener> captor = ArgumentCaptor.forClass(ImageReader.OnImageAvailableListener.class);

    public ImageReaderEmulator() {
        Mockito.doReturn(image).when(imageReader).acquireLatestImage();
        Mockito.doReturn(surface).when(imageReader).getSurface();
    }

    public ImageReader getImageReader() {
        return imageReader;
    }

    public Image onImageAvailable() {
        Mockito.verify(imageReader).setOnImageAvailableListener(captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onImageAvailable(imageReader);
        return image;
    }
}
