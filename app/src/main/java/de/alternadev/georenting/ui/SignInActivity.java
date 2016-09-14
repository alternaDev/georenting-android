package de.alternadev.georenting.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.location.LocationServices;

import javax.inject.Inject;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.auth.GoogleAuth;
import de.alternadev.georenting.data.tasks.RegisterFcmTask;
import de.alternadev.georenting.databinding.ActivitySignInBinding;
import de.alternadev.georenting.ui.main.MainActivity;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLVE_ERR = 42;
    private static final int REQUEST_CODE_SIGN_IN = 44;

    private GoogleApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    private OnboarderActivityHelper mOnboarderHelper;


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

        mOnboarderHelper = new OnboarderActivityHelper(this, IntroActivity.class);
        mOnboarderHelper.show(savedInstanceState);
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
                    RegisterFcmTask.scheduleRegisterFcm(this);
                    logSignIn(sessionToken.user);
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
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        mOnboarderHelper.onSaveInstanceState(b);
    }




}
