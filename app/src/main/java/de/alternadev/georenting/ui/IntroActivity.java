package de.alternadev.georenting.ui;

import android.content.Context;
import android.os.Bundle;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

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

        // Create your first page
        OnboarderPage onboarderPage1 = new OnboarderPage(R.string.onboarding_1_title, R.string.onboarding_1_description, R.drawable.ic_launcher);
        OnboarderPage onboarderPage2 = new OnboarderPage(R.string.content_description_empty, R.string.onboarding_2_description);
        OnboarderPage onboarderPage3 = new OnboarderPage(R.string.content_description_empty, R.string.onboarding_3_description);
        OnboarderPage onboarderPage4 = new OnboarderPage(R.string.content_description_empty, R.string.onboarding_4_description);
        OnboarderPage onboarderPage5 = new OnboarderPage(R.string.content_description_empty, R.string.onboarding_5_description);

        // You can define title and description colors (by default white)
        onboarderPage1.setTitleColor(R.color.white);
        onboarderPage1.setDescriptionColor(R.color.white);

        // Add your pages to the list
        onboarderPages.add(onboarderPage1);
        onboarderPages.add(onboarderPage2);
        onboarderPages.add(onboarderPage3);
        onboarderPages.add(onboarderPage4);
        onboarderPages.add(onboarderPage5);

        for(OnboarderPage p : onboarderPages) {
            p.setBackgroundColor(R.color.dark_primary_color);
            p.setDescriptionTextSize(16);
            p.setDescriptionColor(R.color.white);
        }

        setDividerHeight(0);
        setSkipButtonHidden();

        // And pass your pages to 'setOnboardPagesReady' method
        setOnboardPagesReady(onboarderPages);

    }

    @Override
    public void onFinishButtonPressed() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(OnboarderActivityHelper.KEY_ONBOARDER_ACTIVITY_STARTED, true).apply();
        finish();
    }
}
