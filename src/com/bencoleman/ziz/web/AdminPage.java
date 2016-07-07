/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import com.bencoleman.ziz.*;
import com.bencoleman.ziz.utils.Utils;
import java.io.*;
import javax.servlet.http.*;
import java.lang.management.ManagementFactory;
import org.apache.log4j.Level;

/**
 *
 * @author colemanb
 */
public class AdminPage extends Page
{

    public AdminPage(String target, HttpServletRequest request, HttpServletResponse response, boolean secure) throws IOException
    {
        super(target, request, response);

        setTitle("Admin");
        setRefresh(false);

        if(secure) {
            String auth = request.getHeader("Authorization");
            if(auth == null) {
                response.addHeader("WWW-Authenticate", "Basic realm=\"Ziz Admin\"");
                response.sendError(401);
                return;
            } else {
                String decoded = com.bencoleman.ziz.utils.Base64Coder.decodeString(auth.substring(6));
                String[] parts = decoded.split(":");
                String user = Main.config.getStr("web_admin_user");
                String pass = Main.config.getPassword("web_admin_password");
                if(!(parts[0].equals(user) && parts[1].equals(pass))) {
                    response.addHeader("WWW-Authenticate", "Basic realm=\"Ziz Admin\"");
                    response.sendError(401);
                    return;
                }
            }
        }

        String action = request.getParameter("action");

        if(action == null) {
            render();
        } else {
            if(action.equalsIgnoreCase("force")) {
                int mon = Integer.parseInt(request.getParameter("monitor"));
                Main.runner.getMonitorById(mon).forceRun();
                response.sendRedirect("/admin");
            }
            if(action.equalsIgnoreCase("alert")) {
                int mon = Integer.parseInt(request.getParameter("monitor"));
                Main.runner.getMonitorById(mon).forceAlert();
                response.sendRedirect("/admin");
            }
            if(action.equalsIgnoreCase("reload")) {
                Main.runner.readMonitors();
                response.sendRedirect("/admin");
            }
            if(action.equalsIgnoreCase("edit")) {
                new EditorPage(target, request, response).render();
            }
            if(action.startsWith("save")) {
                try {
                    String xml = request.getParameter("xml");
                    FileWriter out = new FileWriter(Main.CONF_DIR+Main.MONITOR_FILE);
                    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<monitors>\n"+xml.trim()+"\n</monitors>");
                    out.flush();
                    out.close();
                    if(action.endsWith("reload")) {
                        Main.runner.readMonitors();
                    }
                    response.sendRedirect("/admin?action=edit");
                } catch (IOException ioe) {
                    Main.log.error("Failed to save monitor config file, monitors.xml - "+ioe.getMessage());
                }
            }
            if(action.equalsIgnoreCase("log")) {
                this.logPageRender();
            }
            if(action.equalsIgnoreCase("setdebug")) {
                Main.log.setLevel(Level.DEBUG);
                response.sendRedirect("/admin?action=log");
            }
        }
    }

    void render()
    {
        doPageHeader();
        
        out.println("<h2>Ziz - Administration</h2>\n");
        out.println("<h3>Admin Actions</h3>\n" +
                "<button type='button' onclick='document.location.href=\"/\"'><img src='/img/undo.png'/> Return</button>" +
                "<button type='button' onclick='warnReload()'><img src='/img/revert.png'/> Reload</button>" +
                "<button type='button' onclick='document.location.href=\"/admin?action=edit\"'><img src='/img/edit.png'/> Edit Monitors</button>" +
                "<button type='button' onclick='document.location.href=\"/admin?action=log\"'><img src='/img/log.png'/> View Log</button>" +
                "<h3>Monitor Actions</h3><table border=0 cellpadding=3 cellspacing=2>\n");
        for(Monitor m : Main.runner.getRootMonitors()) {
            out.println("<tr><td><b>"+m+"</b></td><td><a href='/admin?action=force&monitor="+m.getId()+"'>" +
                    "<img src='/img/reload.png' title='Force monitor to run now'/></a></td><td><a href='/admin?action=alert&monitor="+m.getId()+"'>" +
                    "<img src='/img/alarm.png' title='Force fake alert'/></a></td>" +
                    "<td style='font-size:0.8em'>"+Utils.flattenProperties(m.getProperties())+"</td>" +
                    "</tr>\n");
        }
        for(Group g : Main.runner.getGroups()) {
            out.write("<tr><td colspan='4' style='color:black;background-color:white'><b>["+g.getName()+"]</b></td></tr>\n");
            for(Monitor m : g.getMonitors()) {
                out.println("<tr><td><b>"+m+"</b></td><td><a href='/admin?action=force&monitor="+m.getId()+"'>" +
                        "<img src='/img/reload.png' title='Force monitor to run now...'/></a></td>");
                out.println("<td><a href='/admin?action=alert&monitor="+m.getId()+"'><img src='/img/alarm.png' " +
                        "title='Force fake alert...'/></a></td>" +
                        "<td style='font-size:0.8em'>"+Utils.flattenProperties(m.getProperties())+"</td>" +
                        "</tr>\n");
            }
        }
        out.println("</table><br/><br/><b>Version:</b> "+Main.VERSION+"<br/>");
        out.println("<b>Monitors:</b> "+Main.runner.getMonitors().size()+"<br/>");
        out.println("<b>Groups:</b> "+Main.runner.getGroups().size()+"<br/>");
        int threads = ManagementFactory.getThreadMXBean().getThreadCount();
        long heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 10242;
        long non_heap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed() / 10242;
        out.println("<b>Memory:</b> Heap="+heap+"Mb / Non-Heap="+non_heap+"Mb<br/>");
        out.println("<b>Threads:</b> "+threads+"<br/>");
        
        doPageFooter();
    }

    private void logPageRender()
    {
        doPageHeader();
        
        out.println("<h2>Ziz - System Log</h2><textarea id='log' cols='130' rows='45' wrap='off' style='font-size:0.85em'>");

        try {
            File log = new File("log/system.log");
            long max = 204800;
            long size = log.length();

            BufferedReader br = new BufferedReader(new FileReader("log/system.log"));
            if(size - max > 0) {
                br.skip(size - max);
                while(true) {
                    int charr = br.read();
                    if(charr == 13 || charr == 10) { br.read(); break; }
                }
            }
            String line = br.readLine();
            while(line != null) {
                response.getWriter().println(line);
                line = br.readLine();
            }

        } catch(Exception e) {
            Main.log.error("Error reading log: "+e.getMessage());
        }

        out.println("</textarea>" +
                "<br/><button type='button' onclick='document.location.href=\"/admin\"'><img src='/img/undo.png'/> Return</button>" +
                "<button type='button' onclick='document.location.reload()'><img src='/img/reload-22.png'/> Reload</button>" +
                "<button type='button' onclick='document.location.href=\"/admin?action=setdebug\"'><img src='/img/configure.png'/> Enable Debug</button>" +
                "<script>textareaelem = document.getElementById('log');" +
                "textareaelem.scrollTop = textareaelem.scrollHeight;" +
                "</script>");

        doPageFooter();
    }
}
