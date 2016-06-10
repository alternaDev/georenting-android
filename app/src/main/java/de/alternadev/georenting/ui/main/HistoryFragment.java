package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.ActivityItem;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.models.Notification;
import de.alternadev.georenting.databinding.FragmentHistoryBinding;
import de.alternadev.georenting.databinding.FragmentMyGeofencesBinding;
import de.alternadev.georenting.ui.main.history.ActivityItemAdapter;
import de.alternadev.georenting.ui.main.history.OnLoadMoreListener;
import de.alternadev.georenting.ui.main.mygeofences.GeofenceAdapter;
import hugo.weaving.DebugLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


/**
 * Created by jhbruhn on 23.04.16.
 */
public class HistoryFragment extends Fragment implements OnLoadMoreListener {

    private static final long INFINITE_SCROLL_DISTANCE = 2 * 24 * 60 * 60;

    public static HistoryFragment newInstance() {
        HistoryFragment f = new HistoryFragment();

        return f;
    }

    @Inject
    GeoRentingService mService;

    @Inject
    BriteDatabase mDatabase;

    private ActivityItemAdapter mAdapter;
    private long mLastEndTime;
    private long mLastStartTime;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoRentingApplication) getActivity().getApplicationContext()).getComponent().inject(this);

        Notification.deleteAll(mDatabase);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentHistoryBinding b = FragmentHistoryBinding.inflate(inflater, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        b.historyList.setLayoutManager(layoutManager);

        b.historyRefresh.setOnRefreshListener(() -> loadHistory(b));
        b.historyRefresh.setRefreshing(true);
        loadHistory(b);

        return b.getRoot();
    }

    private void loadHistory(FragmentHistoryBinding b) {
        mLastEndTime = (new Date().getTime() / 1000);
        mLastStartTime = mLastEndTime - INFINITE_SCROLL_DISTANCE;
        mService.getHistory(mLastEndTime, mLastStartTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(history -> {
                    b.setItems(history);
                    if(history != null) {
                        mAdapter = new ActivityItemAdapter(history, b.historyList);
                        mAdapter.setOnLoadMoreListener(this);
                        b.historyList.setAdapter(mAdapter);
                        if(history.size() == 0) return;

                        if(history.size() < ActivityItemAdapter.VISIBLE_THRESHOLD) {
                            this.onLoadMore();
                        }
                    }

                }, t -> {
                    t.printStackTrace();
                    Snackbar.make(b.getRoot(), R.string.error_network, Snackbar.LENGTH_LONG).setAction(R.string.error_network_action_retry, v -> {
                        this.loadHistory(b);
                    }).show();
                }, () -> {
                    b.historyRefresh.setRefreshing(false);
                });
    }

    @Override
    @DebugLog
    public void onLoadMore() {
        mAdapter.getActivityList().add(null);
        mAdapter.notifyItemInserted(mAdapter.getActivityList().size() - 1);

        mLastEndTime = mLastStartTime - 1;
        mLastStartTime = mLastEndTime - INFINITE_SCROLL_DISTANCE;
        mService.getHistory(mLastEndTime, mLastStartTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(history -> {
                    mAdapter.getActivityList().remove(mAdapter.getActivityList().size() - 1);
                    mAdapter.notifyItemRemoved(mAdapter.getActivityList().size());
                    for(int i = 0; i < history.size(); i++) {
                        mAdapter.getActivityList().add(history.get(i));
                        mAdapter.notifyItemInserted(mAdapter.getActivityList().size());
                    }
                    mAdapter.setLoaded();
                    if(history.size() == 0) return;
                    if(mAdapter.getActivityList().size() < ActivityItemAdapter.VISIBLE_THRESHOLD) {
                        this.onLoadMore();
                    }
                });
    }
}
