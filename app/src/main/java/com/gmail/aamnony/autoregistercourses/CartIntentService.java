package com.gmail.aamnony.autoregistercourses;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class CartIntentService extends IntentService
{
    static final String ACTION_ADD = "com.gmail.aamnony.autoregistercourses.action.ADD";
    static final String ACTION_REMOVE = "com.gmail.aamnony.autoregistercourses.action.REMOVE";
    static final String ACTION_GET_ALL = "com.gmail.aamnony.autoregistercourses.action.GET_ALL";
    static final String ACTION_REGISTER = "com.gmail.aamnony.autoregistercourses.action.REGISTER";

    static final String EXTRA_COURSE = "com.gmail.aamnony.autoregistercourses.extra.COURSE";
    static final String EXTRA_COURSES = "com.gmail.aamnony.autoregistercourses.extra.COURSES";
    static final String EXTRA_SUCCESS = "com.gmail.aamnony.autoregistercourses.extra.SUCCESS";


    public CartIntentService ()
    {
        super("CartIntentService");
    }

    @Override
    protected void onHandleIntent (Intent intent)
    {
        if (intent != null)
        {
            String extra;
            Cart cart = new Cart(getApplicationContext());
            switch (intent.getAction())
            {
                case ACTION_ADD:
                    extra = intent.getStringExtra(EXTRA_COURSE);
                    intent = new Intent(ACTION_ADD);
                    intent.putExtra(EXTRA_COURSE, extra);
                    intent.putExtra(EXTRA_SUCCESS, cart.add(new Gson().fromJson(extra, Course.class)));
                    sendBroadcast(intent);
                    break;
                case ACTION_REMOVE:
                    extra = intent.getStringExtra(EXTRA_COURSE);
                    intent = new Intent(ACTION_REMOVE);
                    intent.putExtra(EXTRA_COURSE, extra);
                    intent.putExtra(EXTRA_SUCCESS, cart.remove(new Gson().fromJson(extra, Course.class)));
                    sendBroadcast(intent);
                    break;
                case ACTION_GET_ALL:
                    intent = new Intent(ACTION_GET_ALL);
                    intent.putExtra(EXTRA_COURSES, new Gson().toJson(cart.getAll()));
                    sendBroadcast(intent);
                    break;
                case ACTION_REGISTER:
                    Course[] waitingCourses = Utils.WaitingList.get(CartIntentService.this);
                    if (waitingCourses != null)
                    {
                        for (Course c : waitingCourses)
                        {
                            if (cart.add(c))
                            {
                                Utils.postNotification(String.format(getString(R.string.add_success), c), Integer.parseInt(c.number), CartIntentService.this);
                                Utils.WaitingList.remove(CartIntentService.this, c);
                            }
                        }
                    }
                    registerCart(cart);
                    break;
            }
        }
    }

    /**
     * Starts this service to add a given {@link Course} to cart.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionAdd (Context context, Course course)
    {
        Intent intent = new Intent(context, CartIntentService.class);
        intent.setAction(ACTION_ADD);
        intent.putExtra(EXTRA_COURSE, new Gson().toJson(course));
        context.startService(intent);
    }

    /**
     * Starts this service to remove a given {@link Course} from cart.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemove (Context context, Course course)
    {
        Intent intent = new Intent(context, CartIntentService.class);
        intent.setAction(ACTION_REMOVE);
        intent.putExtra(EXTRA_COURSE, new Gson().toJson(course));
        context.startService(intent);
    }

    /**
     * Starts this service to get all courses from cart.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetAll (Context context)
    {
        Intent intent = new Intent(context, CartIntentService.class);
        intent.setAction(ACTION_GET_ALL);
        context.startService(intent);
    }

    /**
     * Starts this service to register cart.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRegister (Context context)
    {
        Intent intent = new Intent(context, CartIntentService.class);
        intent.setAction(ACTION_REGISTER);
        context.startService(intent);
    }

    private void registerCart (Cart cart)
    {
        Course[] registeredCourses = cart.register();
        ArrayList<Course> newlyRegistered = Utils.compare(registeredCourses, Utils.CurrentRegisteredCourses.get(CartIntentService.this));
        Utils.CurrentRegisteredCourses.set(CartIntentService.this, registeredCourses); // update list.

        if (newlyRegistered.size() > 0)
        {
            Utils.postNotification(newlyRegistered, CartIntentService.this);
        }

        if (PreferenceManager.getDefaultSharedPreferences(CartIntentService.this).getBoolean("debug", false))
        {
            Log.append(getApplicationContext(), newlyRegistered.size());
        }
    }

}
