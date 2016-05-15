package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.parceler.Parcels;

import java.util.ArrayList;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.databinding.FragmentMyGeofencesBinding;
import de.alternadev.georenting.ui.main.mygeofences.GeofenceAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MyGeofencesFragment extends Fragment {
    private static final String SAVED_INSTANCE_CURRENT_USER = "currentUser";


    public static MyGeofencesFragment newInstance(User currentUser) {
        MyGeofencesFragment f = new MyGeofencesFragment();
        Bundle args = new Bundle();
        args.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(currentUser));
        f.setArguments(args);

        return f;
    }

    @Inject
    GeoRentingService mService;

    private User mCurrentUser;

    public MyGeofencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoRentingApplication) getActivity().getApplicationContext()).getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments() != null && getArguments().getParcelable(SAVED_INSTANCE_CURRENT_USER) != null)
            mCurrentUser = Parcels.unwrap(getArguments().getParcelable(SAVED_INSTANCE_CURRENT_USER));

        if(mCurrentUser == null)
            mCurrentUser = Parcels.unwrap(savedInstanceState.getParcelable(SAVED_INSTANCE_CURRENT_USER));

        FragmentMyGeofencesBinding b = FragmentMyGeofencesBinding.inflate(inflater, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        b.geofencesList.setLayoutManager(layoutManager);

        b.geofencesRefresh.setOnRefreshListener(() -> loadFences(b));
        b.geofencesRefresh.setRefreshing(true);
        loadFences(b);

        // Inflate the layout for this fragment
        return b.getRoot();
    }

    private void loadFences(FragmentMyGeofencesBinding b) {
        mService.getFencesBy("" + mCurrentUser.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(geoFences -> {
                    if(geoFences != null) {
                        RecyclerView.Adapter adapter = new GeofenceAdapter(geoFences, getActivity());
                        b.geofencesList.setAdapter(adapter);
                        b.setGeoFences(geoFences);
                    } else {
                        b.setGeoFences(new ArrayList());
                    }
                }, t -> {
                    Snackbar.make(b.getRoot(), R.string.error_network, Snackbar.LENGTH_LONG).setAction(R.string.error_network_action_retry, v -> {
                        this.loadFences(b);
                    }).show();
                }, () -> {
                    b.geofencesRefresh.setRefreshing(false);
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(mCurrentUser));
    }
}
