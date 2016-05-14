package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.location.LocationServices;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.alternadev.georenting.BuildConfig;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.gcm.GcmRegistrationIntentService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.auth.GoogleAuth;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.databinding.ActivityMainBinding;
import de.alternadev.georenting.ui.BaseActivity;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.settings.SettingsActivity;
import hugo.weaving.DebugLog;
import rebus.header.view.HeaderView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static String EXTRA_FRAGMENT = "fragment";

    private ActionBarDrawerToggle mDrawerToggle;
    private HeaderView mHeaderView;
    private DrawerLayout mDrawerLayout;


    @Inject
    GeoRentingService mService;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    GoogleAuth mGoogleAuth;

    private User mCurrentUser;
    private GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);


        getGeoRentingApplication().getComponent().inject(this);

        ActivityMainBinding b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleAuth.getGoogleSignInOptions())
                .addApi(LocationServices.API)
                .build();

        mDrawerToggle = new ActionBarDrawerToggle(this, b.mainDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();

        mHeaderView = new HeaderView(this, false);
        b.mainNavigationView.addHeaderView(mHeaderView);
        b.mainNavigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        SessionToken savedToken = mGoogleAuth.getSavedToken();

        if(savedToken != null && savedToken.token != null && !savedToken.token.equals("")) {
            Timber.i("Using Token.");
            getGeoRentingApplication().setSessionToken(savedToken);
        } else {
            Timber.i("Resigning in.");
            reSignIn();
            return;
        }

        loadCurrentUser();

        mDrawerLayout = b.mainDrawerLayout;

        /* Workaround for bug in AppCompat v23.0.0: https://code.google.com/p/android/issues/detail?id=183166 */
        ViewGroup appBarLayout = b.appbar;
        for (int i = 0; i < appBarLayout.getChildCount(); i++) {
            View childView = appBarLayout.getChildAt(i);
            if (!childView.isClickable()) {
                childView.setOnTouchListener((view, motionEvent) -> true);
            }
        }
        /* End workaround */

        UpdateGeofencesTask.initializeTasks(this);
    }

    private void loadCurrentUser() {
        showFragment(LoadingFragment.newInstance());
        mCurrentUser = getGeoRentingApplication().getSessionToken().user;

        if(mCurrentUser == null) {
            Timber.i("Refreshing Token.");
            mService.refreshToken(getGeoRentingApplication().getSessionToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((sessionToken) -> {
                        getGeoRentingApplication().setSessionToken(sessionToken);
                        setCurrentUser(sessionToken.user);
                    }, error -> {
                        Timber.e(error, "Could not refresh Token.");
                        reSignIn();
                    });

        } else {
            showUserInHeader(mCurrentUser);
            showFragment(MyGeofencesFragment.newInstance(mCurrentUser));
        }
    }

    private void setCurrentUser(User user) {
        mCurrentUser = user;
        showUserInHeader(user);
        showStartFragment();
    }

    private void showStartFragment() {
        if(getIntent() == null || getIntent().getStringExtra(EXTRA_FRAGMENT) == null) {
            showFragment(MyGeofencesFragment.newInstance(mCurrentUser));
            return;
        }
        String extraFragment = getIntent().getStringExtra(EXTRA_FRAGMENT);

        switch(extraFragment) {
            case "myFences":
                showFragment(MyGeofencesFragment.newInstance(mCurrentUser));
                break;
            case "history":
                showFragment(HistoryFragment.newInstance());
                break;
        }
    }

    public void onNewIntent(Intent intent){
        setIntent(intent);
        showStartFragment();
    }

    private void reSignIn() {
        mGoogleAuth.removeToken();
        OptionalPendingResult<GoogleSignInResult> opr = mGoogleAuth.getAuthTokenSilent(mApiClient);

        if(opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignIn(result);
        } else {
            opr.setResultCallback(this::handleSignIn);
        }
    }

    @DebugLog
    private void handleSignIn(GoogleSignInResult result) {
        mGoogleAuth.handleSignIn(result)
                .subscribe((sessionToken) -> {
                    setCurrentUser(sessionToken.user);
                }, error -> {
                    Timber.e(error, "Could not handle Token.");
                    startActivity(new Intent(this, SignInActivity.class));
                    finish();
                });
    }


    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.main_content_frame, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    private boolean onNavigationItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_my_geofences:
                menuItem.setChecked(true);
                showFragment(MyGeofencesFragment.newInstance(mCurrentUser));
                break;
            case R.id.nav_map:
                menuItem.setChecked(true);
                showFragment(MapFragment.newInstance());
                break;
            case R.id.nav_profile:
                menuItem.setChecked(true);
                showFragment(ProfileFragment.newInstance(mCurrentUser));
                break;
            case R.id.nav_history:
                menuItem.setChecked(true);
                showFragment(HistoryFragment.newInstance());
                break;
            case R.id.nav_settings:
                menuItem.setChecked(false);
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

        if(mDrawerLayout != null)
            mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    private void showUserInHeader(User user) {
        if(user.coverUrl != null && !user.coverUrl.isEmpty()) {
            Glide.with(this).load(user.coverUrl)
                    .into(mHeaderView.background());
        } else {
            Glide.with(this).load(R.drawable.default_background)
                    .into(mHeaderView.background());
        }
        if(user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            Glide.with(this).load(user.avatarUrl + "&sz=250")
                    .asBitmap()
                    .into(mHeaderView.avatar());
        }
        mHeaderView.username(user.name);
    }

    @Override
    protected void setStatusBarColor() {
        // Do not set the statusbarcolor here because it does weird things.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
