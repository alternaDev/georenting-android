package de.alternadev.georenting.ui;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GoogleMapsStatic;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ActivityGeofenceDetailBinding;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        getGeoRentingApplication().getComponent().inject(this);

        super.onCreate(savedInstanceState);

        ActivityGeofenceDetailBinding b = DataBindingUtil.setContentView(this, R.layout.activity_geofence_detail);


        if(getIntent().getExtras() != null && getIntent().getParcelableExtra(EXTRA_GEOFENCE) != null)
            mGeofence = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_GEOFENCE));

        b.setGeoFence(mGeofence);
        final boolean[] imageLoaded = {false};
        b.geofenceMap.getViewTreeObserver().addOnPreDrawListener(() -> {
            if(imageLoaded[0]) return true;
            mPicasso.load(mStaticMap.getFenceThumbnailMapUrl(mGeofence,
                    getResources().getDimensionPixelSize(R.dimen.image_map_width) / 2,
                    getResources().getDimensionPixelSize(R.dimen.image_map_height) / 2))
                    .fit()
                    .centerCrop()
                    .into(b.geofenceMap);
            imageLoaded[0] = true;
            return true;
        });

        if(mGeofence != null)
            logSelectGeoFence(mGeofence);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
