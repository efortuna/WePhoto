package com.google.hackathon.wephoto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.hackathon.wephoto.R;

public class PreviousEventsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_previous_events, container, false);

        ListView myListView = (ListView) rootView.findViewById(R.id.previous_events);
        EventsAdapter resultsAdapter = new EventsAdapter(this.getActivity());
        myListView.setAdapter(resultsAdapter);
        ((WePhotoMainActivity) this.getActivity()).listRootFolderContents(resultsAdapter);

        return rootView;
    }

}