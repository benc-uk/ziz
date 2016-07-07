/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.script.*;
import org.apache.log4j.*;
//import org.python.core.*;
//import org.python.util.PythonInterpreter;

/**
 *
 * @author colemanb
 */
public class Monitor implements Runnable
{
    public static final String MON_PATH = "monitors/";

    private String name;
    private int interval;
    private int ticks;
    private Vector<Result> results;
    private String type;
    private Thread thread;
    private Properties props;
    private Logger data;
    private Date last;
    private Date next;
    private int max_results;
    private Group parent;
    private int id = -1;
    private String units;
    private DataLogger logger;
    private boolean has_alerted;

    public Monitor(String type, String name, int intv, Properties properties, Group g, int id)
    {
        this.type = type;
        this.name = name;
        // convert interval from seconds to mill-secs
        this.interval = intv * 1000;
        // ticker starts at some random offset into the interval, this is to 'space out' the monitors runs
        Random rnd = new Random();
        this.ticks = rnd.nextInt(interval);
        this.props = properties;
        this.parent = g;
        this.id = id;

        logger = new DataLogger(this.getNameAndGroup());

        max_results = Main.config.getInt("dash_max_results");
        results = new Vector<Result>(max_results);
        has_alerted = false;
    }
    
    public Properties getProperties() {
        return props;
    }

    public Result getLastResult() {
        try {
            return results.lastElement();
        } catch (Exception e) {
            return null;
        }
    }

    public Vector<Result> getResults() {
        return results;
    }

    public int getTicks() {
        return ticks;
    }

    public String getType() {
        return type;
    }

    public void tick(int t)
    {
        ticks = ticks - t;
    }

    public void resetTicks()
    {
        ticks  = interval;
    }

    public int getInterval() {
        return interval;
    }

