package com.bumbumapps.screenrecorder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.NonPersonalizedAd;
import com.huawei.hms.ads.RequestOptions;
import com.huawei.hms.ads.banner.BannerView;

/**
 * Huawei AppGallery build: same API and interstitial cap as the Play build, but served by Huawei Ads
 * (Petal) instead of AdMob. Only non-personalized ads are requested, so no consent form is needed.
 * The AdMob unit IDs passed by callers are ignored; the hms slot IDs come from resources.
 */
public final class AdsHelper {

    public static final long INTERSTITIAL_MIN_INTERVAL_MS = 10 * 60_000L;
    public static final long INTERSTITIAL_FIRST_GRACE_MS = 3 * 60_000L;
    private static final String TAG = "AdsHelper";
    private static final String CAP_PREFS = "interstitial_cap";
    private static final String KEY_LAST = "last_shown_ms";
    private static final String KEY_FIRST_SEEN = "first_seen_ms";

    private static volatile boolean sdkReady;
    private static InterstitialAd interstitialAd;

    public interface OnAdsReady {
        void onAdsReady();
    }

    private AdsHelper() {
    }

    /** Initialises Huawei Ads (non-personalized) and calls {@code onReady} once. */
    public static void gatherConsent(@NonNull Activity activity, @NonNull OnAdsReady onReady) {
        firstSeen(capPrefs(activity), System.currentTimeMillis());
        if (activity.isFinishing()) {
            return;
        }
        if (!sdkReady) {
            try {
                HwAds.init(activity.getApplicationContext());
                RequestOptions options = HwAds.getRequestOptions().toBuilder()
                        .setNonPersonalizedAd(NonPersonalizedAd.ALLOW_NON_PERSONALIZED)
                        .build();
                HwAds.setRequestOptions(options);
                sdkReady = true;
            } catch (RuntimeException e) {
                Log.w(TAG, "Huawei Ads init failed", e);
                return;
            }
        }
        onReady.onAdsReady();
    }

    public static boolean canRequestAds(@NonNull Context context) {
        return sdkReady;
    }

    public static boolean isPrivacyOptionsRequired(@NonNull Context context) {
        return false;
    }

    public static void showPrivacyOptions(@NonNull Activity activity) {
    }

    private static String slot(Context context, int real, int test) {
        return context.getString(BuildConfig.DEBUG ? test : real);
    }

    /** Adds a Huawei banner to {@code container}; returns null if ads are not ready. */
    @Nullable
    public static BannerView loadBanner(@NonNull Activity activity, @NonNull ViewGroup container, @NonNull String adUnitId) {
        if (!canRequestAds(activity)) {
            return null;
        }
        BannerView banner = new BannerView(activity);
        banner.setAdId(slot(activity, R.string.hms_banner_id, R.string.hms_test_banner_id));
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        banner.setBannerAdSize(BannerAdSize.getCurrentDirectionBannerSize(activity, widthDp));
        container.removeAllViews();
        container.addView(banner);
        banner.loadAd(new AdParam.Builder().build());
        return banner;
    }

    /** Loads one interstitial in the background if none is cached or loading. */
    public static void preloadInterstitial(@NonNull Context context, @NonNull String adUnitId) {
        if (interstitialAd != null || !canRequestAds(context)) {
            return;
        }
        Context app = context.getApplicationContext();
        InterstitialAd ad = new InterstitialAd(app);
        ad.setAdId(slot(app, R.string.hms_interstitial_id, R.string.hms_test_interstitial_id));
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdFailed(int code) {
                if (interstitialAd == ad) {
                    interstitialAd = null;
                }
            }
        });
        interstitialAd = ad;
        ad.loadAd(new AdParam.Builder().build());
    }

    public static boolean showInterstitialIfReady(@NonNull Activity activity, @NonNull String adUnitId) {
        return showInterstitialIfReady(activity, adUnitId, null);
    }

    public static boolean showInterstitialIfReady(@NonNull Activity activity, @NonNull String adUnitId,
                                                  @Nullable Runnable afterAd) {
        boolean shown = showInterstitialInternal(activity, adUnitId, afterAd);
        if (!shown && afterAd != null) {
            afterAd.run();
        }
        return shown;
    }

    private static boolean showInterstitialInternal(Activity activity, String adUnitId, @Nullable Runnable afterAd) {
        long now = System.currentTimeMillis();
        InterstitialAd ad = interstitialAd;
        if (ad == null) {
            preloadInterstitial(activity, adUnitId);
            return false;
        }
        if (!ad.isLoaded()) {
            return false;
        }
        SharedPreferences prefs = capPrefs(activity);
        if (!isIntervalElapsed(firstSeen(prefs, now), now, INTERSTITIAL_FIRST_GRACE_MS)
                || !isIntervalElapsed(prefs.getLong(KEY_LAST, -1L), now, INTERSTITIAL_MIN_INTERVAL_MS)
                || activity.isFinishing()) {
            return false;
        }
        final Context app = activity.getApplicationContext();
        interstitialAd = null;
        // Huawei only reports load failures via onAdFailed: if show() never opens the ad, do not wait forever.
        final boolean[] opened = {false};
        final boolean[] done = {false};
        final Runnable finish = () -> {
            if (done[0]) {
                return;
            }
            done[0] = true;
            preloadInterstitial(app, adUnitId);
            if (afterAd != null) {
                afterAd.run();
            }
        };
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdOpened() {
                opened[0] = true;
            }

            @Override
            public void onAdClosed() {
                finish.run();
            }

            @Override
            public void onAdFailed(int code) {
                finish.run();
            }
        });
        prefs.edit().putLong(KEY_LAST, now).apply();
        ad.show(activity);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!opened[0]) {
                finish.run();
            }
        }, 3000L);
        return true;
    }

    private static SharedPreferences capPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(CAP_PREFS, Context.MODE_PRIVATE);
    }

    private static long firstSeen(SharedPreferences prefs, long now) {
        long first = prefs.getLong(KEY_FIRST_SEEN, 0L);
        if (first == 0L) {
            first = now;
            prefs.edit().putLong(KEY_FIRST_SEEN, first).apply();
        }
        return first;
    }

    /** Frequency cap: true when at least {@code minIntervalMs} passed since {@code lastAt} (negative = never). */
    static boolean isIntervalElapsed(long lastAt, long now, long minIntervalMs) {
        return lastAt < 0 || now - lastAt >= minIntervalMs;
    }
}
