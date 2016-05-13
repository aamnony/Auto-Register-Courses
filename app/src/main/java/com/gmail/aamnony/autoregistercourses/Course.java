package com.gmail.aamnony.autoregistercourses;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Objects;

final class Course
{
    public final String number;
    public final String group;
    public final String name;

    public Course (String number, String group)
    {
        this.number = number;
        this.group = group;
        this.name = "";
    }

    public Course (String number, String group, String name)
    {
        this.number = number;
        this.name = name;
        this.group = group;
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(number, course.number) &&
                Objects.equals(group, course.group);
    }

    @Override
    public int hashCode ()
    {
        return Objects.hash(number, group);
    }

    @Override
    public String toString ()
    {
        if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(group))
        {
            return number + '/' + group;
        }
        else return "";
    }

    public String fullInfo ()
    {
        return number + ' ' + name + " - קב' " + group;
    }
}
