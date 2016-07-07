/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import com.bencoleman.ziz.*;
import com.bencoleman.ziz.utils.PasswordEncoder;
import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import jofc2.model.elements.LineChart.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
/**
 *
 * @author Ben Coleman
 */
public class WebServer extends AbstractHandler implements Runnable
{
    private Thread thread;
    private int port;
    private boolean admin_secure = false;
    private WebReport report;

    /**
     *
     * @param listen_port
     */
    public WebServer(int listen_port)
    {
        port = listen_port;
        thread = new Thread(this, "Ziz Webserver");

        admin_secure = Main.config.containsKey("web_admin_user");

        thread.start();
    }

    /**
     *
     */
    public void run()
    {
        try {
            org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);

            ResourceHandler resource_handler = new ResourceHandler();
            resource_handler.setDirectoriesListed(true);
            resource_handler.setWelcomeFiles(new String[]{ "index.html" });
            resource_handler.setResourceBase("web");

            /*HashSessionManager sess_mgr = new HashSessionManager();
            sess_mgr.setStoreDirectory(new File("."));
            sess_mgr.setUsingCookies(true);
            sess_mgr.setSessionCookie("ziz_sess");
            sess_mgr.setSavePeriod(4);
            SessionIdManager id_mgr = new HashSessionIdManager(new Random());
            id_mgr.start();
            sess_mgr.setIdManager(id_mgr);
            sess_mgr.setSessionDomain("localhost");*/

            //SessionHandler sess_hand = new SessionHandler(sess_mgr);
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { this, resource_handler });
            server.setHandler(handlers);
            //HashSessionManager sess_mgr = new HashSessionManager();
            //server.setSessionManager(sess_mgr);

            Main.log.info("Starting webserver, listening on port "+port);
            server.start();
            server.join();
        } catch (Exception ex) {
            ex.printStackTrace();
            Main.log.fatal("Error starting webserver on port "+port+": "+ex+" "+ex.getMessage());
        }
    }

    /**
     *
     * @param target
     * @param base_req
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    public void handle(String target, Request base_req, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        if(target.startsWith("/admin")) {
            base_req.setHandled(true);
            new AdminPage(target, request, response, admin_secure);
            return;
        }

        if(target.startsWith("/encrypt")) {
            base_req.setHandled(true);
            String pass = request.getParameter("password");
            String en_pass = PasswordEncoder.encrypt(pass);
            response.getWriter().println(en_pass);
            return;
        }

        if(target.startsWith("/report")) {
            base_req.setHandled(true);
            new ReportPage(target, request, response).render();
            return;
        }

        if(target.startsWith("/show_report")) {
            base_req.setHandled(true);
            String[] parts = target.split("/");
            String mon_str = request.getParameter("monitor");
            String start = request.getParameter("start");
            String end = request.getParameter("end");
            int max = Integer.parseInt(request.getParameter("max"));
            String zero_check = request.getParameter("zero");
            boolean zero = false;
            if(zero_check != null)
                zero = true;
            String dates_check = request.getParameter("dates");
            boolean dates_on = false;
            if(dates_check != null)
                dates_on = true;

            try {
                report = new WebReport(mon_str, start, end, max, zero, dates_on);
                report.display(response);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        if(target.startsWith("/rep_data_chart")) {
            base_req.setHandled(true);
            response.getWriter().print(report.getDataChart());
            return;
        }

        if(target.startsWith("/rep_status_chart")) {
            base_req.setHandled(true);
            response.getWriter().print(report.getStatusChart());
            return;
        }

        if(target.startsWith("/rep_pie_chart")) {
            base_req.setHandled(true);
            response.getWriter().print(report.getPieChart());
            return;
        }

        if(target.startsWith("/dash")) {
            base_req.setHandled(true);
            new DashboardPage(target, request, response).render();
            return;
        }

        if(target.startsWith("/dial")) {
            base_req.setHandled(true);
            new DialPage(target, request, response).render();
            return;
        }

        if(target.startsWith("/graph")) {
            base_req.setHandled(true);
            String data = request.getParameter("data");
            if(data != null) {
                new GraphPage(target, request, response).renderOFCData();
            } else {
                new GraphPage(target, request, response).render();
            }
            return;
        }

        if(target.startsWith("/history")) {
            base_req.setHandled(true);
            new HistoryPage(target, request, response).render();
            return;
        }

        base_req.setHandled(false);
    }


}
