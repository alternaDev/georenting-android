package de.alternadev.georenting;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
//import android.support.multidex.MultiDex;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.MapView;
import com.google.firebase.crash.FirebaseCrash;

import javax.inject.Inject;

import dagger.Component;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.UpgradeSettings;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.auth.GoogleAuth;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GeoRentingApplication extends Application {

    private GeoRentingComponent mComponent;
    private SessionToken mSessionToken = null;
    private UpgradeSettings mUpgradeSettings = null;
    private boolean mMapViewCached;

    @Inject
    GeoRentingService mService;
    @Inject
    SharedPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        setComponent(DaggerGeoRentingComponent.builder()
                .geoRentingModule(new GeoRentingModule(this))
                .build());

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    FirebaseCrash.logcat(priority, tag, message);
                    if (t != null) {
                        FirebaseCrash.report(t);
                    }
                }
            });
        }

        if(BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        if (BuildConfig.DEBUG && BuildConfig.VERSION_CODE >= 23) {
            // Enable StrictMode
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        Timber.d("GeoRenting started.");

        loadSettings();
    }

    public void setComponent(GeoRentingComponent c) {
        mComponent = c;
        mComponent.inject(this);
    }

    private void loadSettings() {
        mService.getUpgradeSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((settings) -> {
                    this.mUpgradeSettings = settings;
                });
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    public GeoRentingComponent getComponent() {
        return mComponent;
    }

    public UpgradeSettings getUpgradeSettings() {
        return mUpgradeSettings;
    }

    public SessionToken getSessionToken() {return mSessionToken;}

    public void setSessionToken(SessionToken sessionToken) {
        this.mSessionToken = sessionToken;
        if(sessionToken != null && sessionToken.token != null)
            mPreferences.edit().putString(GoogleAuth.PREF_TOKEN, sessionToken.token).apply();
    }

    public void createMapViewCacheIfNecessary() {
        if(mMapViewCached) return;
        new Thread(() -> {
            try {
                MapView mv = new MapView(getApplicationContext());
                mv.onCreate(null);
                mv.onPause();
                mv.onDestroy();
            }catch (Exception ignored){

            }
        }).start();
        mMapViewCached = true;
    }
}
