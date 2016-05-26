package de.alternadev.georenting.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;
import de.alternadev.georenting.data.api.model.GeoFence;
import de.alternadev.georenting.data.api.model.User;

/**
 * Created by jhbruhn on 14.04.16.
 */
public class BaseActivity extends AppCompatActivity {
    public static final String ANALYTICS_PROPERTY_USER_NAME = "name";
    private static final String ANALYTICS_PROPERTY_USER_ID = "id";

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarColor();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementsUseOverlay(false);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public GeoRentingApplication getGeoRentingApplication() {
        return ((GeoRentingApplication) getApplication());
    }

    protected void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(Build.VERSION.SDK_INT >= 21)
            window.setStatusBarColor(getResources().getColor(R.color.dark_primary_color));
    }

    protected void logUserAnalytics(User user) {
        mFirebaseAnalytics.setUserId(user.id + "");
        mFirebaseAnalytics.setUserProperty(ANALYTICS_PROPERTY_USER_NAME, user.name);
    }

    void logBuyGeoFence(GeoFence fence, double price) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, fence.id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "geo_fence");
        bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, "geo_coin");
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, price);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SPEND_VIRTUAL_CURRENCY, bundle);
    }

    void logSignIn(User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putString(ANALYTICS_PROPERTY_USER_NAME, currentUser.name);
        bundle.putInt(ANALYTICS_PROPERTY_USER_ID, currentUser.id);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    void logSelectGeoFence(GeoFence fence) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "geo_fence");
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, fence.id);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    protected void logSentInvite() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "app");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
    }
}
