package com.gmail.aamnony.autoregistercourses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class CoursesAdapter extends BaseAdapter
{
    private final Course[] mCourses;
    private final Context mContext;

    public CoursesAdapter (Course[] courses, Context context)
    {
        mCourses = courses;
        mContext = context;
    }

    @Override
    public int getCount ()
    {
        return mCourses.length;
    }

    @Override
    public Course getItem (int position)
    {
        return mCourses[position];
    }

    @Override
    public long getItemId (int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_course, parent, false);
            holder = new ViewHolder();
            holder.numberGroup = (TextView) convertView.findViewById(R.id.item_course_txtNumberGroup);
            holder.name = (TextView) convertView.findViewById(R.id.item_course_txtName);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bind(getItem(position));
        return convertView;
    }

    private static class ViewHolder
    {
        TextView name;
        TextView numberGroup;

        private void bind (Course c)
        {
            numberGroup.setText(c.toString());
            name.setText(c.name);
        }
    }
}
