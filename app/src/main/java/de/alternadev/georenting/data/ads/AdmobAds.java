package de.alternadev.georenting.data.ads;


import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.Date;

import de.alternadev.georenting.BuildConfig;
import de.alternadev.georenting.R;
import hugo.weaving.DebugLog;

public class AdmobAds {
    private static final String[] TEST_DEVICES = new String[]{
            "25C8329ACC0278B771784BE6A25C1829" // JHB
    };
    private static final long INTERSTITIAL_INTERVAL_MILLISECONDS = 60 * 1000; // Show interstitial once very 60 seconds.

    private InterstitialAd mInterstitialAd;
    private Date mLastInterstitialShown;

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

    @DebugLog
    public void loadIntersitial(Context context) {
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getString(R.string.interstitial_ad_unit_id));

        mInterstitialAd.loadAd(getAdRequest());
    }

    @DebugLog
    public void showInterstitialOrContinue(Context ctx, OnInterstitialFinishedListener l) {
        if(!shouldLoadAd()) {
            l.onFinished();
            return;
        }
        if(mInterstitialAd == null) {
            loadIntersitial(ctx);
            l.onFinished();
            return;
        }
        if(mLastInterstitialShown == null || (new Date().getTime() - mLastInterstitialShown.getTime()) > INTERSTITIAL_INTERVAL_MILLISECONDS) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        loadIntersitial(ctx);
                        l.onFinished();
                    }
                });
                mInterstitialAd.show();
                mLastInterstitialShown = new Date();
                return;
            }
        }
        l.onFinished();
    }

    private boolean shouldLoadAd() {
        return !BuildConfig.DEBUG && BuildConfig.FLAVOR.equals("production"); //TODO: Check for AntiAdInAppPurchase Here.
    }

    public void initialize(Context context) {
        MobileAds.initialize(context, context.getString(R.string.admob_app_id));
    }

    public interface OnInterstitialFinishedListener {
        void onFinished();
    }
}
