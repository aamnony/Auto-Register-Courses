package com.gmail.aamnony.autoregistercourses;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;


// http://stackoverflow.com/questions/25461792/persistent-cookie-store-using-okhttp-2-on-android
class Cart
{
    private static final String REGISTER_URL = "https://ug3.technion.ac.il/rishum/register/";
    private static final String REGISTER_CONFIRM_URL = REGISTER_URL + "confirm";
    private static final String CART_URL = "https://ug3.technion.ac.il/rishum/cart";
    private static final String LOGIN_URL = "https://ug3.technion.ac.il/rishum/login";
    private final Context mContext;

    private final OkHttpClient mClient;

    public Cart (Context appContext)
    {
        mClient = new OkHttpClient();
        mClient.setCookieHandler(new CookieManager(new PersistentCookieStore(appContext), CookiePolicy.ACCEPT_ALL));
        mContext = appContext;
    }

    /**
     * Add course to cart.
     *
     * @param course {@link Course} to add.
     * @return true/false whether operation was successful.
     */
    public boolean add (Course course)
    {
        if (!login())
        {
            return false;
        }
        RequestBody requestBody = new FormEncodingBuilder()
                .add("LMK1", course.number)
                .add("LGRP1", course.group)
                .add("addToCart", "Y")
                .build();

        Request request = new Request.Builder()
                .url(CART_URL)
                .post(requestBody)
                .build();
        try
        {
            Response response = mClient.newCall(request).execute();
            return !response.body().string().contains("error");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all courses in cart.
     *
     * @return all courses in cart.
     */
    public Course[] getAll ()
    {
        if (!login())
        {
            return new Course[0];
        }

        Request request = new Request.Builder()
                .url(CART_URL)
                .build();
        try
        {
            Response response = mClient.newCall(request).execute();
            return parse(response.body().string(), false);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return new Course[0];
    }

    /**
     * Register cart.
     *
     * @return array of courses which were successfully registered.
     */
    public Course[] register ()
    {
        Request request = new Request.Builder()
                .url(REGISTER_CONFIRM_URL)
                .build();
        try
        {
            Response response = mClient.newCall(request).execute();
            return parse(response.body().string(), true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return new Course[0];
    }

    /**
     * Remove a given course from cart.
     *
     * @param course {@link Course} to remove from cart.
     * @return true/false whether operation was successful.
     */
    boolean remove (Course course)
    {
        if (!login())
        {
            return false;
        }
        Request request = new Request.Builder()
                .url(CART_URL + "/remove/" + course.toString())
                .build();

        try
        {
            Response response = mClient.newCall(request).execute();
            return !response.body().string().contains("error");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Parse a cart webpage.
     *
     * @param cartPage a given cart webpage.
     * @return {@link Course} array of the current courses in the cart.
     */
    private static Course[] parse (String cartPage, boolean checkMyCoursesOnly)
    {
        if (TextUtils.isEmpty(cartPage))
        {
            return new Course[0];
        }
        String lines[] = Utils.toLines(cartPage);
        ArrayList<Course> courses = new ArrayList<>();

        int i = 0;
        while (i < lines.length)
        {
            if (checkMyCoursesOnly && lines[i].contains("fullCart"))
            {
                break;
            }
            if (lines[i].contains("course-number"))
            {
                i++;
                String number = Utils.substring(lines[i], '>', '<');
                i += 3;
                String name = lines[i].trim();
                i += 6;
                String group = lines[i].replace("קבוצה", "").trim();
                courses.add(new Course(number, group, name));
            }
            i++;
        }
        return courses.toArray(new Course[courses.size()]);
    }

    /**
     * Login to register site using.
     *
     * @return true/false whether operation was successful.
     * @see #LOGIN_URL
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean login ()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        RequestBody requestBody = new FormEncodingBuilder()
                .add("OP", "LI")
                .add("UID", preferences.getString("user_id", ""))
                .add("PWD", preferences.getString("password", ""))
                .add("Login.x", "התחבר")
                .build();

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(requestBody)
                .build();
        try
        {
            Response response = mClient.newCall(request).execute();
            return response.body().string().contains("logout");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

}
