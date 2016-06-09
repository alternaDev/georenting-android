package de.alternadev.georenting;


import android.os.Build;
import android.os.RemoteException;
import android.support.multidex.MultiDex;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.test.ActivityInstrumentationTestCase2;

import de.alternadev.georenting.data.api.TestApiModule;
import de.alternadev.georenting.data.api.model.SessionToken;
import de.alternadev.georenting.data.auth.TestAuthModule;
import de.alternadev.georenting.ui.main.MainActivity;
import timber.log.Timber;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleUtil;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private SessionToken mToken = new SessionToken();
    private UiDevice mDevice;
    private SystemAnimations systemAnimations;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void setUp() {
        MultiDex.install(getTargetContext());
        systemAnimations = new SystemAnimations(getInstrumentation().getContext());
        systemAnimations.disableAll();

        mDevice = UiDevice.getInstance(getInstrumentation());


        try {
            mDevice.wakeUp();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mToken.token = "token";

        GeoRentingApplication app = ((GeoRentingApplication) getInstrumentation().getTargetContext().getApplicationContext());
        app.setSessionToken(mToken);
        app.setComponent(DaggerGeoRentingComponent.builder()
               .apiModule(new TestApiModule())
               .authModule(new TestAuthModule())
               .geoRentingModule(new GeoRentingModule(app))
               .build());
        try {
            mDevice.wakeUp();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        getActivity();
        try {
            mDevice.wakeUp();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        LocaleUtil.changeDeviceLocaleTo(LocaleUtil.getTestLocale());
    }

    private void allowLocationServicePermissionsIfNeeded() {
        UiObject allowPermissions = mDevice.findObject(new UiSelector().text(getActivity().getString(R.string.yes)));
        if (allowPermissions.exists()) {
            try {
                allowPermissions.click();
            } catch (UiObjectNotFoundException e) {
                Timber.e(e, "There is no Location Service dialog to interact with ");
            }
        }
    }

    public void tearDown() {
        LocaleUtil.changeDeviceLocaleTo(LocaleUtil.getEndingLocale());
        systemAnimations.enableAll();
    }

    public void testTakeScreenshot() {
        allowLocationServicePermissionsIfNeeded();
        allowLocationServicePermissionsIfNeeded();
        Screengrab.screenshot("main_activity");

        onView(withId(R.id.main_drawer_layout)).perform(DrawerActions.open());

        Screengrab.screenshot("drawer_main_activity");

        onView(withId(R.id.main_navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_map));

        Screengrab.screenshot("map_main_activity");

    }
}
