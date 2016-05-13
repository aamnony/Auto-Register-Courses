package com.gmail.aamnony.autoregistercourses;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;
import static java.text.DateFormat.DEFAULT;
import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getDateTimeInstance;

class Log
{
    static void append (Context context, int n)
    {
        String msg = String.format("%s - %d courses.\n", getDateTimeInstance(SHORT, DEFAULT).format(new Date()), n);
        write(context, msg, MODE_APPEND);
    }

    static void clear (Context context)
    {
        write(context, "", MODE_PRIVATE);
    }

    static String read (Context context)
    {
        try (FileInputStream fis = context.openFileInput("log.txt"))
        {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int n;

            while ((n = fis.read(buffer)) > 0)
            {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray());
        }
        catch (java.io.IOException e)
        {
            return e.getMessage();
        }
    }

    private static void write (Context context, String msg, int mode)
    {
        try (FileOutputStream fos = context.openFileOutput("log.txt", mode))
        {
            fos.write(msg.getBytes());
        }
        catch (java.io.IOException e)
        {
            android.util.Log.d("Log", e.getMessage());
        }
    }
}
