package de.alternadev.georenting.data.auth;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.util.TaskUtil;
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
    @Named("unAuthed")
    GeoRentingService mGeoRentingService;

    @Inject
    GeoRentingApplication mApp;

    @Inject
    SharedPreferences mPreferences;

    private final FirebaseAuth mAuth;

    public GoogleAuth(Application application) {
        ((GeoRentingApplication) application).getComponent().inject(this);

        FirebaseAuth auth = null;
        try {
            auth = FirebaseAuth.getInstance();
        } catch (IllegalStateException e) {
            Timber.e(e, "Could not get FirebaseAuth");
        }
        mAuth = auth;
    }

    public OptionalPendingResult<GoogleSignInResult> getAuthTokenSilent(GoogleApiClient apiClient) {
        return Auth.GoogleSignInApi.silentSignIn(apiClient);
    }

    public Observable<SessionToken> handleSignIn(GoogleSignInResult result) {
        if(result.isSuccess() && result.getSignInAccount() != null) {
            return firebaseAuthWithGoogle(result.getSignInAccount()).switchMap( authResult -> startAuthTokenFetch());
        } else {
            return Observable.error(new Exception());
        }
    }

    public Observable<SessionToken> startAuthTokenFetch() {
        return Observable.create(subscriber -> {
            subscriber.onStart();
            if(mAuth.getCurrentUser() == null) {
                subscriber.onError(new Exception("Not user signed in."));
                return;
            }

            mAuth.getCurrentUser().getToken(false).addOnSuccessListener(getTokenResult -> {
                Timber.d("Got Token REsult:%s", getTokenResult.getToken());

                mGeoRentingService.auth(new User(getTokenResult.getToken()))
                        .retry(2)
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


            try {
                String authCode = TaskUtil.waitForTask(firebaseAuthWithGoogle(r.getSignInAccount()).toBlocking().first().getUser().getToken(false)).getToken();
                SessionToken sessionToken = mGeoRentingService.auth(new User(authCode)).retry(2).toBlocking().single();
                mApp.setSessionToken(sessionToken);
            } catch(Exception e) {
                return false;
            }

            return true;
        }

        return false;
    }



    private Observable<AuthResult> firebaseAuthWithGoogle(GoogleSignInAccount account) {
        return Observable.create(subscriber -> {
            subscriber.onStart();
            mAuth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
            .addOnSuccessListener(authResult -> {
                Timber.d("Got Auth REsult!");

                subscriber.onNext(authResult);
                subscriber.onCompleted();

            })
            .addOnFailureListener(e -> {
                Timber.d("Got Auth REsult!");

                subscriber.onError(e);
                subscriber.onCompleted();
            });
        });
    }

    public void signOut(GoogleApiClient mGoogleClient) {
        mAuth.signOut();
        mGoogleClient.clearDefaultAccountAndReconnect();
        mPreferences.edit()
                .putBoolean(GoogleAuth.PREF_SIGNED_IN_BEFORE, false)
                .apply();
        removeToken();
    }

    @DebugLog
    public void removeToken() {
        mPreferences.edit()
                .remove(GoogleAuth.PREF_TOKEN)
                .apply();
        mApp.setSessionToken(null);
    }

    private boolean getTokenExpired(String token) {
        long expiration = JWTTool.getExpiration(token);
        return !new Date(expiration* 1000).after(new Date());
    }

    @DebugLog
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
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(mApp.getString(R.string.google_server_id))
                //.requestServerAuthCode(mApp.getString(R.string.google_server_id), false)
                .build();
    }
}
