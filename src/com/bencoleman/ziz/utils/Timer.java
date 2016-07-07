/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.utils;

/**
 * This is a simple helper class for timing events, will millisecond precision
 * Used by the monitor scripts
 *
 * @author Ben Coleman
 */
public class Timer
{
    long t1;
    
    public Timer()
    {
        t1 = System.currentTimeMillis();
    }

    public void start()
    {
        t1 = System.currentTimeMillis();
    }

    public long stop()
    {
        return System.currentTimeMillis() - t1;
    }
}
