package de.alternadev.georenting.ui.main.history;

import android.support.v7.widget.RecyclerView;

import de.alternadev.georenting.databinding.ItemActivityBinding;

/**
 * Created by jhbruhn on 23.04.16.
 */
public class ActivityItemViewHolder extends RecyclerView.ViewHolder {
    private final ItemActivityBinding mBinding;

    public ActivityItemViewHolder(ItemActivityBinding b) {
        super(b.getRoot());
        mBinding = b;
    }

    public ItemActivityBinding getBinding() {
        return mBinding;
    }

}
