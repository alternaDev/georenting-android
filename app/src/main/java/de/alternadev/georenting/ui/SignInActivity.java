package de.alternadev.georenting.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.databinding.ActivitySignInBinding;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLVE_ERR = 42;


    private GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((GeoRentingApplication)getApplication()).getComponent().inject(this);

        ActivitySignInBinding b = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        b.signInButton.setOnClickListener(this::onClickSignIn);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API, new Plus.PlusOptions.Builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void onClickSignIn(View v) {
        mApiClient.connect();
    }

    @Override
    @DebugLog
    public void onConnected(Bundle bundle) {
        //Plus.AccountApi.getAccountName()
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    @DebugLog
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Timber.e("Connection to Google+ failed: ", connectionResult.getErrorCode());

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_RESOLVE_ERR) {
            mApiClient.connect();
        }
    }
}
