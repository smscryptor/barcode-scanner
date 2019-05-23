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
