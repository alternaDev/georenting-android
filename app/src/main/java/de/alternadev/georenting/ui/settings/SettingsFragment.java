package de.alternadev.georenting.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import de.alternadev.georenting.R;

/**
 * Created by jhbruhn on 25.08.15 for georenting-android.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
