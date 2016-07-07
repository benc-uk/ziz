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
public class DashboardPage extends Page
{
    private Group group;
    private int size;

    public DashboardPage(String target, HttpServletRequest request, HttpServletResponse response)
    {
        super(target, request, response);

        this.size = 2;
        try {
            Cookie[] cookies = request.getCookies();
            if(cookies != null) {
                for(Cookie c : cookies) {
                    if(c.getName().equals("size")) {
                        this.size = Integer.parseInt(c.getValue());
                    }
                }
            }
        } catch(Exception e) {
            Main.log.error("Cookie error: "+e.getMessage());
        }

        String[] parts = target.split("/");
        int group_id = -1;
        if(parts.length > 2)
            group_id = Integer.parseInt(parts[2]);
        this.group = Main.runner.getGroupById(group_id);

        if(this.group != null)
            setTitle("Dashboard: "+this.group.getName());
        else
            setTitle("Dashboard");

        setRefresh(true);
    }

    public void render()
    {
        doPageHeader();

        try {
            int group_id = -1;
            String div_list = "";
            String font_size = "1.0";
            if(size == 1) font_size = "0.8";
            if(size == 3) font_size = "1.8";

            out.println("<div id='message' style='text-align:center; color: red'></div>" +
                    "<table cellpadding='5' align='center' style='font-size:"+font_size+"em'>\n");            

            Vector<Monitor> monitor_collection;

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            int newsize = size == 3 ? 1 : size + 1;
            out.println("<tr><td colspan='4' align='center'>"
                    + "<span style='font-size:80%; color:#777777'>Updated: "+sdf.format(new Date())+"</span>"
                    + "&nbsp;&nbsp;<a href='javascript:window.location.reload()'><img src='/img/reload-22.png'/></a>"
                    + "&nbsp;&nbsp;<a href='javascript:void(0)' onclick='setCookie(\"size\", "+newsize+", false, \"/dash\"); document.location.reload();'><img src='/img/mag-22.png'/></a>" 
                    + "&nbsp;&nbsp;<a href='/dials/"+group_id+"'><img src='/img/backg-22.png'/></a>"
                    + "&nbsp;&nbsp;<a href='/'><img src='/img/home-22.png'/></a>"
                    + "</td></tr>\n");
            
            // at root level so, loop and show groups
            if(group == null) {
                for (Group g : Main.runner.getGroups()) {
                    String link = "<a href='/dash/"+g.getId()+"'>";
                    out.println("<tr><td>"+link+"<img src='/img/"+size+"/"+g.getStatus()+".png'/></a></td><td>"+link+"<b>[" + g.getName() + "]</b></a></td><td>"+link+"&nbsp;</a></td><td>"+link+g.getMessage()+"</a></td></tr>\n");

                    if(Main.config.getBool("web_notifications")) {
                        if(g.getPrevStatus() != g.getStatus() && g.getStatus() < Result.STATUS_GOOD) {
                            out.println("<script>showNotification("+g.getCauser().getLastResult().dt.getTime()/1000+", "+g.getStatus()+", \""+g.getName()+"\", \""+g.getMessage()+"\");</script>");
                        }
                    }
                }
                monitor_collection = Main.runner.getMonitors();
            } else {
                group_id = group.getId();
                monitor_collection = group.getMonitors();
                out.println("<tr><td colspan='4' align='center'><a href='/dash'><img src='/img/undo.png' align='absmiddle'/>&nbsp;&nbsp;<i>Back Up</i></a></td></tr>\n");
                //go_up_link = "<a href='/dash'><img src='/img/undo.png' align='absmiddle'/>&nbsp;&nbsp;<i>Back Up</i></a>";
            }

            
            for (Monitor m : monitor_collection) {
                if(m.getParentGroup() != group) {
                    // this monitor isn't in the group we want - ignore
                    continue;
                }

                int status_code = -1;
                String msg = "";
                String val_str = "";

                Result r = m.getLastResult();
                if(r == null) {
                    val_str = "n/a";
                    msg = "Monitor hasn't run yet";
                } else {
                    status_code = r.status;
                    if(Main.config.getBool("web_notifications")) {
                        if(m.getResults().size() > 1 && status_code < Result.STATUS_GOOD && m.getResults().get(m.getResults().size()-2).status >= Result.STATUS_GOOD) {
                            out.println("<script>showNotification("+r.dt.getTime()/1000+", "+status_code+", \""+m.getName()+"\", \""+r.msg+"\");</script>");
                        }
                    }
                    msg = r.msg;
                    double value = r.value;
                    if(Double.isNaN(value)) {
                        val_str = "n/a";
                    } else {
                        val_str = ""+value+m.getUnits();
                    }
                }

//                String val_link = "<a href='/graph/graph.html?ofc=/chart_data/"+m.getId()+"'>" + val_str + "</a>";
                //onclick='alert(33); setCookie(\"prev_group\", "+group.getId()+", false \"/\")'
                String val_link = "<a onclick='setCookie(\"prev_group\", "+group_id+", false, \"/\")' href='/graph/"+m.getId()+"'>" + val_str + "</a>";

                out.println("<tr><td><a href='javascript:void(0)' onclick='hide_all_popups(); popup_details(event, \""+m.getId()+"\");'>" +
                        "<img src='/img/" +size+ "/" + status_code + ".png'/></a></td>" +
                        "<td><a href='javascript:void(0)' onclick='hide_all_popups(); popup_details(event, \""+m.getId()+"\");'><b>" + m.getName() + "</b></a></td>" +
                        "<td>" + val_link + "</td>" +
                        "<td><a onclick='setCookie(\"prev_group\", "+group_id+", false, \"/\")' href='/history/"+m.getId()+"'>" + msg + "</a></td></tr>\n");

                String prop_str = Utils.flattenProperties(m.getProperties());

                div_list += "\n<div id='"+m.getId()+"' class='mondetails_hidden' onclick='hide_all_popups();'>" +
                        "<h3 style='background-color:"+Result.statusColor(status_code)+"'>&nbsp;"+m.getName()+"</h3>" +
                        "<b>Status: </b>"+Result.statusName(status_code)+"<br>" +
                        "<b>Value: </b>"+val_str+"<br>" +
                        "<b>Message: </b>"+Utils.stringToHTMLString(msg)+"<br>" +
                        "<b>Type: </b>"+m.getType()+"<br>" +
                        "<b>Last Run: </b>"+m.getLastRun()+"<br>" +
                        "<b>Next Run: </b>"+m.getNextRun()+"<br>" +
                        "<b>Properties: </b><span class='prop_list'>"+prop_str+"</span><br>" +
                        "</div>\n";
            }


            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            out.println("\n</table>");

            // write out the popup divs
            out.println(div_list);

            //int newsize = size == 3 ? 1 : size + 1;
            //out.println("<div class='time'>&nbsp;Updated: "+sdf.format(new Date())+" <a href='javascript:window.location.reload()'><img src='/img/reload.png'/></a>" +
            //        " <a href='/'><img src='/img/gohome.png'/></a>" +
            //        " <a href='javascript:void(0)' onclick='setCookie(\"size\", "+newsize+", false, \"/dash\"); document.location.reload();'><img src='/img/mag-16.png'/></a>" +
            //        " <a href='/dials/"+group_id+"'><img src='/img/dial-16.png'/></a>" +
            //        "</div>");

        } catch (Exception e) {
            e.printStackTrace();
            Main.log.error(e);
        }


        doPageFooter();
    }
}
