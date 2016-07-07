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
import java.util.Vector;
import javax.servlet.http.*;

/**
 *
 * @author colemanb
 */
public class HistoryPage extends Page
{
    private Monitor monitor;
    private int prev_group_id = -1;

    public HistoryPage(String target, HttpServletRequest request, HttpServletResponse response)
    {
        super(target, request, response);

        try {
            Cookie[] cookies = request.getCookies();
            if(cookies != null) {
                for(Cookie c : cookies) {
                    if(c.getName().equals("prev_group")) {
                        prev_group_id = Integer.parseInt(c.getValue());
                    }
                }
            }
        } catch(Exception e) {
            Main.log.error("Cookie error: "+e.getMessage());
        }

        String[] parts = target.split("/");
        int mon_id = Integer.parseInt(parts[2]);
        this.monitor = Main.runner.getMonitorById(mon_id);

        setTitle("History: "+monitor.getName());
        setRefresh(true);
    }

    void render()
    {
        doPageHeader();

        String back_link = "/dash";
        if(prev_group_id != -1)
            back_link = "/dash/"+prev_group_id;

        out.println("<p align='center'><a href='"+back_link+"' style='color:grey'><img src='/img/undo-16.png'> Return to the Dashboard</a><br/>");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");

            synchronized(this) {
                Vector<Result> results = monitor.getResults();

                double avg_val = 0.0;
                double tot_val = 0.0;
                int val_count = 0;
                int good_count = 0;
                int warn_count = 0;
                int error_count = 0;
                for(Result result : results) {
                    if(result.status == Result.STATUS_GOOD) good_count++;
                    if(result.status == Result.STATUS_WARN) warn_count++;
                    if(result.status == Result.STATUS_ERROR) error_count++;
                    if(Double.isNaN(result.value)) continue;
                    tot_val += result.value;
                    val_count++;
                }
                avg_val = tot_val / val_count;
                if(Double.isNaN(avg_val))
                    avg_val = 0.0;

                out.println("<table align='center' cellspacing='5'>");
                java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
                out.println("<tr style='padding:0px'><td><b>Average:</b></td><td>"+df.format(avg_val)+monitor.getUnits()+"</td>");
                out.println("<td<b>&nbsp;&nbsp;&nbsp;Min:</b></td><td>"+monitor.getResultsMin()+monitor.getUnits()+"</td>");
                out.println("<td><b>&nbsp;&nbsp;&nbsp;Max:</b></td><td>"+monitor.getResultsMax()+monitor.getUnits()+"</td></tr>");
                out.println("<tr><td><b>Good:</b></td><td>"+(int)((good_count/(float)results.size())*100)+"%</td>");
                out.println("<td><b>&nbsp;&nbsp;&nbsp;Warning:</b></td><td>"+(int)((warn_count/(float)results.size())*100)+"%</td>");
                out.println("<td><b>&nbsp;&nbsp;&nbsp;Error:</b></td><td>"+(int)((error_count/(float)results.size())*100)+"%</td></tr>");
                out.println("</table><table align='center'>");

                // loop backwards so results are listed with newest at the top
                for(int ri = results.size()-1; ri >= 0; ri--) {
                    Result result = results.get(ri);
                    out.println("<tr style='color: black; background-color:"+result.statusColour()+"'>" +
                            "<td style='padding:2px'>"+sdf.format(result.dt)+"</td>" +
                            "<td style='padding:2px; font-weight:bold;'>"+result.statusName()+"</td>" +
                            "<td style='padding:2px; '>"+result.value+monitor.getUnits()+"</td>" +
                            "<td style='padding:2px'>"+Utils.stringToHTMLString(result.msg)+"</td></tr>");
                }
            }
        } catch (Exception e) {
            Main.log.error(e);
        }

        out.println("</table>");

        doPageFooter();
    }
}
