package de.alternadev.georenting.ui.main.history;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.model.ActivityItem;
import de.alternadev.georenting.databinding.ItemActivityBinding;

/**
 * Created by jhbruhn on 23.04.16.
 */
public class ActivityItemAdapter extends RecyclerView.Adapter {
    private final List<ActivityItem> mActivityItems;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADER = 1;
    public static final int VISIBLE_THRESHOLD = 7;

    private int totalItemCount;
    private int lastVisibleItem;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public ActivityItemAdapter(List<ActivityItem> history, RecyclerView recyclerView) {
        mActivityItems = history;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                .getLayoutManager();


        recyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView,
                                           int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager
                                .findLastVisibleItemPosition();
                        if (!loading
                                && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
                            // End has been reached
                            // Do something
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                            loading = true;
                        }
                    }
                });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ITEM) {
            ItemActivityBinding b = ItemActivityBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ActivityItemViewHolder(b);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_history_progress, parent, false);
            return new LoadingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(mActivityItems.get(position) != null) {
            ActivityItem i = mActivityItems.get(position);
            ((ActivityItemViewHolder) holder).getBinding().setItem(i);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mActivityItems.get(position) == null ? VIEW_TYPE_LOADER : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mActivityItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }

    public List<ActivityItem> getActivityList() {
        return mActivityItems;
    }
}
