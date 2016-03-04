package de.alternadev.georenting.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jakewharton.processphoenix.ProcessPhoenix;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.ui.SignInActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jhbruhn on 25.08.15 for georenting-android.
 */
public class SettingsFragment extends PreferenceFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @Inject
    SharedPreferences mPreferences;

    @Inject
    GeoRentingService mApi;

    private GoogleApiClient mGoogleClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((GeoRentingApplication) getActivity().getApplication()).getComponent().inject(this);

        mGoogleClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleClient.connect();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        findPreference(getString(R.string.pref_key_account_logout)).setOnPreferenceClickListener(preference -> logOut());
    }

    private boolean logOut() {

        if(mGoogleClient.isConnected()) {

            mApi.deAuth()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(sessionToken -> {
                        mGoogleClient.clearDefaultAccountAndReconnect();
                        mPreferences.edit().putBoolean(SignInActivity.PREF_SIGNED_IN_BEFORE, false).commit();

                        ProcessPhoenix.triggerRebirth(this.getActivity(), new Intent(this.getActivity(), SignInActivity.class));
                    }, throwable -> Timber.e(throwable, "Failed to log out."));
        }

        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
