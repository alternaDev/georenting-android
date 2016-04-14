package de.alternadev.georenting.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import de.alternadev.georenting.GeoRentingApplication;
import de.alternadev.georenting.R;

/**
 * Created by jhbruhn on 14.04.16.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(Build.VERSION.SDK_INT >= 21)
            window.setStatusBarColor(getResources().getColor(R.color.dark_primary_color));
    }

    public GeoRentingApplication getGeoRentingApplication() {
        return ((GeoRentingApplication) getApplication());
    }
}
