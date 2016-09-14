package de.alternadev.georenting.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.ads.AdmobAds;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.auth.GoogleAuth;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.databinding.ActivityMainBinding;
import de.alternadev.georenting.ui.BaseActivity;
import de.alternadev.georenting.ui.IntroActivity;
import de.alternadev.georenting.ui.OnboarderActivityHelper;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.settings.SettingsActivity;
import hugo.weaving.DebugLog;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rebus.header.view.HeaderCompactView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@RuntimePermissions
public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE_CHECK_SETTINGS = 45;
    private static final int REQUEST_CODE_INVITE = 43;

    public static final String EXTRA_FRAGMENT = "fragment";

    public static final String FRAGMENT_MAP = "map";
    public static final String FRAGMENT_PROFILE = "profile";
    public static final String FRAGMENT_MY_FENCES = "myGeofences";
    public static final String FRAGMENT_HISTORY = "history";

    private GoogleApiClient mApiClient;

    private ActionBarDrawerToggle mDrawerToggle;
    private HeaderCompactView mHeaderView;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private User mCurrentUser;
    private String mCurrentFragment = FRAGMENT_MY_FENCES;
    private Fragment mContent;
    private OnboarderActivityHelper mOnboarderHelper;

    @Inject
    GeoRentingService mService;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    GoogleAuth mGoogleAuth;

    @Inject
    Picasso mPicasso;

    @Inject
    AdmobAds mAds;

    @Override
    @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        getGeoRentingApplication().getComponent().inject(this);

        ActivityMainBinding b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleAuth.getGoogleSignInOptions())
                .addApi(LocationServices.API)
                .build();

        mDrawerLayout = b.mainDrawerLayout;

        if(mDrawerLayout != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
            mDrawerToggle.syncState();
        }

        mHeaderView = new HeaderCompactView(this, false);

        b.mainNavigationView.addHeaderView(mHeaderView);
        b.mainNavigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        SessionToken savedToken = mGoogleAuth.getSavedToken();

        if(savedToken != null && savedToken.token != null && !savedToken.token.equals("")) {
            Timber.i("Using Saved Token.");
            getGeoRentingApplication().setSessionToken(savedToken);
        } else {
            Timber.i("Resigning in.");
            refreshToken();
            return;
        }

        UpdateGeofencesTask.initializeTasks(this);

        mCurrentUser = getGeoRentingApplication().getSessionToken().user;

        if(savedInstanceState != null) {
            mCurrentFragment = savedInstanceState.getString(EXTRA_FRAGMENT, FRAGMENT_MY_FENCES);
        }

        showFragment(LoadingFragment.newInstance());

        getGeoRentingApplication().createMapViewCacheIfNecessary();

        mOnboarderHelper = new OnboarderActivityHelper(this, IntroActivity.class);
        mOnboarderHelper.show(savedInstanceState);
    }

    @DebugLog
    private void loadCurrentUser() {
        if(mCurrentUser == null) {
            reloadCurrentUser();
        } else {
            showUserInHeader(mCurrentUser);
            logUserAnalytics(mCurrentUser);
        }
    }

    @DebugLog
    private void reloadCurrentUser() {
        Timber.i("Refreshing User.");
        mService.getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((user) -> {
                    getGeoRentingApplication().getSessionToken().user = user;
                    setCurrentUser(user);
                }, error -> {
                    Timber.d("Could not get Current User.");
                    refreshToken();
                });
    }

    @DebugLog
    private void refreshToken() {
        mGoogleAuth.startAuthTokenFetch().subscribe(sessionToken -> {
            getGeoRentingApplication().setSessionToken(sessionToken);
            setCurrentUser(sessionToken.user);
        }, error -> {
            Timber.d("Could not refresh Token.");
            reSignIn();
        });
    }

    @DebugLog
    private void setCurrentUser(User user) {
        mCurrentUser = user;
        showUserInHeader(user);
        showCurrentFragment();
    }

    private void showCurrentFragment() {
        switch(this.mCurrentFragment) {
            case FRAGMENT_MY_FENCES:
                showFragment(MyGeofencesFragment.newInstance(mCurrentUser));
                mToolbar.setTitle(R.string.main_drawer_my_geofences);
                break;
            case FRAGMENT_PROFILE:
                showFragment(ProfileFragment.newInstance(mCurrentUser));
                mToolbar.setTitle(R.string.main_drawer_profile);
                break;
            case FRAGMENT_MAP:
                showFragment(MapFragment.newInstance());
                mToolbar.setTitle(R.string.main_drawer_map);
                break;
            case FRAGMENT_HISTORY:
                showFragment(HistoryFragment.newInstance());
                mToolbar.setTitle(R.string.main_drawer_history);
                break;
        }
    }

    private void showStartFragment() {
        if(getIntent() == null || getIntent().getStringExtra(EXTRA_FRAGMENT) == null) {
            showCurrentFragment();
            return;
        }
        String extraFragment = getIntent().getStringExtra(EXTRA_FRAGMENT);

        if(extraFragment != null && !TextUtils.isEmpty(extraFragment)) {
            mCurrentFragment = extraFragment;
        }

        showCurrentFragment();

        setIntent(null);
    }

    public void onNewIntent(Intent intent){
        setIntent(intent);
        showStartFragment();
    }

    @DebugLog
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mOnboarderHelper.onSaveInstanceState(outState);
        outState.putString(EXTRA_FRAGMENT, mCurrentFragment);
    }


    private void showFragment(Fragment fragment) {
        if(fragment.getClass().isInstance(mContent)) return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.main_content_frame, fragment);

        transaction.commit();

        mContent = fragment;
    }

    private boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_my_geofences:
                menuItem.setChecked(true);
                this.mCurrentFragment = FRAGMENT_MY_FENCES;
                showCurrentFragment();
                break;
            case R.id.nav_map:
                menuItem.setChecked(true);
                this.mCurrentFragment = FRAGMENT_MAP;
                showCurrentFragment();
                break;
            case R.id.nav_profile:
                menuItem.setChecked(true);
                this.mCurrentFragment = FRAGMENT_PROFILE;
                showCurrentFragment();
                break;
            case R.id.nav_history:
                menuItem.setChecked(true);
                this.mCurrentFragment = FRAGMENT_HISTORY;
                showCurrentFragment();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_support:
                onSupportClicked();
                break;
            case R.id.nav_invite:
                onInviteClicked();
                break;
        }

        if(mDrawerLayout != null)
            mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void onSupportClicked() {
        String url = "http://magicpedarecords.de";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ActivityCompat.getColor(this, R.color.primary_color));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,
                Uri.parse("app://" + getPackageName()));
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    private void onInviteClicked() {
        mAds.loadIntersitial(this);
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.app_invite_title))
                .setMessage(getString(R.string.app_invite_message))
                //.setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                //.setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.app_invite_cta))
                .build();
        startActivityForResult(intent, REQUEST_CODE_INVITE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(mDrawerToggle != null)
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerLayout != null) return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }


    private void showUserInHeader(User user) {
        mHeaderView.background().setBackgroundColor(ActivityCompat.getColor(this, R.color.dark_primary_color));
        if(user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            mPicasso.load(user.avatarUrl)
                    .into(mHeaderView.avatar());
        }
        mHeaderView.username(user.name);
        mHeaderView.email(getString(R.string.n_money_units, user.balance));
        MainActivityPermissionsDispatcher.askForLocationAccessWithCheck(this);
    }

    @Override
    protected void setStatusBarColor() {
        // Do not set the statusbarcolor here because it does weird things.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForLocation(PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_location_rationale)
                .setPositiveButton(R.string.button_allow, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.button_deny, (dialog, button) -> request.cancel())
                .show();
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void askForLocationAccess() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                MainActivity.this,
                                REQUEST_CODE_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    askForLocationAccess();
                    break;
                default:
                    break;
            }
        } else if(requestCode == REQUEST_CODE_INVITE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    mAds.showInterstitialOrContinue(this, this::logSentInvite);
                    break;
            }
        }
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCurrentUser = null;
        this.loadCurrentUser();
    }
}
