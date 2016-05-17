package de.alternadev.georenting.ui.main;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import org.parceler.Parcels;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.models.Fence;
import de.alternadev.georenting.databinding.FragmentMapBinding;
import de.alternadev.georenting.ui.GeofenceDetailActivity;
import io.realm.Realm;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MapFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private LocationListener mListener;

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Inject
    GeoRentingService mService;

    private GoogleApiClient mApiClient;

    private MapView mMapView;

    private User mCurrentUser;

    private boolean mMapInited;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoRentingApplication) getActivity().getApplication()).getComponent().inject(this);

        if(((GeoRentingApplication) getActivity().getApplication()).getSessionToken() != null)
            mCurrentUser = ((GeoRentingApplication) getActivity().getApplication()).getSessionToken().user;

        mApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mApiClient.connect();

        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, mListener);

        if (mMapView != null)
            mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null)
            mMapView.onResume();
        if(mApiClient.isConnected())
            mMapView.getMapAsync(this::initMap);
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
        return b.getRoot();
    }

    private void initMap(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(map == null) return;
        map.setMyLocationEnabled(true);

        startLocationUpdates(map);
    }

    private void startLocationUpdates(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest request = LocationRequest.create()
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mListener = l -> {
            if(map == null) return;

            if(!mMapInited) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 15));
                mMapInited = true;
            }

            mService.getFencesNearObservable(l.getLatitude(), l.getLongitude(), 500)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((fences) -> {
                        if(map == null) return;
                        map.clear();
                        map.addCircle(new CircleOptions().center(new LatLng(l.getLatitude(), l.getLongitude())).radius(500));
                        for(GeoFence f : fences) {
                            CircleOptions circle = new CircleOptions().center(new LatLng(f.centerLat, f.centerLon)).radius(f.radius);
                            if(getActivity() == null) continue;
                            circle.fillColor(getActivity().getResources().getColor(f.owner == mCurrentUser.id ? R.color.blue : R.color.red));
                            circle.strokeColor(getActivity().getResources().getColor(f.owner == mCurrentUser.id ? R.color.blue : R.color.red));
                            map.addCircle(circle);
                        }
                        map.setOnMapClickListener(latLng -> {
                            for(GeoFence f : fences) {
                                double radius = f.radius;
                                float[] distance = new float[1];
                                Location.distanceBetween(latLng.latitude, latLng.longitude, f.centerLat, f.centerLon, distance);
                                if(distance[0] < radius) {
                                    onGeoFenceClick(f);
                                    break;
                                }
                            }
                        });
                    });
        };

        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, request, mListener);
    }

    private void onGeoFenceClick(GeoFence f) {
        Intent intent = new Intent(getActivity(), GeofenceDetailActivity.class);
        intent.putExtra(GeofenceDetailActivity.EXTRA_GEOFENCE, Parcels.wrap(f));
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mMapView.getMapAsync(this::initMap);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
