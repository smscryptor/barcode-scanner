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

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.TextureView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class SurfaceTextureObservable implements TextureView.SurfaceTextureListener,
        ObservableOnSubscribe<Tuple.Tuple2<SurfaceTextureObservable.Callback, SurfaceTexture>> {

    public enum Callback {
        AVAILABLE,
        SIZE_CHANGED,
        UPDATED
    }

    protected static Observable<Tuple.Tuple2<SurfaceTextureObservable.Callback, SurfaceTexture>>
    create(TextureView textureView, Handler mainHandler) {
        return Observable.create(new SurfaceTextureObservable(textureView, mainHandler));
    }

    private final TextureView textureView;
    private final Handler mainHandler;
    private volatile ObservableEmitter<Tuple.Tuple2<SurfaceTextureObservable.Callback, SurfaceTexture>> observableEmitter;

    private SurfaceTextureObservable(TextureView textureView, Handler mainHandler) {
        this.textureView = textureView;
        this.mainHandler = mainHandler;
    }

    @Override
    public void subscribe(ObservableEmitter<Tuple.Tuple2<SurfaceTextureObservable.Callback, SurfaceTexture>> emitter) {
        observableEmitter = emitter;
        if (!observableEmitter.isDisposed()) {
            observableEmitter.setCancellable(() -> {
                textureView.setSurfaceTextureListener(null);
            });
            textureView.setSurfaceTextureListener(this);
            if (textureView.isAvailable()) {
                mainHandler.post(() -> onSurfaceTextureAvailable(textureView.getSurfaceTexture(), 0, 0));
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (surfaceTexture == null || !next(Tuple.of(Callback.AVAILABLE, surfaceTexture))) {
            textureView.setSurfaceTextureListener(null);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        next(Tuple.of(Callback.SIZE_CHANGED, surfaceTexture));
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        observableEmitter.onComplete();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        next(Tuple.of(Callback.UPDATED, surfaceTexture));
    }

    public boolean next(Tuple.Tuple2<SurfaceTextureObservable.Callback, SurfaceTexture> value) {
        if (observableEmitter != null && !observableEmitter.isDisposed()) {
            observableEmitter.onNext(value);
            return true;
        } else {
            return false;
        }
    }
}
