package com.google.hackathon.wephoto;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.query.Filter;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = PhotoGalleryFragment.class.getSimpleName();
    private String mNextPageToken;
    private Boolean mHasMore = true;
    private DrivePhotoList resultsAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        ListView myListView = (ListView) rootView.findViewById(R.id.fragment_gallery_list);
        resultsAdapter = new DrivePhotoList(this.getActivity());
        myListView.setAdapter(resultsAdapter);

        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            /**
             * Handles onScroll to retrieve next pages of results
             * if there are more results items to display.
             */
            @Override
            public void onScroll(AbsListView view, int first, int visible, int total) {
                if (mNextPageToken != null && first + visible + 5 < total) {
                    retrieveNextPage();
                }
            }
        });

        retrieveNextPage();

        Button myRefreshButton = (Button) rootView.findViewById(R.id.fragment_gallery_refresh);
        myRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "clicked refresh!");
                resultsAdapter.notifyDataSetInvalidated();
                resultsAdapter.clear();
                mNextPageToken = null;
                mHasMore = true;
                retrieveNextPage();
            }
        });

        return rootView;
    }

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private void retrieveNextPage() {
        // if there are no more results to retrieve,
        // return silently.
        if (!mHasMore) {
            return;
        }
        // TODO(jakemac): List files only in the current folder!
        ((WePhotoMainActivity) this.getActivity()).listDriveFolderContents(mNextPageToken)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                        if (!metadataBufferResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to get files.");
                            return;
                        }
                        resultsAdapter.append(metadataBufferResult.getMetadataBuffer());
                        String mNextPageToken =
                                metadataBufferResult.getMetadataBuffer().getNextPageToken();
                        mHasMore = mNextPageToken != null;
                    }
                });
    }
}
