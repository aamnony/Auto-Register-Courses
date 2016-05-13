package com.gmail.aamnony.autoregistercourses;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class WaitingListActivity extends ListActivity
{
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        setupActionBar();

        refresh();
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id)
    {
        final Course course = (Course) getListAdapter().getItem(position);

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
                        Utils.WaitingList.remove(WaitingListActivity.this, course);
                        refresh();
                    }
                })
                .show();
    }

    private void refresh ()
    {
        Course[] courses = Utils.WaitingList.get(WaitingListActivity.this);
        if (courses != null && courses.length > 0)
        {
            setListAdapter(new CoursesAdapter(courses, WaitingListActivity.this));
        }
        else
        {
            setListAdapter(null);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar ()
    {
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


}
