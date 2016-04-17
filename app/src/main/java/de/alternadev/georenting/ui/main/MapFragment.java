package de.alternadev.georenting.ui.main;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import rx.android.schedulers.AndroidSchedulers;

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
        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMapBinding b = FragmentMapBinding.inflate(inflater, container, false);
        mMapView = b.fragmentMapMapView;
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this::initMap);
        return b.getRoot();
    }

    private void initMap(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        Realm.getDefaultInstance().where(Fence.class).findAllAsync().asObservable().observeOn(AndroidSchedulers.mainThread()).subscribe(fences -> {
            Log.i("Fence", fences + "");
            for(Fence f : fences) {
                Log.i("Fence", f.getId());
                map.addCircle(new CircleOptions().center(new LatLng(f.getLatitude(), f.getLongitude())).radius(f.getRadius()));
            }
        });
    }

}
