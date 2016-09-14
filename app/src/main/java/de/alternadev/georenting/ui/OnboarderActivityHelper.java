package de.alternadev.georenting.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.chyrta.onboarder.OnboarderActivity;
import com.facebook.stetho.inspector.domstorage.SharedPreferencesHelper;

/**
 * Created by jhbru on 14.09.2016.
 */

public class OnboarderActivityHelper {
    public static final int DEFAULT_ONBOARDER_ACTIVITY_REQUEST = 1;

    public static final String KEY_ONBOARDER_ACTIVITY_STARTED = "onboarder_screen_started";
    public static final String PREFS_NAME = "onboarder";

    private final SharedPreferences mPreferences;
    private Activity mActivity;
    private Class<? extends OnboarderActivity> mActivityClass;
    private boolean onboarderActivityStarted = false;

    public OnboarderActivityHelper(Activity activity, Class<? extends OnboarderActivity> activityClass) {
        mActivity = activity;
        mActivityClass = activityClass;
        mPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private boolean getOnboarderActivityStarted(Bundle savedInstanceState) {
        if (!onboarderActivityStarted) {
            onboarderActivityStarted = savedInstanceState != null && savedInstanceState.getBoolean(KEY_ONBOARDER_ACTIVITY_STARTED, false);
        }
        return onboarderActivityStarted;
    }

    private boolean shouldShow(Bundle savedInstanceState) {
        return !getOnboarderActivityStarted(savedInstanceState) &&
                !mPreferences.getBoolean(KEY_ONBOARDER_ACTIVITY_STARTED, false);
    }

    @Deprecated
    public boolean show() {
        return show(DEFAULT_ONBOARDER_ACTIVITY_REQUEST);
    }


    @Deprecated
    public boolean show(int requestCode) {
        return show(null, requestCode);
    }

    public boolean show(Bundle savedInstanceState) {
        return show(savedInstanceState, DEFAULT_ONBOARDER_ACTIVITY_REQUEST);
    }

    public boolean show(Bundle savedInstanceState, int requestCode) {
        boolean shouldShow = shouldShow(savedInstanceState);
        if (shouldShow) {
            onboarderActivityStarted = true;
            startActivity(requestCode);
        }
        return shouldShow;
    }

    public void forceShow() {
        forceShow(DEFAULT_ONBOARDER_ACTIVITY_REQUEST);
    }

    public void forceShow(int requestCode) {
        startActivity(requestCode);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_ONBOARDER_ACTIVITY_STARTED, onboarderActivityStarted);
    }

    private void startActivity(int requestCode) {
        Intent intent = new Intent(mActivity, mActivityClass);
        mActivity.startActivityForResult(intent, requestCode);
    }
}
