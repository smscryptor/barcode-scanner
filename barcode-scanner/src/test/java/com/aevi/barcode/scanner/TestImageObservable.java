package com.aevi.barcode.scanner;

import android.media.Image;

import com.aevi.barcode.scanner.emulator.ImageReaderEmulator;

import org.junit.Test;
import org.mockito.Mockito;

import io.reactivex.observers.TestObserver;

public class TestImageObservable extends BaseTest {

    private final ImageReaderEmulator emulator = new ImageReaderEmulator();

    @Test
    public void doEmitImage() {
        TestObserver<Image> observer = ImageObservable.create(emulator.getImageReader(), 0, null).test();

        Image image = emulator.onImageAvailable();

        observer.assertValue(image);
        Mockito.verify(image).close();
    }

    @Test
    public void doCloseImageUponException() {
        ImageObservable.create(emulator.getImageReader(), 0, null).subscribe(image -> {
            Mockito.verify(image, Mockito.never()).close();
            throw new IllegalArgumentException("this is an exception!");
        }, throwable -> {
        });

        Image image = emulator.onImageAvailable();

        Mockito.verify(image).close();
    }
}
