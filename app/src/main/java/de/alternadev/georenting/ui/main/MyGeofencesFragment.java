package de.alternadev.georenting.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.ads.AdmobAds;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.databinding.FragmentMyGeofencesBinding;
import de.alternadev.georenting.databinding.ItemGeofenceBinding;
import de.alternadev.georenting.ui.CreateGeofenceActivity;
import de.alternadev.georenting.ui.GeofenceDetailActivity;
import de.alternadev.georenting.ui.main.mygeofences.GeofenceAdapter;
import hugo.weaving.DebugLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MyGeofencesFragment extends Fragment implements GeofenceAdapter.GeofenceAdapterListener {
    private static final String SAVED_INSTANCE_CURRENT_USER = "currentUser";


    public static MyGeofencesFragment newInstance(User currentUser) {
        MyGeofencesFragment f = new MyGeofencesFragment();
        Bundle args = new Bundle();
        args.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(currentUser));
        f.setArguments(args);

        return f;
    }

    @Inject
    GeoRentingService mService;

    @Inject
    AdmobAds mAds;

    private User mCurrentUser;
    private FragmentMyGeofencesBinding mBinding;
    private GeofenceAdapter mAdapter;
    private List<GeoFence> mGeoFences;

    public MyGeofencesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoRentingApplication) getActivity().getApplicationContext()).getComponent().inject(this);

        mAds.initialize(getActivity());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(getArguments() != null && getArguments().getParcelable(SAVED_INSTANCE_CURRENT_USER) != null)
            mCurrentUser = Parcels.unwrap(getArguments().getParcelable(SAVED_INSTANCE_CURRENT_USER));

        if(mCurrentUser == null)
            mCurrentUser = Parcels.unwrap(savedInstanceState.getParcelable(SAVED_INSTANCE_CURRENT_USER));

        mBinding = FragmentMyGeofencesBinding.inflate(inflater, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mBinding.geofencesList.setLayoutManager(layoutManager);

        mBinding.buttonAddGeofence.setOnClickListener(view -> {
            Intent i = new Intent(this.getActivity(), CreateGeofenceActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
        });

        mBinding.geofencesRefresh.setOnRefreshListener(() -> loadFences(mBinding));
        mBinding.geofencesRefresh.setRefreshing(true);
        loadFences(mBinding);


        mAds.loadBannerAdIntoAdView(mBinding.adView);

        // Inflate the layout for this fragment
        return mBinding.getRoot();
    }

    private void loadFences(FragmentMyGeofencesBinding b) {
        mService.getFencesBy("" + mCurrentUser.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(geoFences -> {
                    if(geoFences != null && getActivity() != null) {
                        if (mAdapter == null) {
                            mAdapter = new GeofenceAdapter(mGeoFences = geoFences, this.getActivity());
                            mAdapter.setGeofenceAdapaterListener(this);
                            b.geofencesList.setAdapter(mAdapter);
                        } else {
                            mAdapter.setGeoFences(geoFences);
                        }

                        b.setGeoFences(mGeoFences = geoFences);


                    } else {
                        b.setGeoFences(new ArrayList());
                    }
                }, t -> {
                    Timber.e(t, "Could not load Fences.");
                    if(b.getRoot() == null || b.getRoot().getContext() == null && getActivity() != null) return;

                    Snackbar.make(b.getRoot(), R.string.error_network, Snackbar.LENGTH_LONG).setAction(R.string.error_network_action_retry, v -> this.loadFences(b)).show();
                }, () -> b.geofencesRefresh.setRefreshing(false));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_geofences_toolbar, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        if(searchMenuItem == null) return;

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if(searchView == null) return;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getApplicationWindowToken(), 0);
                return false;
            }

            @Override
            @DebugLog
            public boolean onQueryTextChange(String newText) {
                if(mGeoFences != null) {
                    List<GeoFence> filteredList = new ArrayList<>();
                    for(GeoFence f : mGeoFences) {
                        if(f.name.toLowerCase().contains(newText.toLowerCase().trim())) {
                            filteredList.add(f);
                        }
                    }
                    mAdapter.setGeoFences(filteredList);
                }
                return true;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(mCurrentUser));
    }

    @Override
    public void onClick(GeoFence fence, ItemGeofenceBinding b) {
        Intent intent = new Intent(getActivity(), GeofenceDetailActivity.class);
        intent.putExtra(GeofenceDetailActivity.EXTRA_GEOFENCE, Parcels.wrap(fence));

        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(R.anim.left_to_right, R.anim.fade_back);
    }

    @Override
    public void onLongClick(GeoFence fence, ItemGeofenceBinding b) {
        PopupMenu popup = new PopupMenu(this.getActivity(), b.getRoot());
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.geofence_item_actions, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.tear_down:
                    mService.deleteGeoFence(fence.id).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((f) -> {
                                Timber.d("Removed GeoFence");
                                loadFences(mBinding);
                            }, error -> {
                                Timber.e(error, "Could not tear down GeoFence.");
                            });
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.loadFences(mBinding);
    }
}
