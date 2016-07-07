/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import com.bencoleman.ziz.*;
import com.bencoleman.ziz.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.*;

/**
 *
 * @author colemanb
 */
public class DialPage extends Page
{
    private Group group;
    private int size;

    public DialPage(String target, HttpServletRequest request, HttpServletResponse response)
    {
        super(target, request, response);

        String[] parts = target.split("/");
        int group_id = -1;
        if(parts.length > 2)
            group_id = Integer.parseInt(parts[2]);
        this.group = Main.runner.getGroupById(group_id);

        if(this.group != null)
            setTitle("Dials: "+this.group.getName());
        else
            setTitle("Dials");

        //setRefresh(true);
    }

    public void render()
    {

        addHeader("<script type='text/javascript' src='https://www.google.com/jsapi'></script>");
        addHeader("<script type='text/javascript'>" +
                      "google.load('visualization', '1', {packages:['gauge']});"+
                      "google.setOnLoadCallback(drawCharts);"+
                      "function drawCharts() {");


        try {
            int group_id = -1;

            out.println("<div id='message' style='text-align:center; color: red'></div>" +
                    "<table cellpadding='5' align='center' style='font-size:"+1+"em'>\n");

            Vector<Monitor> monitor_collection;

            // at root level so, loop and show groups
            if(group == null) {
                monitor_collection = Main.runner.getMonitors();
            } else {
                group_id = group.getId();
                monitor_collection = group.getMonitors();
                out.println("<p align='center'><a href='/dash'><img src='/img/undo.png' align='absmiddle'/>&nbsp;&nbsp;<i>Back Up</i></a>\n");
            }

            int m_index = 0;
            for (Monitor m : monitor_collection) {
                if(m.getParentGroup() != group) {
                    // this monitor isn't in the group we want - ignore
                    continue;
                }

                Result r = m.getLastResult();
                double value = 0.0;
                if(r == null) {
                    value = 0.0;
                } else {
                    value = r.value;
                }

                // code to work out a decent max value for the dial
                double wibble = value;
                int max = 0;
                do {
                    wibble = wibble / 10.0;
                    max++;
                } while(wibble > 1);
                max = (int)Math.pow(10.0, (double)max);

                // half it if we're under halfway
                if(value / max < 0.5) {
                    max /= 2.0;
                }

                // hardcoded max-values for certain monitor types
                String mtype = m.getType();
                if(mtype.toLowerCase().contains("cpu")) max = 100;
                if(mtype.toLowerCase().contains("disk")) max = 100;
                if(mtype.toLowerCase().contains("filesystem")) max = 100;


                String colours = "";
                String warn = m.getProperties().getProperty("warning_threshold");
                String error = m.getProperties().getProperty("error_threshold");

                if(error != null && warn != null) {
                    colours += ", greenFrom: 0, greenTo:"+warn;
                    colours += ", yellowFrom: "+warn+", yellowTo:"+error;
                    colours += ", redFrom: "+error+", redTo:"+max;
                } else if(warn != null) {
                    colours += ", greenFrom: 0, greenTo:"+warn;
                    colours += ", yellowFrom: "+warn+", yellowTo:"+max;
                } else if(error != null) {
                    colours += ", greenFrom: 0, greenTo:"+error;
                    colours += ", redFrom: "+error+", redTo:"+max;
                }

                String lwarn = m.getProperties().getProperty("warning_threshold_low");
                String lerror = m.getProperties().getProperty("error_threshold_low");

                if(lerror != null && lwarn != null) {
                    colours += ", redFrom: 0, redTo:"+lerror;
                    colours += ", yellowFrom: "+lerror+", yellowTo:"+lwarn;
                    colours += ", greenFrom: "+lwarn+", greenTo:"+max;
                } else if(lerror != null) {
                    colours += ", greenFrom: "+lerror+", greenTo:"+max;
                    colours += ", redFrom: 0, redTo:"+lerror;
                } else if(lwarn != null) {
                    colours += ", greenFrom: "+lwarn+", greenTo:"+max;
                    colours += ", yellowFrom: 0, yellowTo:"+lwarn;
                }

                addHeader("var data"+m_index+" = new google.visualization.DataTable();");
                addHeader("data"+m_index+".addRows("+monitor_collection.size()+");");
                addHeader("data"+m_index+".addColumn('string', 'Label'); data"+m_index+".addColumn('number', 'Value');");
                addHeader("data"+m_index+".setValue("+m_index+", 0, ''); data"+m_index+".setValue("+m_index+", 1, "+value+");");
                addHeader("var chart"+m_index+" = new google.visualization.Gauge(document.getElementById('dial_div"+m_index+"'));" +
                          "var options"+m_index+" = {height:150, max:"+max+" "+colours+"};" +
                          "chart"+m_index+".draw(data"+m_index+", options"+m_index+");");
        
                m_index++;
            }
            
            addHeader("}</script>");

            doPageHeader();

            m_index = 0;
            for (Monitor m : monitor_collection) {
                if(m.getParentGroup() != group) {
                    // this monitor isn't in the group we want - ignore
                    continue;
                }
                //onclick='setCookie(\"prev_group\", "+group_id+", false, \"/\"); document.location.href=\"/graph/"+m.getId()+"\"'
                out.println("<div style='width: 150; margin-left: auto; margin-right: auto;' id='dial_div"+m_index+"'></div>"
                          + "<div style='width: 300; margin-left: auto; margin-right: auto; text-align:center'>"+m.getName()+"</div>");
                m_index++;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            out.println("<div class='time'>&nbsp;Updated: "+sdf.format(new Date())+" <a href='javascript:window.location.reload()'><img src='/img/reload.png'/></a>" +
                    " <a href='/'><img src='/img/gohome.png'/></a>" +
                    " <a href='/dash/"+group_id+"'><img src='/img/dash-16.png'/></a>" +
                    "</div>");

            doPageFooter();

        } catch (Exception e) {
            e.printStackTrace();
            Main.log.error(e);
        }


        doPageFooter();
    }
}
