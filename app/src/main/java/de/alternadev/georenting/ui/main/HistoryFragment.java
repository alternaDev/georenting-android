package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.databinding.FragmentHistoryBinding;
import de.alternadev.georenting.databinding.FragmentMyGeofencesBinding;
import de.alternadev.georenting.ui.main.history.ActivityItemAdapter;
import de.alternadev.georenting.ui.main.mygeofences.GeofenceAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jhbruhn on 23.04.16.
 */
public class HistoryFragment extends Fragment {
    public static HistoryFragment newInstance() {
        HistoryFragment f = new HistoryFragment();

        return f;
    }

    @Inject
    GeoRentingService mService;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoRentingApplication) getActivity().getApplicationContext()).getComponent().inject(this);
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
        mService.getHistory(new Date().getTime() / 1000, (new Date().getTime() - 3 * 24 * 60 * 60 * 1000) / 1000) // TODO: Implement infinite Scroll.
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(history -> {
                    b.setItems(history);
                    if(history != null) {
                        RecyclerView.Adapter adapter = new ActivityItemAdapter(history, getActivity());
                        b.historyList.setAdapter(adapter);
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
}
