package de.alternadev.georenting.ui.main.mygeofences;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.data.api.GoogleMapsStatic;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ItemGeofenceBinding;

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
                          b.geofenceMap.getWidth(),
                          b.geofenceMap.getHeight()))
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
        void onClick(GeoFence fence, ItemGeofenceBinding v);
        void onLongClick(GeoFence fence, ItemGeofenceBinding v);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(mGeoFences.get(position).id);
    }
}
