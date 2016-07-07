/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */
package com.bencoleman.ziz;

import java.util.Date;

/**
 *
 * @author colemanb
 */
public class Result
{
    public static final int STATUS_GOOD = 10;
    public static final int STATUS_WARN = 5;
    public static final int STATUS_ERROR = 0;
    //public static final int STATUS_RUNNING = -1;
    //public static final int STATUS_NOTRUN = -2;
    public static final int STATUS_FAILED = -3;

    public double value;
    public int status;
    public String msg;
    public Date dt;

    public boolean alerted;
    
    public Result()
    {
        value = Double.NaN;
        status = STATUS_GOOD;
        msg = "Monitor has not run yet";
    }

    public String toString()
    {
        return value+", "+status+", "+msg;
    }

    public String statusName()
    {
        switch(status) {
            case STATUS_GOOD:
                return "Good";
            case STATUS_WARN:
                return "Warning";
            case STATUS_ERROR:
                return "Error";
            //case STATUS_NOTRUN:
            //    return "Never run";
            //case STATUS_RUNNING:
            //    return "Running now";
            case STATUS_FAILED:
                return "Failed";
            default:
                return "Other";
        }
    }

    public static String statusName(int status)
    {
        Result temp = new Result();
        temp.status = status;
        return temp.statusName();
    }

    public static String statusColor(int stat)
    {
        switch(stat) {
            case STATUS_GOOD:
                return "#4EDB39";
            case STATUS_WARN:
                return "#FFC235";
            case STATUS_ERROR:
                return "#EA1717";
            //case STATUS_NOTRUN:
            //    return "#C1C1C1";
            //case STATUS_RUNNING:
            //    return "#C1C1C1";
            case STATUS_FAILED:
                return "#131FD3";
            default:
                return "#666666";
        }
    }

    public String statusColour()
    {
        switch(status) {
            case STATUS_GOOD:
                return "#4EDB39";
            case STATUS_WARN:
                return "#FFC235";
            case STATUS_ERROR:
                return "#EA1717";
            //case STATUS_NOTRUN:
            //    return "#C1C1C1";
            //case STATUS_RUNNING:
            //    return "#C1C1C1";
            case STATUS_FAILED:
                return "#131FD3";
            default:
                return "#666666";
        }
    }

    public void setDateNow()
    {
        dt = new Date();
    }
}
