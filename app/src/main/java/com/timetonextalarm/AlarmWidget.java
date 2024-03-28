package com.timetonextalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
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
        views.setTextViewText(R.id.appwidget_text, remainingTime);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
