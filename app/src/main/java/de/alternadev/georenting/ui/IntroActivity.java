package de.alternadev.georenting.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.alternadev.georenting.R;

import static de.alternadev.georenting.ui.OnboarderActivityHelper.PREFS_NAME;

/**
 * Created by jhbru on 14.09.2016.
 */

public class IntroActivity extends OnboarderActivity {

    List<OnboarderPage> onboarderPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onboarderPages = new ArrayList<OnboarderPage>();

        OnboarderPage onboarderPage1 = new OnboarderPage(R.string.onboarding_1_title, R.string.onboarding_1_description, R.drawable.ic_launcher);
        OnboarderPage onboarderPage2 = new OnboarderPage(R.string.onboarding_2_title, R.string.onboarding_2_description, R.drawable.ob2);
        OnboarderPage onboarderPage3 = new OnboarderPage(R.string.onboarding_3_title, R.string.onboarding_3_description, R.drawable.ob3);
        OnboarderPage onboarderPage4 = new OnboarderPage(R.string.onboarding_4_title, R.string.onboarding_4_description, R.drawable.ob4);
        OnboarderPage onboarderPage5 = new OnboarderPage(R.string.onboarding_5_title, R.string.onboarding_5_description, R.drawable.ob5);

        onboarderPages.add(onboarderPage1);
        onboarderPages.add(onboarderPage2);
        onboarderPages.add(onboarderPage3);
        onboarderPages.add(onboarderPage4);
        onboarderPages.add(onboarderPage5);

        for(OnboarderPage p : onboarderPages) {
            p.setBackgroundColor(R.color.primary_color);
            p.setDescriptionTextSize(18);
            p.setDescriptionColor(R.color.white);
        }

        setDividerHeight(0);
        setSkipButtonHidden();
        setFinishButtonTitle(R.string.onboarding_finish);

        setOnboardPagesReady(onboarderPages);
    }

    @Override
    public void onFinishButtonPressed() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(OnboarderActivityHelper.KEY_ONBOARDER_ACTIVITY_STARTED, true).apply();
        finish();
    }
}
