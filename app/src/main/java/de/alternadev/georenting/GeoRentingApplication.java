package de.alternadev.georenting;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import javax.inject.Inject;

import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class GeoRentingApplication extends Application {

    private GeoRentingComponent mComponent;
    private SessionToken mSessionToken = null;

    @Inject
    GeoRentingService mService;

    @Override
    public void onCreate() {
        super.onCreate();

        mComponent = DaggerGeoRentingComponent.builder()
                .geoRentingModule(new GeoRentingModule(this))
                .build();

        mComponent.inject(this);

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if(BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(
                                    Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())

                            .build());
        }

        initRealm();

        Timber.d("GeoRenting started.");
    }

    private void initRealm() {
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).build());
    }

    public boolean blockingSignIn() {
        if(getSessionToken() != null && !TextUtils.isEmpty(getSessionToken().token) && getSessionToken().user != null) return true;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestServerAuthCode(getString(R.string.google_server_id), false)
                .build();

        GoogleApiClient client = new GoogleApiClient.Builder(this)
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

            SessionToken sessionToken = mService.auth(new User(authCode)).toBlocking().first();
            setSessionToken(sessionToken);

            return true;
        }

        return false;
    }

    public GeoRentingComponent getComponent() {
        return mComponent;
    }

    public SessionToken getSessionToken() {return mSessionToken;}

    public void setSessionToken(SessionToken sessionToken) { this.mSessionToken = sessionToken;}
}
