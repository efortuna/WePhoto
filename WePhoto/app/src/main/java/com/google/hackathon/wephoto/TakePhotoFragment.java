package com.google.hackathon.wephoto;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class TakePhotoFragment extends Fragment {

    private static final String TAG = TakePhotoFragment.class.getSimpleName();
    private static final int ACTIVITY_REQUEST_CAPTURE = 1;
    private static final String PREFS_SNAP_NUMBER = "snapNumber";
    private int snapNumber;

    @Override
    public void onStart() {
        super.onStart();
        Button btn = (Button)getActivity().findViewById(R.id.snapButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snapPicture(view);
            }
        });
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateState();
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_take_photo, container, false);

        return rootView;
    }

    protected void updateState() {
        SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
        snapNumber = prefs.getInt(PREFS_SNAP_NUMBER, 0);
    }

    public void snapPicture(View view) {
        Log.i(TAG, "snapPicture");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        bumpSnapfile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getSnapfile()));
        startActivityForResult(intent, ACTIVITY_REQUEST_CAPTURE);
    }
    protected void bumpSnapfile() {
        snapNumber += 1;
        SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREFS_SNAP_NUMBER, snapNumber);
        editor.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_CAPTURE && resultCode == Activity.RESULT_OK) {
            uploadPhoto();
            updateView();
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
        if (bitmap == null) {
            Log.w(TAG, "Failed to read image from storage");
            return;
        }
        Log.i(TAG, "Snapshot dimensions: " + bitmap.getWidth() + " x " + bitmap.getHeight());
        ImageView imageView = (ImageView) getActivity().findViewById(R.id.imageView);
        if (imageView == null) {
            return;
        }
        imageView.setImageBitmap(bitmap);
    }

    protected void uploadPhoto() {
        String snapPath = getSnapfile().getAbsolutePath();
        Bitmap bitmap = BitmapFactory.decodeFile(snapPath);
        if (bitmap == null) {
            Log.w(TAG, "Failed to read image from storage");
            return;
        }
        ((WePhotoMainActivity) this.getActivity()).saveImageToDrive(
                bitmap, getSnapfile().getName());
        Log.i(TAG, "Saving file to drive: " + getSnapfile().getName());
    }

    protected File getSnapfile() {
        File snapDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                getString(R.string.pictureFolder));
        snapDir.mkdirs();
        File snapFile = new File(snapDir, String.format("snap-%04d.png", snapNumber));
        Log.i(TAG, "Snapshot path: " + snapFile.getAbsolutePath());
        return snapFile;
    }
}