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
import android.view.Surface;

import com.aevi.barcode.scanner.SurfaceTextureObservable.Callback;
import com.aevi.barcode.scanner.Tuple.Tuple2;

import io.reactivex.Observable;

public class SurfaceObservable {

    public interface SurfaceFactory {

        Surface create(SurfaceTexture surfaceTexture);
    }

    public static Observable<Surface> create(
            Observable<Tuple2<Callback, SurfaceTexture>> surfaceTextureObservable, SurfaceFactory surfaceFactory) {

        return Observable.using(
                () -> new Surface[]{null},
                (ref) -> surfaceTextureObservable
                        .filter(tuple -> Callback.AVAILABLE.equals(tuple.t1))
                        .map(tuple -> (ref[0] = surfaceFactory.create(tuple.t2))),
                (ref) -> releaseIfNotNull(ref[0])
        );
    }

    private static void releaseIfNotNull(Surface surface) {
        if (surface != null) {
            surface.release();
        }
    }
}
