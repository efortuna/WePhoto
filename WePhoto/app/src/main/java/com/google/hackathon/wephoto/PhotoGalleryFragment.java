package com.google.hackathon.wephoto;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.query.Filter;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        ListView myListView = (ListView) rootView.findViewById(R.id.fragment_gallery_list);
        final DrivePhotoList resultsAdapter = new DrivePhotoList(this.getActivity());
        myListView.setAdapter(resultsAdapter);
        // TODO(jakemac): List files only in the current folder!
        ((WePhotoMainActivity) this.getActivity()).listDriveFolderContents()
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                        if (!metadataBufferResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to get files.");
                            return;
                        }
                        resultsAdapter.append(metadataBufferResult.getMetadataBuffer());
                    }
                });

        return rootView;
    }
}
