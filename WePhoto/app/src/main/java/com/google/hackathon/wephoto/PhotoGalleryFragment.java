package com.google.hackathon.wephoto;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class PhotoGalleryFragment extends Fragment {
    ListView list;
    String[] web;
    Integer[] imageId;
    View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        web = new String[100];
        imageId = new Integer[100];
        for (int i=0; i < web.length; i++) {
            web[i] = (i%2 == 0) ? "grand canyon" : "space needle";
            imageId[i] = (i%2 == 0) ? R.drawable.image1 : R.drawable.image2;
        }
        PhotoList adapter = new
                PhotoList(getActivity(), web, imageId);
        list=(ListView)(rootView.findViewById(R.id.fragment_gallery_list));
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
            }
        });


        return rootView;
    }
}
