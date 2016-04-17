package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.security.Key;
import java.util.Date;

import javax.inject.Inject;

import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.GeoRentingService;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.databinding.ActivityMainBinding;
import de.alternadev.georenting.ui.BaseActivity;
import de.alternadev.georenting.ui.SignInActivity;
import de.alternadev.georenting.ui.settings.SettingsActivity;
import hugo.weaving.DebugLog;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import rebus.header.view.HeaderView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private HeaderView mHeaderView;
    private DrawerLayout mDrawerLayout;


    @Inject
    GeoRentingService mService;

    @Inject
    SharedPreferences mPreferences;

    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getGeoRentingApplication().getComponent().inject(this);

        ActivityMainBinding b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, b.mainDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();

        mHeaderView = new HeaderView(this, false);
        b.mainNavigationView.addHeaderView(mHeaderView);
        b.mainNavigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        SessionToken savedToken = getSavedToken();

        if(savedToken != null) {
            getGeoRentingApplication().setSessionToken(savedToken);
        } else {
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

    private SessionToken getSavedToken() {
        String token = mPreferences.getString(SignInActivity.PREF_TOKEN, "");
        if(token.equals("")) return null;

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
                    SessionToken t = new SessionToken();
                    t.token = token;
                    Timber.i("Using token %s", token);
                    return t;
                }
            }
            Timber.e(e, "Could not parse JWT.");
        }

        return null;
    }

    private void loadCurrentUser() {
        mCurrentUser = getGeoRentingApplication().getSessionToken().user;

        if(mCurrentUser == null) {
            Timber.i("Refreshing Token.");
            mService.refreshToken(getGeoRentingApplication().getSessionToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((sessionToken) -> {
                        getGeoRentingApplication().setSessionToken(sessionToken);
                        mCurrentUser = sessionToken.user;
                        showUserInHeader(mCurrentUser);
                        showFragment(MyGeofencesFragment.newInstance(mCurrentUser));
                    }, error -> {
                        Timber.e(error, "Could not refresh Token.");
                        reSignIn();
                    });

        } else {
            showUserInHeader(mCurrentUser);
            showFragment(MyGeofencesFragment.newInstance(mCurrentUser));
        }
    }

    private void reSignIn() {
        mPreferences.edit().remove(SignInActivity.PREF_TOKEN).commit();
        getGeoRentingApplication().setSessionToken(null);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
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
            case R.id.nav_settings:
                menuItem.setChecked(false);
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }

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
}
