package de.alternadev.georenting.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import de.alternadev.georenting.BuildConfig;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.models.Fence;
import de.alternadev.georenting.databinding.ActivityGeofenceCreateBinding;
import de.alternadev.georenting.databinding.ActivityGeofenceDetailBinding;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class CreateGeofenceActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks {
    private static final float MINIMUM_ACCURACY = 40;

    private GoogleApiClient mApiClient;
    private MapView mMapView;
    private LocationListener mListener;
    private ActivityGeofenceCreateBinding mBinding;

    private Location mLocation;

    @Inject
    GeoRentingService mService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        getGeoRentingApplication().getComponent().inject(this);

        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_geofence_create);

        mMapView = mBinding.geofenceMap;
        mMapView.onCreate(savedInstanceState);
        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mApiClient.connect();

        mBinding.createButton.setOnClickListener(this::onCreateClick);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void onCreateClick(View view) {

        if(TextUtils.isEmpty(mBinding.nameText.getText())) {
            mBinding.nameLayout.setErrorEnabled(true);
            mBinding.nameLayout.setError(getString(R.string.create_geofence_error_name_empty));
        }
        if(mLocation == null || mLocation.getAccuracy() > MINIMUM_ACCURACY) {
            Timber.d("Location: %s", mLocation);
            return;
        }

        String fenceName = mBinding.nameText.getText().toString();

        GeoFence fence = new GeoFence();
        fence.centerLat = mLocation.getLatitude();
        fence.centerLon = mLocation.getLongitude();
        fence.name = fenceName;

        stopLocationUpdates();
        mBinding.setLoading(true);
        mService.createGeoFence(fence)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((geoFence) -> {
                    finish();
                }, error -> {
                    mBinding.setLoading(false);
                    if(mApiClient.isConnected())
                        mMapView.getMapAsync(this::initMap);

                    if(error instanceof HttpException) {
                        if (((HttpException) error).code() == 400) { // Overlap
                            Snackbar.make(mBinding.getRoot(), R.string.error_create_fence_overlap, Snackbar.LENGTH_LONG).show();
                        } else if (((HttpException) error).code() == 402) { // GeoCoins
                            Snackbar.make(mBinding.getRoot(), R.string.error_create_fence_cant_afford, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
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



    private void initMap(GoogleMap googleMap) {
        if (googleMap == null) return;
        googleMap.getUiSettings().setScrollGesturesEnabled(false);

        startLocationUpdates(googleMap);
    }

    private void startLocationUpdates(GoogleMap map) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest request = LocationRequest.create()
                .setFastestInterval(1000)
                .setInterval(2500)
                .setSmallestDisplacement(1)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mBinding.setLoading(true);

        mListener = l -> {
            if(map == null) return;
            if(l.getAccuracy() > MINIMUM_ACCURACY) return;

            this.mLocation = l;

            // if(l.isFromMockProvider() && !BuildConfig.DEBUG) return; //TODO: Detect mock location on api level 15-18

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 15));
            map.clear();
            mBinding.setLoading(true);

            CircleOptions circle = new CircleOptions().center(new LatLng(l.getLatitude(), l.getLongitude())).radius(100); // TODO: Fetch possible Geofence Sizes from server.
            circle.fillColor(getResources().getColor(R.color.blue));
            circle.strokeColor(getResources().getColor( R.color.blue));
            map.addCircle(circle);

            mService.estimateCost(new GeoFence(l.getLatitude(), l.getLongitude()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((estimate) -> {
                        mBinding.setOverlap(false);
                        mBinding.setCostEstimate(estimate);
                        mBinding.setLoading(false);

                    }, (error) -> {
                        if(error instanceof HttpException) {
                            if(((HttpException) error).code() == 400) {
                                mBinding.setOverlap(true);
                                mBinding.setLoading(false);
                            }
                        }
                    });
        };

        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, request, mListener);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mMapView.getMapAsync(this::initMap);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
