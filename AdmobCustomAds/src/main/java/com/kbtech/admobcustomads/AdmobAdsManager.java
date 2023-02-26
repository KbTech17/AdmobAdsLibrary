package com.kbtech.admobcustomads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class AdmobAdsManager {

    private static InterstitialAd mInterstitialAd;
    private static int LAUNCHED_AD = 0;
    private static RewardedAd mRewardedAd;
    int HANDLER_SUCCESS = 1;


    public static void initializeMobileAds(final Context context) {
        MobileAds.initialize(context, initializationStatus -> {
        });
    }

    //================ BANNER START ==================
    public void showBannerAd(Activity mActivity, String adId, int isEnable, LinearLayout layout) {
        try {
            layout.setVisibility(View.VISIBLE);
            if (!isPremium()) {
                if (isEnable == HANDLER_SUCCESS) {
                    final AdView mAdView = new AdView(mActivity);
                    layout.removeAllViews();
                    EasyAdsDynamic.forBanner(mActivity)
                            .withLayout(layout, mAdView)
                            .listener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    super.onAdFailedToLoad(loadAdError);
                                }

                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                }
                            }).adUnitId(adId)
                            .adSize(getAdSize(mActivity))
                            .show();
                } else {
                    layout.setVisibility(View.GONE);
                }
            } else {
                layout.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    private static AdSize getAdSize(final Activity mActivity) {
        Display display = mActivity.getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);
    }
    //================ BANNER END ==================

    //================ INTERSTITIAL START ================
    public void loadInterstitialAd(final Activity mActivity) {
        InterstitialAd.load(mActivity, getInterstitialId(), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }
        });
    }
    /**
     * ==================================CAUTION!!!==========================================
     * THIS BELOW METHOD IS USED ONLY AT HomeFragment. USING THIS METHOD SHOW INT AD WITH ONLY ONE TIME PERMISSION TO CONNECT VPN.
     * IF PERMISSION ASKED TWICE WILL TRIGGER TWICE AT ProfileManager.java at line 230 (Thread.sleep(100);) THEN SLOWER THE SYSTEM.
     */
    public void showIntAdSingle(final Activity activity, final Handler handler) {
        if (isPremium()) {
            if (getIsIntAdEnable() == HANDLER_SUCCESS) {
                if (getIntAdClick() <= LAUNCHED_AD) {
                    LAUNCHED_AD = 0;
                    if (mInterstitialAd != null) {
                        activity.runOnUiThread(() -> {
                            if (!activity.isFinishing()) {
                                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        askPermissionOrConnect(handler);
                                        loadInterstitialAd(activity);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        super.onAdFailedToShowFullScreenContent(adError);
                                        askPermissionOrConnect(handler);
                                    }
                                });
                                mInterstitialAd.show(activity);
                            }
                        });
                    } else {
                        askPermissionOrConnect(handler);
                    }
                } else {
                    LAUNCHED_AD++;
                    askPermissionOrConnect(handler);
                }
            } else {
                askPermissionOrConnect(handler);
            }
        } else {
            askPermissionOrConnect(handler);
        }
    }

    private static void askPermissionOrConnect(final Handler handler) {
        if (handler != null) {
            final Message message = Message.obtain();
            message.setTarget(handler);
            message.sendToTarget();
        }
    }


    public void showIntSimple(final Activity activity) {
        final Setting setting = MyPref.get().getSettingModel();
        final boolean isPremium = MyPref.get().isPremiumUser();
        if (setting != null && !isPremium) {
            if (setting.interstital_ad == HANDLER_SUCCESS) {
                if (setting.interstital_ad_click <= LAUNCHED_AD) {
                    LAUNCHED_AD = ZERO;
                    if (mInterstitialAd != null) {
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                loadInterstitialAd(activity);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                            }
                        });
                        mInterstitialAd.show(activity);
                    }
                } else {
                    LAUNCHED_AD++;
                }
            }
        }
    }


    public void showIntAds(final Activity mActivity, final Handler handler) {
        final Setting setting = MyPref.get().getSettingModel();
        final boolean isPremium = MyPref.get().isPremiumUser();
        if (setting != null && !isPremium) {
            if (setting.interstital_ad == HANDLER_SUCCESS) {
                if (setting.interstital_ad_click <= LAUNCHED_AD) {
                    LAUNCHED_AD = ZERO;
                    if (mInterstitialAd != null) {
                        mActivity.runOnUiThread(() -> {
                            if (!mActivity.isFinishing()) {
                                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        openNextActivity(handler);
                                        loadInterstitialAd(mActivity);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                        super.onAdFailedToShowFullScreenContent(adError);
                                        openNextActivity(handler);
                                    }
                                });
                                mInterstitialAd.show(mActivity);
                            }
                        });
                    } else {
                        openNextActivity(handler);
                    }
                } else {
                    LAUNCHED_AD++;
                    openNextActivity(handler);
                }
            } else {
                openNextActivity(handler);
            }
        } else {
            openNextActivity(handler);
        }
    }

    private static void openNextActivity(final Handler handler) {
        if (handler != null) {
            final Message message = Message.obtain();
            message.setTarget(handler);
            message.sendToTarget();
        }
    }
    //================ INTERSTITIAL END ================

    public static void loadRewardAd(final Activity activity) {
        initializeMobileAds(activity);
        final Setting setting = MyPref.get().getSettingModel();
        final boolean isPremium = MyPref.get().isPremiumUser();
        if (setting != null && !isPremium) {
            RewardedAd.load(activity, setting.reward_ad_id,
                    new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            loadRewardAd(activity);
                            mRewardedAd = null;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            mRewardedAd = rewardedAd;
                            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent();
                                    loadRewardAd(activity);
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                    super.onAdFailedToShowFullScreenContent(adError);
                                    mRewardedAd = null;
                                }
                            });
                        }
                    });
        }
    }

    public static void showRewardedAd(Activity activity, final Handler handler) {
        final boolean isPremium = MyPref.get().isPremiumUser();
        if (!isPremium) {
            if (mRewardedAd != null) {
                mRewardedAd.show(activity, rewardItem -> {
                    Toasty.info(activity, activity.getString(R.string.close_ad), Toasty.LENGTH_LONG).show();
                    openNextActivity(handler);
                });
            } else {
                loadInterstitialAd(activity);
                if (mInterstitialAd != null) {
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            loadInterstitialAd(activity);
                            openNextActivity(handler);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            Toasty.success(activity, R.string.congra_one_time, Toasty.LENGTH_SHORT).show();
                            openNextActivity(handler);
                        }
                    });
                    mInterstitialAd.show(activity);
                } else {
                    Toasty.success(activity, R.string.congra_one_time, Toasty.LENGTH_SHORT).show();
                    openNextActivity(handler);
                }
            }
        } else {
            openNextActivity(handler);
        }
    }

    //================ NATIVE START ====================
    public static void initNativeAds(final Activity mActivity, final TemplateView template, nativeAdsFailed failed) {
        final Setting setting = MyPref.get().getSettingModel();
        final boolean isPremium = MyPref.get().isPremiumUser();
        if (setting != null && !isPremium) {
            if (setting.native_ad == HANDLER_SUCCESS) {
                initializeMobileAds(mActivity);
                AdLoader adLoader = new AdLoader.Builder(mActivity, setting.native_ad_id)
                        .forNativeAd(template::setNativeAd).withAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                failed.onNativeFailed(true);
                            }

                            @Override
                            public void onAdOpened() {
                                super.onAdOpened();
                            }

                            @Override
                            public void onAdLoaded() {
                                failed.onNativeFailed(false);
                                template.setVisibility(View.VISIBLE);
                                super.onAdLoaded();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                            }
                        }).build();

                adLoader.loadAd(new AdRequest.Builder().build());
            }
        }
    }

    public interface nativeAdsFailed{
        void onNativeFailed(boolean isFail);
    }
    //================ NATIVE END ====================

}
