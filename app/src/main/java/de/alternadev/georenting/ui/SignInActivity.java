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
import de.alternadev.georenting.data.auth.GoogleAuth;
import de.alternadev.georenting.databinding.ActivitySignInBinding;
import de.alternadev.georenting.ui.main.MainActivity;
import hugo.weaving.DebugLog;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLVE_ERR = 42;
    private static final int REQUEST_CODE_SIGN_IN = 44;


    private GoogleApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    GeoRentingService mGeoRentingService;

    @Inject
    GoogleAuth mGoogleAuth;

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

        GoogleSignInOptions gso = mGoogleAuth.getGoogleSignInOptions();

        b.signInButton.setScopes(gso.getScopeArray());

        mApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    @DebugLog
    protected void onStart() {
        super.onStart();
        mProgressDialog.show();

        if(getGeoRentingApplication().getSessionToken() != null && getGeoRentingApplication().getSessionToken().token != null && !getGeoRentingApplication().getSessionToken().token.equals("")) {
            Timber.i("We seem to have a token. Starting Main Activity.");
            startMainActivity();
            return;
        }

        OptionalPendingResult<GoogleSignInResult> opr = mGoogleAuth.getAuthTokenSilent(mApiClient);

        if(opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignIn(result);
        } else {
            opr.setResultCallback(this::handleSignIn);
        }
    }


    public void onClickSignIn(View v) {
        startSignIn();
    }

    @DebugLog
    void startSignIn() {
        mProgressDialog.show();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    @DebugLog
    private void handleSignIn(GoogleSignInResult result) {
        mGoogleAuth.handleSignIn(result)
                .subscribe((sessionToken) -> {
                    Timber.d("Test: %s", sessionToken);
                    mProgressDialog.dismiss();
                    startService(new Intent(this, GcmRegistrationIntentService.class));
                    startMainActivity();
                }, error -> {
                    Timber.e(error, "Could not handle Token.");
                    mProgressDialog.dismiss();
                });
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

    private void startMainActivity() {
        mProgressDialog.dismiss();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    @DebugLog
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignIn(result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SignInActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }




}
