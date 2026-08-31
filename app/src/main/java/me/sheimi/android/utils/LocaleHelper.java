package me.sheimi.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * Applies the user-selected app locale to contexts and resources.
 */
public final class LocaleHelper {

    private LocaleHelper() {
    }

    /**
     * Returns a Context whose resources use the user-selected app locale.
     * Call from {@code attachBaseContext}.
     */
    public static Context attachBaseContext(Context context) {
        Locale locale = Profile.getAppLocale(context);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        return context.createConfigurationContext(config);
    }

    /**
     * Updates the resources of the given context in place to the
     * user-selected app locale.
     */
    public static void updateResources(Context context) {
        Locale locale = Profile.getAppLocale(context);
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
