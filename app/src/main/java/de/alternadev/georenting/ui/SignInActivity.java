package de.alternadev.georenting.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import javax.inject.Inject;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.databinding.ActivitySignInBinding;
import de.alternadev.georenting.ui.main.MainActivity;
import hugo.weaving.DebugLog;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@RuntimePermissions
public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLVE_ERR = 42;
    private static final int REQUEST_CODE_SIGN_IN = 44;
    private static final int REQUEST_CODE_CHECK_SETTINGS = 45;

    public static final String PREF_SIGNED_IN_BEFORE = "signedIn";
    public static final String PREF_TOKEN = "t";

    private GoogleApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    GeoRentingService mGeoRentingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getGeoRentingApplication().getComponent().inject(this);

        ActivitySignInBinding b = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        b.signInButton.setOnClickListener(this::onClickSignIn);
        b.signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.message_signing_in));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode(getString(R.string.google_server_id), false)
                .build();

        mApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mProgressDialog.show();

        if(getGeoRentingApplication().getSessionToken() != null && getGeoRentingApplication().getSessionToken().token != null && !getGeoRentingApplication().getSessionToken().token.equals("")) {
            askForLocationAccess();
            return;
        }


        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mApiClient);

        if(opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignIn(result);
        } else {
            opr.setResultCallback(this::handleSignIn);
        }
    }


    public void onClickSignIn(View v) {
        SignInActivityPermissionsDispatcher.startSignInWithCheck(this);
    }

    @NeedsPermission({Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION})
    @DebugLog
    void startSignIn() {
        mProgressDialog.show();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    @DebugLog
    private void handleSignIn(GoogleSignInResult result) {
        if(result.isSuccess() && result.getSignInAccount() != null) {
            mGeoRentingService.auth(new User(result.getSignInAccount().getServerAuthCode()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((sessionToken) -> {
                        Timber.d("Test: %s", sessionToken);
                        mProgressDialog.dismiss();
                        mPreferences.edit()
                                .putBoolean(PREF_SIGNED_IN_BEFORE, true)
                                .putString(PREF_TOKEN, sessionToken.token)
                                .apply();

                        getGeoRentingApplication().setSessionToken(sessionToken);
                        startService(new Intent(this, GcmRegistrationIntentService.class));

                        askForLocationAccess();
                    }, error -> Timber.e(error, "Could not handle Token."));
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    @DebugLog
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.e("Connection to Google+ failed: %s", connectionResult.toString());

        if(connectionResult.hasResolution()) {
            Timber.i("Attempting to use the given resolution.");
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                mApiClient.connect();
            }
        }

    }

    private void proceed() {
        mProgressDialog.dismiss();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignIn(result);
        } else if(requestCode == REQUEST_CODE_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    proceed();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    askForLocationAccess();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    @DebugLog
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SignInActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void askForLocationAccess() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    proceed();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                SignInActivity.this,
                                REQUEST_CODE_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    break;
            }
        });
    }


}
