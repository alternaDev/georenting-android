package de.alternadev.georenting.ui.main.history;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import de.alternadev.georenting.data.api.model.ActivityItem;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.databinding.ItemActivityBinding;
import de.alternadev.georenting.databinding.ItemGeofenceBinding;
import de.alternadev.georenting.ui.main.mygeofences.GeofenceViewHolder;

/**
 * Created by jhbruhn on 23.04.16.
 */
public class ActivityItemAdapter extends RecyclerView.Adapter<ActivityItemViewHolder> {
    private final List<ActivityItem> mActivityItems;

    public ActivityItemAdapter(List<ActivityItem> history, Activity activity) {
        mActivityItems = history;
    }

    @Override
    public ActivityItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemActivityBinding b = ItemActivityBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ActivityItemViewHolder(b);
    }

    @Override
    public void onBindViewHolder(ActivityItemViewHolder holder, int position) {
        ActivityItem i = mActivityItems.get(position);
        holder.getBinding().setItem(i);
    }

    @Override
    public int getItemCount() {
        return mActivityItems.size();
    }
}
