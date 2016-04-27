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

import java.security.Key;
import java.util.Date;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
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

    public OptionalPendingResult<GoogleSignInResult> getAuthTokenSilent(GoogleApiClient apiClient) {
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

        GoogleSignInOptions gso = getGoogleSignInOptions();

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

        GoogleSignInResult r = getAuthTokenSilent(client).await();

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

    public boolean getTokenExpired(String token) {
        // EXTREMELY DIRTY HACK

        final int[] exp = {-1};
        try {
            Jwts.parser().setSigningKeyResolver(new SigningKeyResolver() {
                @Override
                public Key resolveSigningKey(JwsHeader header, Claims claims) {
                    exp[0] = (int) header.get("exp");
                    return null;
                }

                @Override
                public Key resolveSigningKey(JwsHeader header, String plaintext) {
                    exp[0] = (int) header.get("exp");
                    return null;
                }
            }).parse(token);

        } catch(Exception e) {
            if(exp[0] != -1) {
                if(new Date((long) exp[0] * 1000).after(new Date())) {
                    return false;
                }
            }
            Timber.e(e, "Could not parse JWT.");
        }

        return true;
    }

    public SessionToken getSavedToken() {
        if(mApp.getSessionToken() != null && !TextUtils.isEmpty(mApp.getSessionToken().token)) {
            Timber.i("Using token from App");
            return mApp.getSessionToken();
        }
        String token = mPreferences.getString(GoogleAuth.PREF_TOKEN, "");
        if(token.equals("")) return null;

        if(getTokenExpired(token)) {
            Timber.e("Token is expired.");
            return null;
        }

        SessionToken t = new SessionToken();
        t.token = token;
        Timber.i("Using token from Prefs %s", token);
        return t;
    }

    public GoogleSignInOptions getGoogleSignInOptions() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode(mApp.getString(R.string.google_server_id), false)
                .build();
        return gso;
    }
}