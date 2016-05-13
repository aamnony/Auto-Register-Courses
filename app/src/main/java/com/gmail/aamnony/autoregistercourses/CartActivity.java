package com.gmail.aamnony.autoregistercourses;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import static com.gmail.aamnony.autoregistercourses.CartIntentService.ACTION_ADD;
import static com.gmail.aamnony.autoregistercourses.CartIntentService.ACTION_GET_ALL;
import static com.gmail.aamnony.autoregistercourses.CartIntentService.ACTION_REMOVE;
import static com.gmail.aamnony.autoregistercourses.CartIntentService.EXTRA_COURSE;
import static com.gmail.aamnony.autoregistercourses.CartIntentService.EXTRA_COURSES;
import static com.gmail.aamnony.autoregistercourses.CartIntentService.EXTRA_SUCCESS;

public class CartActivity extends ListActivity
{
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private Course mAddedCourse;
    private View emptyView;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setTitle(R.string.my_cart);

        emptyView = findViewById(android.R.id.empty);

        mFilter = new IntentFilter(ACTION_ADD);
        mFilter.addAction(ACTION_REMOVE);
        mFilter.addAction(ACTION_GET_ALL);

        mReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive (Context context, Intent intent)
            {
                if (intent != null)
                {
                    switch (intent.getAction())
                    {
                        case ACTION_ADD:
                            onAdd(new Gson().fromJson(intent.getStringExtra(EXTRA_COURSE), Course.class), intent.getBooleanExtra(EXTRA_SUCCESS, false));
                            break;
                        case ACTION_REMOVE:
                            onRemove(new Gson().fromJson(intent.getStringExtra(EXTRA_COURSE), Course.class), intent.getBooleanExtra(EXTRA_SUCCESS, false));
                            break;
                        case ACTION_GET_ALL:
                            onGetAll(new Gson().fromJson(intent.getStringExtra(EXTRA_COURSES), Course[].class));
                            break;
                    }
                }
            }
        };
        registerReceiver(mReceiver, mFilter);

        CartIntentService.startActionGetAll(CartActivity.this);
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        registerReceiver(mReceiver, mFilter);
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        final boolean debug = PreferenceManager.getDefaultSharedPreferences(CartActivity.this).getBoolean("debug", false);
        menu.findItem(R.id.action_open_log_file).setVisible(debug);
        menu.findItem(R.id.action_force_check).setVisible(debug);

        long nextCheck = PreferenceManager.getDefaultSharedPreferences(CartActivity.this).getLong("next_check", -1);
        if (nextCheck >= 0)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(nextCheck);
            menu.findItem(R.id.action_set_alarm).setTitle(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).format(cal.getTime()));
        }
        else
        {
            menu.findItem(R.id.action_set_alarm).setTitle(R.string.set_alarm);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_set_alarm:
                showSetAlarmDialog();
                return true;
            case R.id.action_open_log_file:
                showLogFile();
                return true;
            case R.id.action_force_check:
                CartIntentService.startActionRegister(CartActivity.this);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(CartActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_waiting_list:
                startActivity(new Intent(CartActivity.this, WaitingListActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id)
    {
        final Course course = (Course) getListAdapter().getItem(position);
        if (!TextUtils.isEmpty(course.toString()))
        {
            new AlertDialog.Builder(this)
                    .setMessage(String.format(getString(R.string.remove_course), course.toString()))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick (DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick (DialogInterface dialog, int which)
                        {
                            CartIntentService.startActionRemove(CartActivity.this, course);
                        }
                    })
                    .show();
        }
    }

    public void onFabClick (@SuppressWarnings("UnusedParameters") View view)
    {
        showAddDialog();
    }

    private void showLogFile ()
    {
        new AlertDialog.Builder(this)
                .setMessage(Log.read(getApplicationContext()))
                .setNeutralButton("Clear", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog, int which)
                    {
                        Log.clear(getApplicationContext());
                    }
                })
                .show();
    }

    private void onAdd (Course course, boolean success)
    {
        if (success)
        {
            Toast.makeText(CartActivity.this,
                    String.format(getString(R.string.add_success), course.toString()),
                    Toast.LENGTH_SHORT).show();
            CartIntentService.startActionGetAll(CartActivity.this);
            setListAdapter(null);
            mAddedCourse = course;
        }
        else
        {
            showAddToWaitingListDialog(course);
        }
    }

    private void onRemove (Course course, boolean success)
    {
        if (success)
        {
            Toast.makeText(CartActivity.this,
                    String.format(getString(R.string.remove_success), course.toString()),
                    Toast.LENGTH_SHORT).show();
            CartIntentService.startActionGetAll(CartActivity.this);
            setListAdapter(null);
        }
        else
        {
            Toast.makeText(CartActivity.this,
                    String.format(getString(R.string.remove_error), course.toString()),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void onGetAll (Course[] courses)
    {
        if (courses.length > 0)
        {
            // TODO: Check if needed.
            boolean registered = mAddedCourse != null;
            for (Course c : courses)
            {
                if (c.equals(mAddedCourse))
                {
                    registered = false;
                }
            }
            if (registered)
            {
                Utils.postNotification(String.format("נרשמת לקורס %s", mAddedCourse.toString()), Integer.parseInt(mAddedCourse.number), CartActivity.this);
            }
            // --------------------------------------------
            CoursesAdapter adapter = new CoursesAdapter(courses, CartActivity.this);
            setListAdapter(adapter);
        }
        else
        {
            emptyView.setVisibility(View.INVISIBLE);
        }
        mAddedCourse = null;
    }

    private void showSetAlarmDialog ()
    {
        final Calendar cal = Calendar.getInstance();

        long nextCheck = PreferenceManager.getDefaultSharedPreferences(CartActivity.this).getLong("next_check", -1);
        if (nextCheck >= 0)
        {
            cal.setTimeInMillis(nextCheck);
        }

        new DatePickerDialog(CartActivity.this, R.style.AppTheme_Dialog, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet (DatePicker view, final int year, final int monthOfYear, final int dayOfMonth)
            {
                new TimePickerDialog(CartActivity.this, R.style.AppTheme_Dialog, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet (TimePicker view, int hourOfDay, int minute)
                    {
                        long nextCheck = Long.parseLong(  // in millis.
                                PreferenceManager.getDefaultSharedPreferences(CartActivity.this).getString("sync_frequency", "1800000"));

                        cal.set(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                        RegisterCartReceiver.scheduleRegisterCartAlarm(CartActivity.this, cal.getTimeInMillis(), nextCheck);

                        invalidateOptionsMenu();

                        if (PreferenceManager.getDefaultSharedPreferences(CartActivity.this).getBoolean("debug", false))
                        {
                            Toast.makeText(CartActivity.this,
                                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).format(cal.getTime()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showAddDialog ()
    {
        final View view = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);

        final EditText course = ((EditText) view.findViewById(R.id.dialog_add_to_cart_editCourse));
        final EditText group = ((EditText) view.findViewById(R.id.dialog_add_to_cart_editGroup));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_course_to_cart)
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.add_to_cart, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog, int which)
                    {
                        if (TextUtils.isEmpty(course.getText()) || TextUtils.isEmpty(group.getText()))
                        {
                            Toast.makeText(CartActivity.this, R.string.empty_course_or_group, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            CartIntentService.startActionAdd(CartActivity.this, new Course(course.getText().toString(), group.getText().toString()));
                        }
                    }
                }).create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    private void showAddToWaitingListDialog (final Course course)
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_course_to_cart)
                .setTitle(String.format(getString(R.string.add_error), course.toString()))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.add_to_waiting_list, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick (DialogInterface dialog, int which)
                    {
                        Utils.WaitingList.add(CartActivity.this, course);
                    }
                }).show();
    }

}
