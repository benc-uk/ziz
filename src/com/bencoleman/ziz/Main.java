/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

import com.bencoleman.ziz.web.WebServer;
import java.io.*;
import org.apache.log4j.*;

/**
 *
 * @author Ben Coleman
 */
public class Main
{
    public static Config config;
    public static Logger log;
    public static MonitorRunner runner;
    public static final String CONF_DIR = "config/";
    public static final String CONF_FILE = "system.properties";
    public static final String MONITOR_FILE = "monitors.xml";
    public static final String VERSION = "0.9.0";
    
    public static CollectorBridge collector;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // Load the config file
        config = new Config();
        try {
            config.load(new FileInputStream(CONF_DIR+CONF_FILE));
        } catch (IOException ioe) {
            System.err.println(" ### Unable to load config from file: "+CONF_DIR+CONF_FILE);
            System.err.println(" ### Ziz can not start, exiting with RC=188");
            System.exit(188);
        }

        // Create our main logger
        log = createLog("system");
       
        log.info("");
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
        log.info("Ziz is starting up!");

        // Create the main monitor runner, this does 99% of the work
        runner = new MonitorRunner();

        // Webserver for displaying status and other information
        WebServer ws = new WebServer(config.getInt("web_port"));

        // Collector is an external process used to get metrics via WMI from Windows hosts
        if(config.getBool("collector_enabled")) {
            collector = new CollectorBridge(config.getInt("collector_port"));
        }

        // *****************************************************************
        // *** TEST CODE ***************************************************
        // *****************************************************************
        //Main.runner.getMonitors().firstElement().forceRun();
    }

    private static Logger createLog(String name)
    {
        Logger log;

        String log_path = "log/";
        String log_level = Main.config.getStr("sys_log_level");

        Layout file_layout = new PatternLayout("%d{HH:mm:ss dd-MM-yyyy}\t%p\t%C{1}\t%m%n");
        Layout console_layout = new PatternLayout("%d{HH:mm:ss} %m%n");
        log = Logger.getLogger(name);
        log.setLevel(Level.toLevel(log_level));

        try {
            log.addAppender(new DailyRollingFileAppender(file_layout, log_path+name+".log", "'.'yyyy-MM-dd'.log'"));
            log.addAppender(new ConsoleAppender(console_layout));
        } catch (IOException ioe) {
            System.err.println("Can't create log '"+name+"' check config file for log settings");
            return null;
        }

        return log;
    }
}
