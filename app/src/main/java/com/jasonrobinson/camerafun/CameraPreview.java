package com.jasonrobinson.camerafun;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null) {
            refreshPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null && mHolder.getSurface() != null) {
            refreshPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // ignore; camera release handled in activity
    }

    private void refreshPreview() {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore; camera is already not previewing
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCamera(Camera mCamera) {
        this.mCamera = mCamera;

        if (mCamera != null) {
            refreshPreview();
        }
    }
}
