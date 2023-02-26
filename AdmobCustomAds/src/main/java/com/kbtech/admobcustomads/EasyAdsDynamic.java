package com.kbtech.admobcustomads;

import android.content.Context;
import android.os.Handler;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class EasyAdsDynamic {
    private static final Handler delayHandler = new Handler();

    public static Banner forBanner(Context context) {
        return new Banner(context);
    }

    public static class Banner {

        private long delayTime = 0;
        final Context context;
        LinearLayout adLayout = null;
        AdView adViewBanner;
        final AdRequest.Builder adRequestBuilder;

        private Banner(final Context context) {
            this.context = context;
            adRequestBuilder = new AdRequest.Builder();
        }

        public Banner withLayout(LinearLayout adLayoutBanner, AdView adViewBanner) {
            this.adLayout = adLayoutBanner;
            this.adViewBanner = adViewBanner;
            return this;
        }

        public Banner with(AdView adViewBanner) {
            this.adViewBanner = adViewBanner;
            return this;
        }

        public Banner adUnitId(String adsUnitId) {
            this.adViewBanner.setAdUnitId(adsUnitId);
            return this;
        }

        public Banner adSize(AdSize adSize) {
            this.adViewBanner.setAdSize(adSize);
            return this;
        }

        public Banner delay(int timeInMillis) {
            this.delayTime = timeInMillis;
            return this;
        }

        public Banner listener(AdListener adListener) {
            adViewBanner.setAdListener(adListener);
            return this;
        }

        public void show() {
            Runnable delayRunnable = () -> {
                try {
                    adLayout.addView(adViewBanner);
                } catch (Exception ignored) {
                }
                try {
                    adViewBanner.loadAd(adRequestBuilder.build());
                } catch (Exception ignored) {
                }
            };
            delayHandler.postDelayed(delayRunnable, delayTime);
        }

    }
}
