package de.alternadev.georenting;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
//import android.support.multidex.MultiDex;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.google.android.gms.maps.MapView;
import com.google.firebase.crash.FirebaseCrash;

import javax.inject.Inject;

import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.UpgradeSettings;
import de.alternadev.georenting.data.auth.GoogleAuth;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GeoRentingApplication extends Application {

    private GeoRentingComponent mComponent;
    private SessionToken mSessionToken = null;
    private Observable<UpgradeSettings> mUpgradeSettings = null;
    private boolean mMapViewCached;

    @Inject
    GeoRentingService mService;
    @Inject
    SharedPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        // Evil but works.
        RxJavaPlugins.getInstance().registerErrorHandler(new RxJavaErrorHandler() {
            @Override
            public void handleError(Throwable e) {
                super.handleError(e);
                Timber.e(e, "Uncaught Rx Error.");
            }
        });


        setComponent(DaggerGeoRentingComponent.builder()
                .geoRentingModule(new GeoRentingModule(this))
                .build());

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    try {
                        FirebaseCrash.logcat(priority, tag, message);
                        if (t != null) {
                            FirebaseCrash.report(t);
                        }
                    } catch(IllegalStateException e) {
                        e.printStackTrace();
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
        this.mUpgradeSettings = mService.getUpgradeSettings()
                .cache()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    public GeoRentingComponent getComponent() {
        return mComponent;
    }

    public Observable<UpgradeSettings> getUpgradeSettings() {
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
