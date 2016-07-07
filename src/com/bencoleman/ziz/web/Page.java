/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import com.bencoleman.ziz.Main;
import java.io.*;
import java.util.ArrayList;
import javax.servlet.http.*;

/**
 *
 * @author Ben Coleman
 */
abstract public class Page
{
    protected String target;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected PrintWriter out;
    protected String title;
    protected boolean refresh;
    protected ArrayList<String> headers;

    public Page(String target, HttpServletRequest request, HttpServletResponse response)
    {
        this.target = target;
        this.request = request;
        this.response = response;

        try {
            out = response.getWriter();
        } catch (IOException ex) {
            Main.log.error(ex);
        }

        this.headers = new ArrayList<String>(5);
    }

    protected void doPageHeader()
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        String refresh_header = "";
        if(this.refresh) {
            int ref_interval = Main.config.getInt("dash_refresh");
            refresh_header = "<meta http-equiv='refresh' content='"+ref_interval+"'/>\n";
        }

        String head_extra = "";
        for(String head : this.headers) { head_extra += head+"\n"; }
        out.println("<html>\n<head>\n<title>Ziz: "+title+"</title>\n" +
                refresh_header +
                "<link href='/css/main.css' rel='stylesheet' type='text/css'>\n" +
                "<script src='/js/main.js' type='text/javascript'></script>\n" +
                head_extra +
                "</head>\n<body>\n\n");
    }

    protected void doPageFooter()
    {
        out.println("\n\n</body>\n</html>");
    }

    abstract void render();

    protected void setTitle(String t)
    {
        this.title = t;
    }

    public void addHeader(String header)
    {
        this.headers.add(header);
    }

    public void setRefresh(boolean refresh)
    {
        this.refresh = refresh;
    }

}
