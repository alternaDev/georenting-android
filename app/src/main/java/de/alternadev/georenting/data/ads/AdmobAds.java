package de.alternadev.georenting.data.ads;


import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import de.alternadev.georenting.BuildConfig;

public class AdmobAds {
    private static final String[] TEST_DEVICES = new String[]{
            "25C8329ACC0278B771784BE6A25C1829" // JHB
    };
    private AdRequest getAdRequest() {
         AdRequest.Builder b = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR);

        for(String device : TEST_DEVICES) {
            b.addTestDevice(device);
        }

        return b.build();
    }

    public void loadBannerAdIntoAdView(AdView adView) {
        adView.setVisibility(View.GONE);
        if(!shouldLoadAd()) return;

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });
        adView.loadAd(getAdRequest());
    }

    private boolean shouldLoadAd() {
        return !BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("production"); //TODO: Check for AntiAdInAppPurchase Here.
    }
}
