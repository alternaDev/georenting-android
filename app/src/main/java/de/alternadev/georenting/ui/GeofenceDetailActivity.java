package de.alternadev.georenting.ui;

import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GoogleMapsStatic;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ActivityGeofenceDetailBinding;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.util.async.Async;
import timber.log.Timber;

/**
 * Created by jhbruhn on 18.04.16.
 */
public class GeofenceDetailActivity extends BaseActivity {

    public static final String EXTRA_GEOFENCE = "geofence";

    @Inject
    Picasso mPicasso;

    @Inject
    GoogleMapsStatic mStaticMap;

    private GeoFence mGeofence;
    private CountDownTimer mCountDown;
    private Geocoder mGeocoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        getGeoRentingApplication().getComponent().inject(this);

        super.onCreate(savedInstanceState);

        mGeocoder = new Geocoder(getGeoRentingApplication());

        ActivityGeofenceDetailBinding b = DataBindingUtil.setContentView(this, R.layout.activity_geofence_detail);


        if(getIntent().getExtras() != null && getIntent().getParcelableExtra(EXTRA_GEOFENCE) != null)
            mGeofence = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_GEOFENCE));

        b.setGeoFence(mGeofence);

        rx.Observable.fromCallable(() -> {
            try {
                Address address = mGeocoder.getFromLocation(mGeofence.centerLat, mGeofence.centerLon, 1).get(0);
                String val = address.getLocality() + ", " + address.getCountryName();

                return val;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }).observeOn(Schedulers.io())
          .subscribe(b::setLocation);

        mCountDown = new CountDownTimer(mGeofence.diesAt.getTime() - new Date().getTime(), 1000) {

            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                int day = (int)TimeUnit.SECONDS.toDays(seconds);        
                long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
                long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
                long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
                b.geofenceDeathCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d:%02d", day, hours, minute, second));
            }

            public void onFinish() {
                b.geofenceDeathCountdown.setText(getString(R.string.geofence_detail_geofence_dead));
            }
        };
        mCountDown.start();


        final boolean[] imageLoaded = {false};
        b.geofenceMap.getViewTreeObserver().addOnPreDrawListener(() -> {
            if(imageLoaded[0]) return true;
            mPicasso.load(mStaticMap.getFenceThumbnailMapUrl(mGeofence,
                    b.geofenceMap.getWidth(),
                    b.geofenceMap.getHeight()))
                    .fit()
                    .centerCrop()
                    .into(b.geofenceMap);
            imageLoaded[0] = true;
            return true;
        });

        if(mGeofence != null)
            logSelectGeoFence(mGeofence);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //Toolbar toolbarCollapse = (Toolbar) findViewById(R.id.toolbar_collapse);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_forward, R.anim.right_to_left);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_forward, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCountDown.cancel();
    }
}
