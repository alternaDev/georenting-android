package de.alternadev.georenting.ui.main.mygeofences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.GoogleMapsStatic;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ItemGeofenceBinding;
import de.alternadev.georenting.ui.GeofenceDetailActivity;
import hugo.weaving.DebugLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jhbruhn on 17.04.16.
 */

public class GeofenceAdapter extends RecyclerView.Adapter<GeofenceViewHolder> {

    @Inject
    GoogleMapsStatic mStaticMap;
    @Inject
    Picasso mPicasso;

    private List<GeoFence> mGeoFences;

    private GeofenceAdapterListener mListener;

    public GeofenceAdapter(List<GeoFence> geoFences, Activity ctx) {
        this.setHasStableIds(true);
        mGeoFences = geoFences;
        ((GeoRentingApplication) ctx.getApplication()).getComponent().inject(this);
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
                          b.geofenceMap.getWidth() / 2,
                          b.geofenceMap.getHeight() / 2))
                          .fit()
                          .centerCrop()
                          .into(b.geofenceMap);
            imageLoaded[0] = true;
            return true;
        });


        b.getRoot().setClickable(true);
        b.getRoot().setOnClickListener(v -> {
            if(mListener != null)
                mListener.onClick(f, b);
        });
        b.getRoot().setOnLongClickListener(v -> {
            if(mListener != null)
                mListener.onLongClick(f, b);
            return true;
        });
    }

    public void setGeofenceAdapaterListener(GeofenceAdapterListener l) {
        this.mListener = l;
    }

    public void setGeoFences(List<GeoFence> list) {
        for(int i = 0; i < mGeoFences.size(); i++) {
            boolean contains = false;
            for(GeoFence f : list) {
                if(f.id.equals(mGeoFences.get(i).id)) {
                    contains = true;
                }
            }
            if (!contains) {
                mGeoFences.remove(i);
                this.notifyItemRemoved(i);
            }
        }
        for(GeoFence f : list) {
            boolean contains = false;
            for(GeoFence fence : mGeoFences) {
                if(f.id.equals(fence.id)) {
                    contains = true;
                }
            }
            if(!contains) {
                mGeoFences.add(f);
                this.notifyItemInserted(mGeoFences.size());
            }
        }
    }


    @Override
    public int getItemCount() {
        return mGeoFences.size();
    }

    public interface GeofenceAdapterListener {
        public void onClick(GeoFence fence, ItemGeofenceBinding v);
        public void onLongClick(GeoFence fence, ItemGeofenceBinding v);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(mGeoFences.get(position).id);
    }
}
