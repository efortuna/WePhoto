package com.google.hackathon.wephoto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.hackathon.wephoto.R;

import java.util.ArrayList;
import java.util.List;

public class PreviousEventsFragment extends Fragment {

    private static final String TAG = PreviousEventsFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_previous_events, container, false);

        ListView myListView = (ListView) rootView.findViewById(R.id.previous_events);
        final EventsAdapter resultsAdapter = new EventsAdapter(this.getActivity());
        myListView.setAdapter(resultsAdapter);
        // TODO(jakemac): List folders, not files, use the main activities currentEvent DriveId.
        List<Filter> filters = new ArrayList<Filter>();
//        filters.add(Filters.in(
//                SearchableField.PARENTS, ((WePhotoMainActivity) this.getActivity()).currentEvent));
        ((WePhotoMainActivity) this.getActivity()).listDriveFolderContents(null)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                        if (!metadataBufferResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to get folders.");
                            return;
                        }
                        resultsAdapter.append(metadataBufferResult.getMetadataBuffer());
                    }
                });

        return rootView;
    }

}