    public int getIntervalMins() {
        return interval / 60000;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getName() {
        return name;
    }

    public String toString()
    {
        return name + " (" + type + ")";
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void run() {

        try {
            // Run the monitor script, and get the result
            Result res = runScript();
            // Set the timestamp on the results
            res.setDateNow();

            // round value down to 2 decimal places
            if(!Double.isNaN(res.value)) {
                java.text.DecimalFormat twoDForm = new java.text.DecimalFormat("#.##");
                res.value = Double.valueOf(twoDForm.format(res.value));
            }

            // Logic for monitor thresholds, warning & error, upper and lower thresholds
            if(props.containsKey("error_threshold") && res.status != Result.STATUS_ERROR) {
                int error_t = Integer.parseInt(props.getProperty("error_threshold"));
                if(res.value > error_t) {
                    res.status = Result.STATUS_ERROR; 
                    res.msg = "Error threshold exceeded ("+res.value+" > "+error_t+")";
                }               
            }
            if(props.containsKey("error_threshold_low") && res.status != Result.STATUS_ERROR) {
                int error_t = Integer.parseInt(props.getProperty("error_threshold_low"));
                if(res.value < error_t) {
                    res.status = Result.STATUS_ERROR;
                    res.msg = "Error threshold exceeded ("+res.value+" < "+error_t+")";
                }
            }
            if(props.containsKey("warning_threshold") && res.status != Result.STATUS_ERROR && res.status != Result.STATUS_WARN) {
                int error_t = Integer.parseInt(props.getProperty("warning_threshold"));
                if(res.value > error_t) {
                    res.status = Result.STATUS_WARN;
                    res.msg = "Warning threshold exceeded ("+res.value+" > "+error_t+")";
                }
            }
            if(props.containsKey("warning_threshold_low") && res.status != Result.STATUS_ERROR && res.status != Result.STATUS_WARN) {
                int error_t = Integer.parseInt(props.getProperty("warning_threshold_low"));
                if(res.value < error_t) {
                    res.status = Result.STATUS_WARN;
                    res.msg = "Warning threshold exceeded ("+res.value+" < "+error_t+")";
                }
            }

            // Update the last run time and next run time fields
            Calendar c = Calendar.getInstance();
            last = new Date();
            c.setTime(last);
            c.add(Calendar.MILLISECOND, interval);
            next = c.getTime();

            // Store result in memory history buffer and in data log
            // Also triggers alerts
            addResult(res);

            // Update the parent group if present
            if(getParentGroup() != null) {
                getParentGroup().update();
            }

            Main.log.debug("Monitor run complete: "+name+" result="+res.status+", "+res.value+", "+res.msg);
        } catch (Exception e) {
            Main.log.error(e);
        }
    }

    /**
     * Start the monitor run in a seperate new thread
     */
    public void startMonitor()
    {
        thread = new Thread(this, this.toString());
        thread.start();
    }

    public String getLastRun()
    {
        if(last == null) return "Never";

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm:ss");
        return sdf.format(last);
    }

    public String getNextRun()
    {
        if(next == null) return "Unknown";

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy HH:mm:ss");
        return sdf.format(next);
    }

    /**
     * Store the result in the CSV logs, in the results history and trigger any alerts
     * @param res Result to be stored
     */
    public void addResult(Result res)
    {
        // Store result in CSV logs
        /*if(Boolean.parseBoolean(Main.config.getProperty("data_store_enabled"))) {
            data.info(res.status+","+res.value+","+res.msg);
        }*/
        logger.logData(res);

        // Add to in-memory history. Buffer is fixed size, old results get pushed out
        results.add(res);
        if(results.size() > max_results) {
            results.remove(results.firstElement());
        }

        // check for alerts
        boolean alert = false;
        if(props.contains("alert")) {
            alert = Boolean.parseBoolean(props.getProperty("alert"));
        } else {
            alert = Main.config.getBool("alert_enabled");
        }
        if(alert) {
            int trigger_count = Main.config.getInt("alert_trigger_count");
            int alert_level = Main.config.getInt("alert_status_trigger");
            int count = 0;

            // count backward the last trigger_count results, and count the number that are over the trigger threshold
            if(results.size() >= trigger_count + 1) {
                for(int r = results.size()-1; r > results.size()-1-trigger_count; r--) {
                    // result triggers the alert, so count it
                    if(results.get(r).status <= alert_level) count++;
                }
            }
            // if we have thre right number then get the NEXT one previous and see if that was OK
            // if it was then alert
            if(count == trigger_count) {
                if(results.get(results.size()-1-trigger_count).status > alert_level) {
                    new Alerter(this, res).alert();
                    has_alerted = true;
                }
            }
            if(results.size() > 1 && Main.config.getBool("alert_good_followup")) {
                Result last_result = results.get(results.size()-1);
                //Result previous = results.get(results.size()-2);
                if(last_result.status == Result.STATUS_GOOD && has_alerted) {
                    new Alerter(this, res).alert();
                    has_alerted = false;
                }
            }
        }
    }

    public Group getParentGroup()
    {
        return parent;
    }

    public void forceRun()
    {
        this.ticks = 0;
    }

    /**
     * Conveniance method for testing, force this monitor to send an alert
     */
    public void forceAlert()
    {
        Result fake = new Result();
        fake.status = Result.STATUS_ERROR;
        fake.value = 9999.0;
        fake.msg = "Forced fake status for alert test";
        fake.dt = new Date();
        
        Alerter alerter = new Alerter(this, fake);
        alerter.alert();
    }

    /**
     * Run this monitor's external script. The script does all of the real monitoring work and updates a Result object which
     * is then returned by this method
     * @return the Result object populated by the script
     */
    private Result runScript()
    {
        // New empty result object
        Result res = new Result();

        try {
            // Create a JavaScript script engine
            ScriptEngine script = new ScriptEngineManager().getEngineByName("JavaScript");

            // pass the script the result object in a variable called 'result'
            script.put("result", res);
            // pass config for conveniance
            script.put("config", Main.config);
            script.put("monitor", this);
            for (Enumeration e = props.keys(); e.hasMoreElements(); /**/) {
                String key = (String) e.nextElement();
                String value = props.getProperty(key);
                script.put(key, value);
            }

            script.eval(new java.io.FileReader(MON_PATH+"lib/mon_utils.js"));
            java.io.FileReader script_file = new java.io.FileReader(MON_PATH+type+".js");
            script.eval(script_file);

            Invocable invocableEngine = (Invocable)script;
            invocableEngine.invokeFunction("main");

            if(script.get("units") != null) {
                this.units = ""+script.get("units");
            }

        } catch (Exception ex) {
            Main.log.error("Error running monitor "+name+" - "+ex.getMessage());
            res.status = Result.STATUS_FAILED;
            res.msg = ex.getMessage();
        }

        return res;
    }

    public double getResultsMax()
    {
        double max_val = Double.NEGATIVE_INFINITY;
        for(Result r : results) {
            if(r.value > max_val)
                max_val = r.value;
        }
        if(max_val == Double.NEGATIVE_INFINITY)
            return 0.0;
        else
            return max_val;
    }

    public double getResultsMin()
    {
        double min_val = Double.POSITIVE_INFINITY;
        for(Result r : results) {
            if(r.value < min_val)
                min_val = r.value;
        }
        if(min_val == Double.POSITIVE_INFINITY)
            return 0.0;
        else
            return min_val;
    }

    public String getUnits() 
    {
        if(this.units != null)
            return units;
        else
            return "";
    }

    public String getNameAndGroup()
    {
        if(parent != null)
            return name + " ["+parent.getName()+"]";
        else
            return name;
    }

}
