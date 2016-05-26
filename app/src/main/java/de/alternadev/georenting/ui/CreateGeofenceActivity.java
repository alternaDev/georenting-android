package de.alternadev.georenting.ui;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.satsuware.usefulviews.LabelledSpinner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.BuildConfig;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.CostEstimate;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.api.model.UpgradeSettings;
import de.alternadev.georenting.data.models.Fence;
import de.alternadev.georenting.databinding.ActivityGeofenceCreateBinding;
import de.alternadev.georenting.databinding.ActivityGeofenceDetailBinding;
import hugo.weaving.DebugLog;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class CreateGeofenceActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks {
    private static final float MINIMUM_ACCURACY = 50;
    private static final int MINIMUM_TTL = 60 * 60;

    private GoogleApiClient mApiClient;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private LocationListener mListener;

    private ActivityGeofenceCreateBinding mBinding;

    private Location mLocation;
    private MenuItem mBuyButton;
    private boolean mBuyButtonEnabled;
    private int mSelectedRadiusPosition;
    private int mSelectedRentPosition;
    private double mCurrentPrice;

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

        populateSpinners();
        setupTTLSlider();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= 21) {
            mBinding.getRoot().setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = mBinding.getRoot().getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        mBinding.getRoot().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });
            }
        }
    }



    private void circularRevealActivity() {
        overridePendingTransition(0, 0);

        View rootLayout = mBinding.getRoot();

        int cx = rootLayout.getWidth();
        int cy = rootLayout.getHeight();

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
        circularReveal.setDuration(250);

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    private void finishActivityWithAnimation() {
        View rootLayout = mBinding.getRoot();

        int cx = rootLayout.getWidth();
        int cy = rootLayout.getHeight();

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, finalRadius, 0);
        circularReveal.setDuration(250);

        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                rootLayout.setVisibility(View.INVISIBLE);
                overridePendingTransition(0, 0);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        // make the view visible and start the animation
        circularReveal.start();
    }

    private void populateSpinners() {
        List<String> sizes = new ArrayList<>(getGeoRentingApplication().getUpgradeSettings().radius.size());
        for (Integer i : getGeoRentingApplication().getUpgradeSettings().radius) {
            sizes.add(i + "m");
        }
        String[] array = sizes.toArray(new String[sizes.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBinding.radiusSpinner.setCustomAdapter(adapter);
        mBinding.radiusSpinner.setSelection(0);
        mBinding.radiusSpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                mSelectedRadiusPosition = position;
                refreshEstimate();
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

            }
        });

        Double[] array2 = getGeoRentingApplication().getUpgradeSettings().rent.toArray(new Double[getGeoRentingApplication().getUpgradeSettings().rent.size()]);
        ArrayAdapter<Double> adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, array2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBinding.rentMultiplierSpinner.setCustomAdapter(adapter2);
        mBinding.rentMultiplierSpinner.setSelection(0);
        mBinding.rentMultiplierSpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                mSelectedRentPosition = position;
                refreshEstimate();
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

            }
        });

    }

    private void setupTTLSlider() {
        mBinding.ttlSeekBar.setMax((int) getGeoRentingApplication().getUpgradeSettings().maxTtl - MINIMUM_TTL);
        mBinding.ttlSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                refreshTTLEditText();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                refreshEstimate();
            }
        });
        refreshTTLEditText();
    }

    private void refreshTTLEditText() {
        int progress = mBinding.ttlSeekBar.getProgress() + MINIMUM_TTL;
        mBinding.ttlEditText.setText(getString(R.string.create_geofence_ttl_hours, (progress / 60f / 60f)));
    }

    private void onCreateClick() {
        String fenceName = mBinding.nameText.getText().toString().trim();

        if(TextUtils.isEmpty(fenceName)) {
            mBinding.nameLayout.setErrorEnabled(true);
            mBinding.nameLayout.setError(getString(R.string.create_geofence_error_name_empty));
            return;
        }
        if(mLocation == null || mLocation.getAccuracy() > MINIMUM_ACCURACY) {
            Timber.d("Location: %s", mLocation);
            return;
        }

        GeoFence fence = getGeoFenceFromParams();
        fence.name = fenceName;

        stopLocationUpdates();
        setBuyButtonEnabled(false);
        mService.createGeoFence(fence)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((geoFence) -> {
                    logBuyGeoFence(geoFence, mCurrentPrice);
                    finishActivityWithAnimation();
                }, error -> {
                    setBuyButtonEnabled(true);

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

    @DebugLog
    private void stopLocationUpdates() {
        if(mApiClient.isConnected() && mListener != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        overridePendingTransition(0, 0);
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



    @DebugLog
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
        setBuyButtonEnabled(false);

        this.mGoogleMap = map;

        mListener = l -> {
            Timber.d("Location: %s", l);

            if(map == null) return;
            if(l.getAccuracy() > MINIMUM_ACCURACY) {
                Timber.d("Higher Accuracy wanted. Got %f, expected %f.", l.getAccuracy(), MINIMUM_ACCURACY);
                return;
            }

            this.mLocation = l;

            // if(l.isFromMockProvider() && !BuildConfig.DEBUG) return; //TODO: Detect mock location on api level 15-18

           refreshEstimate();
        };

        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, request, mListener);
    }

    private GeoFence getGeoFenceFromParams() {
        UpgradeSettings s = getGeoRentingApplication().getUpgradeSettings();
        GeoFence fence = new GeoFence(mLocation.getLatitude(), mLocation.getLongitude());
        fence.radius = s.radius.get(mSelectedRadiusPosition);
        fence.rentMultiplier = s.rent.get(mSelectedRentPosition);
        fence.ttl = mBinding.ttlSeekBar.getProgress() + MINIMUM_TTL;
        return fence;
    }

    private void refreshEstimate() {
        if(mLocation == null) return;
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 14));
        mGoogleMap.clear();

        mBinding.setLoading(true);
        setBuyButtonEnabled(false);

        GeoFence fence = getGeoFenceFromParams();

        CircleOptions circle = new CircleOptions().center(new LatLng(fence.centerLat, fence.centerLon)).radius(fence.radius);
        circle.fillColor(getResources().getColor(R.color.blue));
        circle.strokeColor(getResources().getColor( R.color.blue));
        mGoogleMap.addCircle(circle);

        mService.estimateCost(fence)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((estimate) -> {
                    mBinding.setOverlap(false);
                    mBinding.setCostEstimate(estimate);
                    mBinding.setLoading(false);
                    setBuyButtonEnabled(estimate.canAfford);
                    mCurrentPrice = estimate.cost;
                }, (error) -> {
                    if(error instanceof HttpException) {
                        setBuyButtonEnabled(false);
                        mBinding.setLoading(false);
                        mBinding.setCostEstimate(new CostEstimate());
                        if(((HttpException) error).code() == 400) {
                            mBinding.setOverlap(true);
                        }
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mMapView.getMapAsync(this::initMap);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_geofence_toolbar, menu);
        mBuyButton = menu.findItem(R.id.action_buy);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_buy:
                if(item.isEnabled() && mBuyButtonEnabled) { // Fix for not working Android API... WTF
                    this.onCreateClick();
                    return true;
                }
                return false;
                // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finishActivityWithAnimation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishActivityWithAnimation();
    }

    private void setBuyButtonEnabled(boolean e) {
        mBuyButtonEnabled = e;
        mBuyButton.setEnabled(e);
        supportInvalidateOptionsMenu();
    }
}
