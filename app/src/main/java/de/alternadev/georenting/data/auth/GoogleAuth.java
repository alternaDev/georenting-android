package de.alternadev.georenting.data.auth;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Date;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.ui.SignInActivity;
import hugo.weaving.DebugLog;
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

    private FirebaseAuth mAuth;

    public GoogleAuth(Application application) {
        ((GeoRentingApplication) application).getComponent().inject(this);

        mAuth = FirebaseAuth.getInstance();
    }

    public OptionalPendingResult<GoogleSignInResult> getAuthTokenSilent(GoogleApiClient apiClient) {
        return Auth.GoogleSignInApi.silentSignIn(apiClient);
    }

    @DebugLog
    public Observable<SessionToken> handleSignIn(GoogleSignInResult result) {
        if(result.isSuccess() && result.getSignInAccount() != null) {
            return Observable.create(subscriber -> {
                subscriber.onStart();
                firebaseAuthWithGoogle(result.getSignInAccount()).addOnSuccessListener(authResult -> {
                    Timber.d("Got Auth REsult!");

                    authResult.getUser().getToken(false).addOnSuccessListener(getTokenResult -> {
                        Timber.d("Got Token REsult:%s", getTokenResult.getToken());

                        mGeoRentingService.auth(new User(getTokenResult.getToken()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .map(sessionToken -> {
                                    Timber.d("Got Session Token!");
                                    if (sessionToken.token == null || sessionToken.token.equals("")) {
                                        return sessionToken;
                                    }

                                    mPreferences.edit()
                                            .putBoolean(PREF_SIGNED_IN_BEFORE, true)
                                            .apply();

                                    mApp.setSessionToken(sessionToken);


                                    return sessionToken;
                                }).subscribe((sessionToken) -> {
                                    subscriber.onNext(sessionToken);
                                    subscriber.onCompleted();
                                }, error -> {
                                    subscriber.onError(error);
                                    subscriber.onCompleted();
                                });
                    });
                });
            });
        } else {
            return Observable.error(new Exception());
        }
    }

    @DebugLog
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

            Task<AuthResult> result = firebaseAuthWithGoogle(r.getSignInAccount());

            String authCode = result.getResult().getUser().getToken(false).getResult().getToken();

            SessionToken sessionToken = mGeoRentingService.auth(new User(authCode)).toBlocking().first();
            mApp.setSessionToken(sessionToken);

            return true;
        }

        return false;
    }

    @DebugLog
    private Task<AuthResult> firebaseAuthWithGoogle(GoogleSignInAccount account) {
        return mAuth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null));
    }

    public void signOut(GoogleApiClient mGoogleClient) {
        mAuth.signOut();
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
        long expiration = JWTTool.getExpiration(token);
        return !new Date(expiration* 1000).after(new Date());
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
                .requestIdToken(mApp.getString(R.string.google_server_id))
                //.requestServerAuthCode(mApp.getString(R.string.google_server_id), false)
                .build();
        return gso;
    }
}
