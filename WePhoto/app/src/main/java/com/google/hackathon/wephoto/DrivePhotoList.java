package com.google.hackathon.wephoto;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.widget.DataBufferAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DrivePhotoList extends DataBufferAdapter<Metadata> {

    private static final String TAG = DrivePhotoList.class.getSimpleName();
    private Activity context;

    public DrivePhotoList(Activity context) {
        super(context, R.layout.list_single);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(),
                    R.layout.list_single, null);
        }
        Metadata metadata = getItem(position);
//        TextView titleTextView =
//                (TextView) convertView.findViewById(R.id.txt);
//        titleTextView.setText(metadata.getTitle());

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.img);
        if (imageView == null) {
            Log.e(TAG, "Failed to find image view");
            return convertView;
        }

        ((WePhotoMainActivity) context).getDriveFileContents(metadata.getDriveId())
                .setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {
                    @Override
                    public void onResult(DriveApi.ContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to read drive file");
                            return;
                        }
                        // Contents object contains pointers to the actual byte stream
                        Contents contents = result.getContents();
                        InputStream stream = contents.getInputStream();

//                        Bitmap bitmap = null;
//                        try {
//                            bitmap = BitmapFactory.decodeStream(stream);
//                        } catch (OutOfMemoryError e) {
//                            Log.i(TAG, "Ran out of memory decoding file: " + e.getMessage());
//                        }

                        // Get the dimensions of the bitmap.
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        // Scale factor will be rounded down to nearest power of two.
                        options.inSampleSize = 2;
                        options.inPurgeable = true;
                        Bitmap bitmap = null;
                        while (true) {
                            try {
                                Log.i(TAG, "Down-sampling image by " + options.inSampleSize + "x");
                                bitmap = BitmapFactory.decodeStream(stream);
                                break;
                            } catch (OutOfMemoryError e) {
                                options.inSampleSize *= 2;
                                try {
                                    stream.reset();
                                } catch (IOException streamError) {
                                    Log.e(TAG,
                                          "error resetting stream:" + streamError.getMessage());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Loading image file error: " + e.getMessage());
                                break;
                            }
                        }
                        if (bitmap == null) {
                            Log.e(TAG, "Failed to decode bitmap stream");
                            return;
                        }
                        imageView.setImageBitmap(bitmap);
                    }
                });

        return convertView;
    }
}