/*
 * Copyright (c) 2019 AEVI International GmbH. All rights reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.aevi.barcode.scanner;

import android.media.Image;
import android.media.ImageReader;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;

public class ImageObservable implements ImageReader.OnImageAvailableListener, ObservableOnSubscribe<ImageReader> {

    public static Observable<Image> create(ImageReader imageReader, long intervalMillis, Scheduler scheduler) {
        Observable<ImageReader> observable = Observable.create(new ImageObservable(imageReader, intervalMillis));
        if (scheduler != null) {
            observable = observable.observeOn(scheduler);
        }
        return observable.concatMap((Function<ImageReader, ObservableSource<Image>>) reader -> {
            Image image = reader.acquireLatestImage();
            return image == null ? Observable.empty() : Observable.just(image);
        }).doAfterNext(image -> image.close());
    }

    public final ImageReader imageReader;
    public final long intervalMillis;
    public long lastEmitted = 0;
    private volatile ObservableEmitter<ImageReader> observableEmitter;

    private ImageObservable(ImageReader imageReader, long intervalMillis) {
        this.imageReader = imageReader;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void subscribe(ObservableEmitter<ImageReader> emitter) {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(() -> {
                imageReader.getSurface().release();
                imageReader.close();
            });
            imageReader.setOnImageAvailableListener(this, null);
        }
    }

    @Override
    public synchronized void onImageAvailable(ImageReader imageReader) {
        long millis = System.currentTimeMillis();
        if (millis - lastEmitted > intervalMillis) {
            observableEmitter.onNext(imageReader);
            lastEmitted = millis;
        } else {
            Image image = imageReader.acquireLatestImage();
            if (image != null) {
                image.close();
            }
        }
    }
}
