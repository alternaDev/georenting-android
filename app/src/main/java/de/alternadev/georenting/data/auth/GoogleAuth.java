package de.alternadev.georenting.data.auth;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by jhbruhn on 25.04.16.
 */
public class GoogleAuth {
    public static final String PREF_SIGNED_IN_BEFORE = "signedIn";
    public static final String PREF_TOKEN = "t";

    @Inject
    GeoRentingService mGeoRentingService;

    @Inject
    GeoRentingApplication mApp;

    @Inject
    SharedPreferences mPreferences;

    public GoogleAuth(Application application) {
        ((GeoRentingApplication) application).getComponent().inject(this);
    }

    public OptionalPendingResult<GoogleSignInResult> getAuthToken(GoogleApiClient apiClient) {
        return Auth.GoogleSignInApi.silentSignIn(apiClient);
    }

    public Observable<SessionToken> handleSignIn(GoogleSignInResult result) {
        if(result.isSuccess() && result.getSignInAccount() != null) {
            Timber.d(result.getSignInAccount().toString());

            return mGeoRentingService.auth(new User(result.getSignInAccount().getServerAuthCode()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(sessionToken -> {
                        if(sessionToken.token == null || sessionToken.token.equals("")) {
                            return sessionToken;
                        }

                        mPreferences.edit()
                                .putBoolean(PREF_SIGNED_IN_BEFORE, true)
                                .putString(PREF_TOKEN, sessionToken.token)
                                .apply();

                        mApp.setSessionToken(sessionToken);

                        return sessionToken;
                    });
                    /*.subscribe((sessionToken) -> {




                    }, error -> Timber.e(error, "Could not handle Token."));*/
        } else {
            return Observable.error(new Exception());
           // mProgressDialog.dismiss();
        }
    }

    public boolean blockingSignIn() {
        if(mApp.getSessionToken() != null && !TextUtils.isEmpty(mApp.getSessionToken().token) && mApp.getSessionToken().user != null) return true;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode(mApp.getApplicationContext().getString(R.string.google_server_id), false)
                .build();

        GoogleApiClient client = new GoogleApiClient.Builder(mApp)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        client.blockingConnect();

        GoogleSignInResult r = Auth.GoogleSignInApi.silentSignIn(client).await();

        if(r.isSuccess()) {
            if(r.getSignInAccount() == null) return false;
            String authCode = r.getSignInAccount().getServerAuthCode();
            if(authCode == null) return false;

            SessionToken sessionToken = mGeoRentingService.auth(new User(authCode)).toBlocking().first();
            mApp.setSessionToken(sessionToken);

            return true;
        }

        return false;
    }

    public void signOut(GoogleApiClient mGoogleClient) {
        mGoogleClient.clearDefaultAccountAndReconnect();
        mPreferences.edit()
                .putBoolean(GoogleAuth.PREF_SIGNED_IN_BEFORE, false)
                .commit();
        removeToken();
    }

    public void removeToken() {
        mPreferences.edit()
                .remove(GoogleAuth.PREF_TOKEN)
                .commit();
        mApp.setSessionToken(null);
    }
}
