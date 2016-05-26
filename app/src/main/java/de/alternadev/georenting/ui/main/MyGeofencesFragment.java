package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.gms.location.Geofence;

import org.parceler.Parcels;

import java.util.ArrayList;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
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

    private User mCurrentUser;
    private FragmentMyGeofencesBinding mBinding;
    private GeofenceAdapter mAdapater;

    public MyGeofencesFragment() {
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

        // Inflate the layout for this fragment
        return mBinding.getRoot();
    }

    private void loadFences(FragmentMyGeofencesBinding b) {
        mService.getFencesBy("" + mCurrentUser.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(geoFences -> {
                    if(geoFences != null) {
                        if (mAdapater == null) {
                            GeofenceAdapter adapter = new GeofenceAdapter(geoFences, this.getActivity());
                            adapter.setGeofenceAdapaterListener(this);
                            b.geofencesList.setAdapter(adapter);
                        } else {
                            mAdapater.setGeoFences(geoFences);
                        }

                        b.setGeoFences(geoFences);


                    } else {
                        b.setGeoFences(new ArrayList());
                    }
                }, t -> {
                    Snackbar.make(b.getRoot(), R.string.error_network, Snackbar.LENGTH_LONG).setAction(R.string.error_network_action_retry, v -> {
                        this.loadFences(b);
                    }).show();
                }, () -> {
                    b.geofencesRefresh.setRefreshing(false);
                });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVED_INSTANCE_CURRENT_USER, Parcels.wrap(mCurrentUser));
    }

    @Override
    public void onClick(GeoFence fence, ItemGeofenceBinding b) {
        Toolbar tb = ((MainActivity) getActivity()).getToolbar();

        Intent intent = new Intent(getActivity(), GeofenceDetailActivity.class);
        intent.putExtra(GeofenceDetailActivity.EXTRA_GEOFENCE, Parcels.wrap(fence));
        Pair<View, String> navPair = Pair.create(getActivity().findViewById(android.R.id.navigationBarBackground),
                Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
        Pair<View, String> toolbarPair = Pair.create(tb, "toolbar");
        Pair<View, String> p1 = Pair.create(b.geofenceMap, "map");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), p1, navPair, toolbarPair);

        getActivity().startActivityForResult(intent, 0, options.toBundle());
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
}
