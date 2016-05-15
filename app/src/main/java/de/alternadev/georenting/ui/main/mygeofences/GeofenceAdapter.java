package de.alternadev.georenting.ui.main.mygeofences;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GoogleMapsStatic;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ItemGeofenceBinding;
import de.alternadev.georenting.ui.GeofenceDetailActivity;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by jhbruhn on 17.04.16.
 */

public class GeofenceAdapter extends RecyclerView.Adapter<GeofenceViewHolder> {

    @Inject
    GoogleMapsStatic mStaticMap;
    @Inject
    Picasso mPicasso;

    private final List<GeoFence> mGeoFences;

    private final Activity mActivity;
    private final Toolbar mToolbar;

    public GeofenceAdapter(List<GeoFence> geoFences, Activity activity, Toolbar toolbar) {
        mGeoFences = geoFences;
        mActivity = activity;
        mToolbar = toolbar;
        ((GeoRentingApplication) activity.getApplication()).getComponent().inject(this);
    }

    @Override
    public GeofenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemGeofenceBinding b = ItemGeofenceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GeofenceViewHolder(b);
    }

    @Override
    public void onBindViewHolder(GeofenceViewHolder holder, int position) {
        ItemGeofenceBinding b = holder.getBinding();
        GeoFence f = mGeoFences.get(position);
        b.setGeoFence(f);

        final boolean[] imageLoaded = {false};
        b.geofenceMap.getViewTreeObserver().addOnPreDrawListener(() -> {
            if(imageLoaded[0]) return true;
            mPicasso.load(mStaticMap.getFenceThumbnailMapUrl(f,
                    mActivity.getResources().getDimensionPixelSize(R.dimen.image_map_width) / 2,
                    mActivity.getResources().getDimensionPixelSize(R.dimen.image_map_height) / 2))
                    .fit()
                    .centerCrop()
                    .into(b.geofenceMap);
            imageLoaded[0] = true;
            return true;
        });


        b.getRoot().setClickable(true);
        b.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GeofenceDetailActivity.class);
            intent.putExtra(GeofenceDetailActivity.EXTRA_GEOFENCE, Parcels.wrap(f));
            Pair<View, String> navPair = Pair.create(mActivity.findViewById(android.R.id.navigationBarBackground),
                    Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
            Pair<View, String> toolbarPair = Pair.create(mToolbar, "toolbar");
            Pair<View, String> p1 = Pair.create(b.geofenceMap, "map");
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(mActivity, p1, navPair, toolbarPair);

            mActivity.startActivity(intent, options.toBundle());
        });
    }


    @Override
    public int getItemCount() {
        return mGeoFences.size();
    }
}
