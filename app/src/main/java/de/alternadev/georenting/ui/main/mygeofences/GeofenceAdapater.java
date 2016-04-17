package de.alternadev.georenting.ui.main.mygeofences;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ItemGeofenceBinding;

/**
 * Created by jhbruhn on 17.04.16.
 */

public class GeofenceAdapater extends RecyclerView.Adapter<GeofenceViewHolder> {

    private List<GeoFence> mGeoFences;

    public GeofenceAdapater(List<GeoFence> geoFences) {
        mGeoFences = geoFences;
    }

    @Override
    public GeofenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemGeofenceBinding b = ItemGeofenceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GeofenceViewHolder(b);
    }

    @Override
    public void onBindViewHolder(GeofenceViewHolder holder, int position) {
        ItemGeofenceBinding b = holder.getBinding();
        b.setGeoFence(mGeoFences.get(position));
    }


    @Override
    public int getItemCount() {
        return mGeoFences.size();
    }
}
