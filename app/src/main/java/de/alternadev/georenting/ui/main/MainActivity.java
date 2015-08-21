package de.alternadev.georenting.ui.main;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.databinding.ActivityMainBinding;
import rebus.header.view.HeaderView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.util.async.Async;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private ActionBarDrawerToggle mDrawerToggle;
    private HeaderView mHeaderView;
    private DrawerLayout mDrawerLayout;

    private GoogleApiClient mClient;

    @Inject
    Picasso picasso;

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

        mClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mClient.connect();

        mDrawerLayout = b.mainDrawerLayout;
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.main_content_frame, fragment);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    private boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        switch (menuItem.getItemId()) {
            case R.id.nav_my_geofences:
                showFragment(MyGeofencesFragment.newInstance());

                break;
            case R.id.nav_map:
                showFragment(MapFragment.newInstance());

                break;
            case R.id.nav_profile:
                showFragment(ProfileFragment.newInstance());
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Async.start(() -> Plus.PeopleApi.getCurrentPerson(mClient), Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showUserInHeader);
    }

    private void showUserInHeader(Person user) {
        picasso.load(user.getCover().getCoverPhoto().getUrl())
               .into(mHeaderView.background());
        picasso.load(user.getImage().getUrl() + "&sz=250")
               .into(mHeaderView.avatar());
        mHeaderView.username(user.getDisplayName());
        mHeaderView.email(Plus.AccountApi.getAccountName(mClient));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
