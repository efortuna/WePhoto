package com.google.hackathon.wephoto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class TakePhotoFragment extends Fragment implements SurfaceHolder.Callback {

    private static final String TAG = TakePhotoFragment.class.getSimpleName();
    private static final int ACTIVITY_REQUEST_CAPTURE = 1;
    private long snapNumber;
    private Bitmap currentBitmap;

    private RelativeLayout mRootView;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    private  PictureTakenCallback mPictureCallback;

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "#####################onStart Called on fragment");
        // setup camera
//       safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK);



        Button btn = (Button)getActivity().findViewById(R.id.snapButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
//                snapPicture(view);
            }
        });
    }

    void takePicture() {
      mCamera.takePicture(null, null, mPictureCallback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPictureCallback = new PictureTakenCallback();

        updateState();
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_take_photo, container, false);
        mSurfaceView = new SurfaceView(getActivity());
        ((RelativeLayout) mRootView.findViewById(R.id.selfView)).addView(mSurfaceView);

        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        return mRootView;
    }

    protected void updateState() {
        SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
    }

    public void snapPicture(View view) {
        Log.i(TAG, "snapPicture");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        bumpSnapfile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getSnapfile()));
        startActivityForResult(intent, ACTIVITY_REQUEST_CAPTURE);
    }
    protected void bumpSnapfile() {
        snapNumber = new Date().getTime();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_CAPTURE && resultCode == Activity.RESULT_OK) {
            updateView();
            uploadPhoto();
            return;
        }
        Log.i(TAG, "Unknown Activity Request code: " + requestCode);
    }

    protected void updateView() {
        // Get the dimensions of the bitmap.
        BitmapFactory.Options options = new BitmapFactory.Options();
        // Scale factor will be rounded down to nearest power of two.
        options.inSampleSize = 2;
        options.inPurgeable = true;
        Bitmap bitmap = null;
        String snapPath = getSnapfile().getAbsolutePath();
        while (true) {
            try {
                Log.i(TAG, "Down-sampling image by " + options.inSampleSize + "x");
                bitmap = BitmapFactory.decodeFile(snapPath, options);
                break;
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
            } catch (Exception e) {
                Log.i(TAG, "Loading image file error: " + e.getMessage());
            }
        }
        currentBitmap = bitmap;
        if (bitmap == null) {
            Log.w(TAG, "Failed to read image from storage");
            return;
        }
        Log.i(TAG, "Snapshot dimensions: " + bitmap.getWidth() + " x " + bitmap.getHeight());
//        ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView);
//        if (imageView == null) {
//            return;
//        }
//        imageView.setImageBitmap(bitmap);
    }

    protected void uploadPhoto() {
        if (currentBitmap == null) {
            Log.w(TAG, "skipping upload of null bitmap");
            return;
        }
        ((WePhotoMainActivity) this.getActivity()).saveImageToDrive(
                currentBitmap, getSnapfile().getName());
        Log.i(TAG, "Saving file to drive: " + getSnapfile().getName());
    }

    protected File getSnapfile() {
        File snapDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.pictureFolder));
        snapDir.mkdirs();
        File snapFile = new File(snapDir, String.format("%d.png", snapNumber));
        Log.i(TAG, "Snapshot path: " + snapFile.getAbsolutePath());
        return snapFile;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        safeCameraOpen(Camera.CameraInfo.CAMERA_FACING_BACK);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        releaseCameraAndPreview();
    }

    private class PictureTakenCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            bumpSnapfile();
            ((WePhotoMainActivity) getActivity()).saveImageBytesToDrive(
                    data, getSnapfile().getName());
            mCamera.startPreview();
        }
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
            mRootView.requestLayout();

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


}