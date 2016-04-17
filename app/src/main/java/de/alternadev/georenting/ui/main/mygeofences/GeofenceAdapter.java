package de.alternadev.georenting.ui.main.mygeofences;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ItemGeofenceBinding;

/**
 * Created by jhbruhn on 17.04.16.
 */

public class GeofenceAdapter extends RecyclerView.Adapter<GeofenceViewHolder> {

    private List<GeoFence> mGeoFences;

    public GeofenceAdapter(List<GeoFence> geoFences) {
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
        GeoFence f = mGeoFences.get(position);
        b.setGeoFence(f);
        b.geofenceMap.onCreate(null);
        b.geofenceMap.setClickable(false);
        b.geofenceMap.getMapAsync((googleMap -> {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(f.centerLat, f.centerLon)));
            googleMap.addCircle(new CircleOptions().center(new LatLng(f.centerLat, f.centerLon)).radius(f.radius));
        }));
    }


    @Override
    public int getItemCount() {
        return mGeoFences.size();
    }
}
