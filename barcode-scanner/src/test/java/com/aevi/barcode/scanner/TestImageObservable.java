package com.aevi.barcode.scanner;

import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.view.Surface;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestImageObservable {

    @Mock
    ImageReader imageReader;
    @Mock
    Image image;
    @Mock
    Surface surface;
    @Captor
    ArgumentCaptor<ImageReader.OnImageAvailableListener> captor;

    @Before
    public void setup() {
        Mockito.doReturn(image).when(imageReader).acquireLatestImage();
        Mockito.doReturn(surface).when(imageReader).getSurface();
    }

    @Test
    public void doEmitImage() {
        TestObserver<Image> observer = ImageObservable.create(imageReader, 0, null).test();

        Mockito.verify(imageReader).setOnImageAvailableListener(captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onImageAvailable(imageReader);

        observer.assertValue(image);
        Mockito.verify(image).close();
    }

    @Test
    public void doCloseImageUponException() {
        ImageObservable.create(imageReader, 0, null).subscribe(new Consumer<Image>() {
            @Override
            public void accept(Image image) {
                Mockito.verify(image, Mockito.never()).close();
                throw new IllegalArgumentException("this is an exception!");
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
            }
        });

        Mockito.verify(imageReader).setOnImageAvailableListener(captor.capture(), Mockito.nullable(Handler.class));
        captor.getValue().onImageAvailable(imageReader);

        Mockito.verify(image).close();
    }
}
