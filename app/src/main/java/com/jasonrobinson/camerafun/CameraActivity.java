package com.jasonrobinson.camerafun;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Button mCaptureButton;

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Camera Fun");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = new CameraPreview(this);

        FrameLayout previewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        previewFrameLayout.addView(mPreview);

        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCaptureButton.getLayoutParams();
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Remove rules pre-API 17
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);

            // Add new rules
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
        } else {
            // Remove rules pre-API 17
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            params.addRule(RelativeLayout.CENTER_VERTICAL, 0);

            // Add new rules
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        mCaptureButton.setLayoutParams(params);

        updateCameraDisplayOrientation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeCamera();
    }

    private void updateCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int rotate = (info.orientation - degrees + 360) % 360;

        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(rotate);
        mCamera.setParameters(params);

        mCamera.setDisplayOrientation(rotate);
    }

    private void initializeCamera() {
        mCamera = getCameraInstance();
        if (mCamera == null) {
            Toast.makeText(this, R.string.camera_unavailable, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        updateCameraDisplayOrientation();
        mPreview.setCamera(mCamera);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mPreview.setCamera(null);
        }
    }

    private Camera getCameraInstance() {
        try {
            return Camera.open();
        } catch (Exception e) {
            return null;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                Toast.makeText(CameraActivity.this, getString(R.string.picture_saved, pictureFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(CameraActivity.this, R.string.saving_error, Toast.LENGTH_LONG).show();
            }
        }
    };
}
