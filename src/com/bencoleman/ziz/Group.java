/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

import java.util.*;

/**
 *
 * @author colemanb
 */
public class Group
{
    private Vector<Monitor> monitors;
    private String name;
    private int status;
    private int prev_status;
    private String message;
    private int id;
    private Monitor causer;

    public Group()
    {
        prev_status = 99;
        status = 99;
        name = "No Name";
        monitors = new Vector(5);
        message = "No details";
    }

    public Group(String name)
    {
        prev_status = 99;
        status = 99;
        this.name = name;
        monitors = new Vector(5);
        message = "No details";
    }

    public Vector<Monitor> getMonitors()
    {
        return monitors;
    }

    public void addMonitor(Monitor mon)
    {
        this.monitors.add(mon);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public String toString()
    {
        return name;
    }

    public void update()
    {
        int worst = 9999;
        causer = null;
        for(Monitor m : monitors) {
            if(m.getLastResult() == null) continue;
            int m_status = m.getLastResult().status;

            if(m_status < worst) {
                worst = m_status;
                causer = m;
            }
        }

        if(worst != 9999) {
            prev_status = status;
            status = worst;
            switch(status) {
                case Result.STATUS_GOOD: message = "All sub-monitors good"; break;
                case Result.STATUS_WARN: message = "Warning caused by: "+causer.getName(); break;
                case Result.STATUS_ERROR: message = "Error caused by: "+causer.getName(); break;                
            }
        }
    }

    public String getMessage() {
        return message;
    }

    public Monitor getCauser() {
        return causer;
    }

    public int getPrevStatus() {
        return prev_status;
    }
    
}
