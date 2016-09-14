package de.alternadev.georenting.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import de.alternadev.georenting.R;

import static de.alternadev.georenting.ui.OnboarderActivityHelper.PREFS_NAME;

/**
 * Created by jhbru on 14.09.2016.
 */

public class IntroActivity extends AppIntro2 {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor();

        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_1_title), getString(R.string.onboarding_1_description), R.drawable.ic_launcher, ActivityCompat.getColor(this, R.color.primary_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_2_title), getString(R.string.onboarding_2_description), R.drawable.ob2, ActivityCompat.getColor(this, R.color.primary_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_3_title), getString(R.string.onboarding_3_description), R.drawable.ob3, ActivityCompat.getColor(this, R.color.primary_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_4_title), getString(R.string.onboarding_4_description), R.drawable.ob4, ActivityCompat.getColor(this, R.color.primary_color)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_5_title), getString(R.string.onboarding_5_description), R.drawable.ob5, ActivityCompat.getColor(this, R.color.primary_color)));

        setButtonState(this.skipButton, false);
        /*setDividerHeight(0);
        setSkipButtonHidden();
        setFinishButtonTitle(R.string.onboarding_finish);

        setOnboardPagesReady(onboarderPages);*/
    }

    protected void setStatusBarColor() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.setStatusBarColor(ActivityCompat.getColor(this, R.color.dark_primary_color));
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(OnboarderActivityHelper.KEY_ONBOARDER_ACTIVITY_STARTED, true).apply();
        finish();
    }

    @Override
    public void onSlideChanged(Fragment a, Fragment b) {
        setButtonState(this.skipButton, false);
    }

}
