package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.alternadev.georenting.R;

public class MyGeofencesFragment extends Fragment {
    public static MyGeofencesFragment newInstance() {
        MyGeofencesFragment fragment = new MyGeofencesFragment();
        return fragment;
    }

    public MyGeofencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_geofences, container, false);
    }
}
