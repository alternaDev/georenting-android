package de.alternadev.georenting.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.databinding.ActivitySignInBinding;
import hugo.weaving.DebugLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.util.async.Async;
import timber.log.Timber;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLVE_ERR = 42;
    private static final int REQUEST_CODE_REQUEST_PERMISSION = 43;
    private static final String PREF_SIGNED_IN_BEFORE = "signedIn";
    private static final String SCOPE_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";


    private GoogleApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    GeoRentingService mGeoRentingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((GeoRentingApplication) getApplication()).getComponent().inject(this);

        ActivitySignInBinding b = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        b.signInButton.setOnClickListener(this::onClickSignIn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.message_signing_in));

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API, new Plus.PlusOptions.Builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if(mPreferences.getBoolean(PREF_SIGNED_IN_BEFORE, false)) {
            mApiClient.connect();
            mProgressDialog.show();
        }
    }

    public void onClickSignIn(View v) {
        mProgressDialog.show();
        mApiClient.connect();
    }

    @Override
    @DebugLog
    public void onConnected(Bundle bundle) {
        Async.start(this::getGoogleAuthToken, Schedulers.newThread())
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap((user) -> mGeoRentingService.auth((User) user))
            .subscribe((sessionToken) -> {
                Timber.d("Test: " + sessionToken);
                mProgressDialog.dismiss();
                mPreferences.edit().putBoolean(PREF_SIGNED_IN_BEFORE, true).apply();

                ((GeoRentingApplication) getApplication()).setSessionToken(sessionToken);
                startService(new Intent(this, GcmRegistrationIntentService.class));
            });
    }

    private User getGoogleAuthToken() {
        try {
            return new User(GoogleAuthUtil
                    .getToken(this,
                            Plus.AccountApi.getAccountName(mApiClient),
                            "oauth2:" + SCOPE_PROFILE));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UserRecoverableAuthException e) {
            e.printStackTrace();
            startActivityForResult(e.getIntent(), REQUEST_CODE_REQUEST_PERMISSION);
        } catch (GoogleAuthException e) {
            e.printStackTrace();
        }
        return new User();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    @DebugLog
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("Connection to Google+ failed: " + connectionResult.toString());

        if(connectionResult.hasResolution()) {
            Timber.i("Attempting to use the given resolution.");
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                // Versuchen Sie erneut, die Verbindung herzustellen.
                mApiClient.connect();
            }
        }

    }

    private void proceed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_RESOLVE_ERR || requestCode == REQUEST_CODE_REQUEST_PERMISSION) {
            mApiClient.connect();
        }
    }
}
