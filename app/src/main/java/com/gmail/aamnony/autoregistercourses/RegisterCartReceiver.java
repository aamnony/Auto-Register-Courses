package com.gmail.aamnony.autoregistercourses;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import java.util.Calendar;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class RegisterCartReceiver extends BroadcastReceiver
{
    private static final String ACTION_REGISTER = "com.gmail.aamnony.autoregistercourses.action.REGISTER_RECEIVER";

    private static final int FINAL_HOUR = 22;
    private static final int START_HOUR = 8;

    public RegisterCartReceiver ()
    {
    }

    @Override
    public void onReceive (Context context, Intent intent)
    {
        if (intent != null)
        {
            long sync_frequency = Long.parseLong(getDefaultSharedPreferences(context).getString("sync_frequency", "1800000"));
            switch (intent.getAction())
            {
                case ACTION_REGISTER:
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if (Utils.exceeds(hour, START_HOUR, FINAL_HOUR))
                    {
                        scheduleRegisterCartAlarm(context, START_HOUR, sync_frequency);
                    }
                    else
                    {
                        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("next_check", System.currentTimeMillis() + sync_frequency).apply();
                        CartIntentService.startActionRegister(context);
                    }
                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                    long nextCheck = PreferenceManager.getDefaultSharedPreferences(context).getLong("next_check", -1);
                    if (nextCheck >= 0)
                    {
                        scheduleRegisterCartAlarm(context, nextCheck, sync_frequency);
                    }
                    break;
            }
        }
    }

    /**
     * Set an alarm in {@link AlarmManager} to try and register cart.
     *
     * @param context  context.
     * @param time     in milliseconds.
     * @param interval in milliseconds.
     */
    static void scheduleRegisterCartAlarm (Context context, long time, long interval)
    {
        context = context.getApplicationContext();
        Intent intent = new Intent(context, RegisterCartReceiver.class);
        intent.setAction(ACTION_REGISTER);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (interval <= 0)
        {
            alarmManager.cancel(pendingIntent);
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("next_check", -1).apply();
        }
        else
        {
            alarmManager.setInexactRepeating(AlarmManager.RTC, time, interval, pendingIntent);
            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("next_check", time).apply();
        }
    }
}
