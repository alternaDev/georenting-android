package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import de.alternadev.georenting.data.models.Fence;
import de.alternadev.georenting.databinding.FragmentMapBinding;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MapFragment extends Fragment {
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    private MapView mMapView;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mMapView != null) {
            mMapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMapBinding b = FragmentMapBinding.inflate(inflater, container, false);
        mMapView = b.fragmentMapMapView;
        mMapView.onCreate(savedInstanceState);
        loadGeofences();
        return b.getRoot();
    }

    private void loadGeofences() {
        Realm.getDefaultInstance().where(Fence.class).findAllAsync().asObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(fences -> {
            Log.i("Fence", fences + "");
            for(Fence f : fences) {
                Log.i("Fence", f.getId());
                mMapView.getMap().addCircle(new CircleOptions().center(new LatLng(f.getLatitude(), f.getLongitude())).radius(f.getRadius()));
            }
        });
    }

}
