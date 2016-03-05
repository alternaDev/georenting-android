package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.model.User;
import de.alternadev.georenting.data.tasks.UpdateGeofencesTask;
import de.alternadev.georenting.databinding.ActivityMainBinding;
import de.alternadev.georenting.ui.settings.SettingsActivity;
import hugo.weaving.DebugLog;
import rebus.header.view.HeaderView;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mDrawerToggle;
    private HeaderView mHeaderView;
    private DrawerLayout mDrawerLayout;

    @Inject
    Picasso picasso;

    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((GeoRentingApplication) getApplication()).getComponent().inject(this);

        ActivityMainBinding b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, b.mainDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();

        mHeaderView = new HeaderView(this);
        b.mainNavigationView.addHeaderView(mHeaderView);
        b.mainNavigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        mCurrentUser = ((GeoRentingApplication) getApplication()).getSessionToken().user;

        if(mCurrentUser == null) {
            finish();
            return;
        }

        showUserInHeader(mCurrentUser);

        mDrawerLayout = b.mainDrawerLayout;

        showFragment(MyGeofencesFragment.newInstance());

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

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.main_content_frame, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @DebugLog
    private boolean onNavigationItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_my_geofences:
                menuItem.setChecked(true);
                showFragment(MyGeofencesFragment.newInstance());

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
            picasso.load(user.coverUrl)
                    .into(mHeaderView.background());
        } else {
            picasso.load(R.drawable.default_background)
                    .fit()
                    .into(mHeaderView.background());
        }
        if(user.avatarUrl != null && !user.avatarUrl.isEmpty()) {
            picasso.load(user.avatarUrl + "&sz=250")
                    .into(mHeaderView.avatar());
        }
        mHeaderView.username(user.name);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
