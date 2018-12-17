package com.aevi.barcode.scanner;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.WindowManager;
import io.reactivex.Observable;

public class Camera2Preview extends TextureView {

    public Camera2Preview(Context context) {
        super(context);
    }

    public Camera2Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Camera2Preview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Camera2Preview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Observable<Image> start(int imageFormat) {
        return CameraFrameObservable.create((CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE),
                (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE), this, imageFormat);
    }
}
