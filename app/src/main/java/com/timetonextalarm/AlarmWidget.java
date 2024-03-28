package com.timetonextalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import java.util.Locale;

public class AlarmWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = alarmManager.getNextAlarmClock();

        long alarmTime = alarmClockInfo != null ? alarmClockInfo.getTriggerTime() : 0;
        String remainingTime = calculateTimeUntilAlarm(alarmTime);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, remainingTime);
        }
    }

    private String calculateTimeUntilAlarm(long alarmTime) {
        if (alarmTime <= 0) {
            return "No alarm set";
        }
        long currentTime = System.currentTimeMillis();
        long diff = alarmTime - currentTime;

        if (diff <= 0) {
            return "No upcoming alarm";
        }

        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff / (1000 * 60)) % 60;

        return String.format(Locale.US, "%d hrs, %02d mins until alarm", hours, minutes);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String remainingTime) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_alarm);

        // Set the text
        views.setTextViewText(R.id.appwidget_text, remainingTime);

        // Dynamically set background and text color if above Android 12
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            int backgroundColor = getSystemAccentColor(context); // Apply background color dynamically
            int textColor = getContrastColor(backgroundColor); // Decide text color based on background

            views.setInt(R.id.widget_background, "setBackgroundColor", backgroundColor);
            views.setTextColor(R.id.appwidget_text, textColor);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static int getSystemAccentColor(Context context) {
        // Correctly obtaining a TypedArray to fetch the color
        TypedArray array = context.obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
        try {
            return array.getColor(0, 0);
        } finally {
            // It's important to recycle your TypedArray afterwards
            array.recycle();
        }
    }

    /**
     * Calculate the luminance of a color to determine how light or dark the color is.
     *
     * @param color The color to calculate the luminance for.
     * @return The luminance of the color.
     */
    public static double calculateLuminance(int color) {
        double red = Color.red(color) / 255.0;
        double green = Color.green(color) / 255.0;
        double blue = Color.blue(color) / 255.0;

        // Adjust color components
        red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);

        // Calculate luminance
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }

    /**
     * Dynamically decides whether the text color should be light or dark, based on the luminance of the background color.
     *
     * @param backgroundColor The background color.
     * @return The color integer of the text color.
     */
    public static int getContrastColor(int backgroundColor) {
        // Assuming a light color by default
        int darkColor = Color.BLACK; // Adjust as needed
        int lightColor = Color.WHITE; // Adjust as needed

        // Calculate the luminance of the background color
        double luminance = calculateLuminance(backgroundColor);

        // Arbitrarily chosen luminance threshold for deciding between light and dark text color
        return luminance < 0.5 ? lightColor : darkColor;
    }

}
