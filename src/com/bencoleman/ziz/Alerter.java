/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

import javax.script.*;

/**
 *
 * @author colemanb
 */
public class Alerter implements Runnable
{
    private Thread thread;
    private Monitor monitor;
    private Result result;
    public static final String ALERT_PATH = "alert/";

    public Alerter(Monitor m, Result r)
    {
        this.monitor = m;
        this.result = r;
    }

    public void alert()
    {
        thread = new Thread(this, "Alert Thread for "+monitor.getName());
        thread.start();
    }

    public void run()
    {
        try {
            // Create a JavaScript script engine
            ScriptEngine script = new ScriptEngineManager().getEngineByName("JavaScript");

            script.eval(new java.io.FileReader(ALERT_PATH+"alert_utils.js"));
            java.io.FileReader script_file = new java.io.FileReader(ALERT_PATH+"alert.js");
            script.put("result", result);
            script.put("monitor", monitor);
            script.put("config", Main.config);
            script.eval(script_file);
        } catch (Exception ex) {
            Main.log.error("Error running alert script "+ex.getMessage());
        }
    }

}
