package de.alternadev.georenting.ui.main.mygeofences;

import android.support.v7.widget.RecyclerView;

import de.alternadev.georenting.databinding.ItemGeofenceBinding;

/**
 * Created by jhbruhn on 17.04.16.
 */
public class GeofenceViewHolder extends RecyclerView.ViewHolder {
    private final ItemGeofenceBinding mLayoutBinding;

    public GeofenceViewHolder(ItemGeofenceBinding b) {
        super(b.getRoot());
        mLayoutBinding = b;
    }

    public ItemGeofenceBinding getBinding() {
        return mLayoutBinding;
    }
}
