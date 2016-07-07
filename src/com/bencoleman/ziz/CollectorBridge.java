/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

import java.io.*;
import java.net.*;

/**
 *
 * @author colemanb
 */
public class CollectorBridge
{
    private int port;
    private BufferedWriter writer;
    private BufferedReader reader;
    private char delim = '\003';
    private Socket socket;
    private boolean connected;
    private static Process win_collector;

    public CollectorBridge(int port)
    {
        // Shutdown hook, needed to close the external collector process
        Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    killProcess();
                }
            });
            
        this.port = port;
        this.connected = false;
        launchProcess(true);
        connect();
    }

    public void launchProcess(boolean kill_first)
    {
        // External collector process - used to collect WMI stats from Windows machines
        try {
            if(kill_first) {
                Process kill = Runtime.getRuntime().exec("taskkill /IM ZizCollector.exe /F");
                kill.waitFor();
            }

            Main.log.info("Launching external collector process...");
            win_collector = Runtime.getRuntime().exec("bin/ZizCollector.exe "+port);
        } catch (Exception ex) {
            Main.log.error("Error launching external collector process. Windows monitors will be unavailable. "+ex.getMessage());
        }
    }

    public void killProcess()
    {
        try {
            win_collector.destroy();
        } catch (Exception ex) {
            Main.log.error("Error killing external collector process: "+ex.getMessage());
        }
    }

    public void connect()
    {
        try {
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            socket = new Socket(addr, port);

            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connected = true;
            Main.log.info("Connected to collector OK");
        } catch(Exception e){
            connected = false;
            Main.log.error("Error opening connection to collector, it might not be running");
        }
    }

    public void disconnect()
    {
        try {
            writer.close();
            socket.close();
            Main.log.info("Disconnected from collector OK");
            connected = false;
        } catch(Exception e){
            Main.log.error("Error disconnecting from collector: "+e.getMessage());
        }
    }

    public String sendCommand(String msg)
    {
        if(!connected || !socket.isConnected())
            connect();
        
        synchronized(this) {
            try {
                writer.write(msg + "\n");
                writer.flush();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String resp = reader.readLine();
                //System.out.println("GOT = "+resp);
                return resp;
            } catch (IOException ex) {
                return "ERROR: "+ex.getMessage();
            }
        }
    }

    public String makeCommand(String[] params)
    {
        String out = params[0];
        for(int p = 1; p < params.length; p++) {
            out += delim + params[p];
        }

        return out;
    }
}
