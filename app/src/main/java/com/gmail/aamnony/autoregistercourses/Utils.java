package com.gmail.aamnony.autoregistercourses;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class Utils
{
    /**
     * @return {@link ArrayList} of elements missing from B but are on A.
     */
    public static <T> ArrayList<T> compare (T[] A, T[] B)
    {
        ArrayList<T> diff = new ArrayList<>();
        if (B==null)
        {
            return diff;
        }
        List<T> Blist = Arrays.asList(B);
        for (T a : A)
        {
            if (!Blist.contains(a))
            {
                diff.add(a);
            }
        }
        return diff;
    }

    /**
     * Returns a {@link String} containing the given subsequence of this String.
     *
     * @param s  a given {@link String} to subsequence.
     * @param cl the left character to start subsequencing from.
     * @param cr the right character to end subsequencing.
     * @return the subsequence
     */
    public static String substring (String s, char cl, char cr)
    {
        if (TextUtils.isEmpty(s))
        {
            return "";
        }
        int left = s.indexOf(cl, 0) + 1;
        int right = s.indexOf(cr, left);
        return s.substring(left, right);
    }

    /**
     * Split a given {@link String} by line breaks.
     *
     * @param s a given {@link String} to split.
     * @return a {@link String} array containing the lines of the given {@link String}.
     */
    public static String[] toLines (String s)
    {
        return s.split("\\r?\\n");
    }

    /**
     * Create a {@link String} of '*' from a given string.
     *
     * @param string - a given {@link String}.
     * @return the encoded {@link String}.
     */
    public static String passwordString (String string)
    {
        char[] chars = new char[string.length()];
        Arrays.fill(chars, '*');
        return new String(chars);
    }

    /**
     * Calculate the given milliseconds value in minutes (rounded down).
     *
     * @param millis time in milliseconds.
     * @return minutes in milliseconds.
     */
    public static long minutes (long millis)
    {
        return millis / 60000;
    }

    /**
     * @return true/false - whether num is outside the interval (lowerLimit, upperLimit).
     */
    public static boolean exceeds (int num, int lowerLimit, int upperLimit)
    {
        return num > upperLimit || num < lowerLimit;
    }

    public static void postNotification (String msg, int id, Context context)
    {
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_reem_notif)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.app_name))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentText(msg);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, builder.build());
    }

    public static void postNotification (List<Course> courses, Context context)
    {
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_reem_notif)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.app_name))
                .setDefaults(Notification.DEFAULT_SOUND);

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();

        inboxStyle.setBigContentTitle("נרשמת לקורסים הבאים:").setSummaryText("נרשמת לקורסים חדשים");

        for (Course c : courses)
        {
            inboxStyle.addLine(c.fullInfo());
        }

        builder.setStyle(inboxStyle);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0xA5AF, builder.build());
    }

    public static class WaitingList
    {
        private static final String WAITING_LIST = "waiting list";

        public static Course[] get (Context context)
        {
            return new Gson().fromJson(context.getSharedPreferences(WAITING_LIST, Context.MODE_PRIVATE).getString(WAITING_LIST, ""),
                    Course[].class);
        }

        public static void remove (Context context, Course course)
        {
            ArrayList<Course> current = new ArrayList<>(Arrays.asList(get(context)));
            current.remove(course);
            set(context, current.toArray(new Course[current.size()]));
        }

        public static void add (Context context, Course course)
        {
            ArrayList<Course> current;
            Course[] currentArr = get(context);
            if (currentArr == null)
            {
                current = new ArrayList<>();
            }
            else
            {
                current = new ArrayList<>(Arrays.asList(currentArr));
            }

            current.add(course);
            set(context, current.toArray(new Course[current.size()]));
        }

        @SuppressLint("CommitPrefEdits")
        public static void set (Context context, Course[] courses)
        {
            context.getSharedPreferences(WAITING_LIST, Context.MODE_PRIVATE)
                    .edit()
                    .putString(WAITING_LIST, new Gson().toJson(courses, Course[].class))
                    .commit();
        }
    }

    public static class CurrentRegisteredCourses
    {
        private static final String CURRENT_REGISTERED = "current registered";

        public static Course[] get (Context context)
        {
            return new Gson().fromJson(context.getSharedPreferences(CURRENT_REGISTERED, Context.MODE_PRIVATE).getString(CURRENT_REGISTERED, ""),
                    Course[].class);
        }

        @SuppressLint("CommitPrefEdits")
        public static void set (Context context, Course[] courses)
        {
            context.getSharedPreferences(CURRENT_REGISTERED, Context.MODE_PRIVATE)
                    .edit()
                    .putString(CURRENT_REGISTERED, new Gson().toJson(courses, Course[].class))
                    .commit();
        }
    }
}
