/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import com.bencoleman.ziz.*;
import java.io.*;
import java.util.ArrayList;
import javax.servlet.http.*;

/**
 *
 * @author colemanb
 */
public class EditorPage extends Page
{
    public EditorPage(String target, HttpServletRequest request, HttpServletResponse response)
    {
        super(target, request, response);

        setTitle("Config Editor");
        addHeader("<script src='/js/codemirror/codemirror.js' type='text/javascript'></script>");
        addHeader("<script src='/js/editor.js' type='text/javascript'></script>");
        setRefresh(false);
    }

    void render()
    {
        doPageHeader();

        String msg = "";
        for(String m : Main.runner.getMessages()) {
            msg += m + "<br/>";
        }
        if(Main.runner.getMessages().size() > 0) {
            msg += "<b>ERRORS IN CONFIGURATION!</b>";
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader(Main.CONF_DIR+Main.MONITOR_FILE));
            String str;
            String mon_conf_data = "";
            while ((str = in.readLine()) != null) {
                if(str.trim().equalsIgnoreCase("<monitors>")) continue;
                if(str.trim().equalsIgnoreCase("</monitors>")) continue;
                if(str.trim().equalsIgnoreCase("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) continue;

                mon_conf_data += str + "\n";
            }
            out.println("<h2 style='margin-top:0px'>Ziz - Monitor Config Editor</h2>" +
                    "<div style='color:red'>"+msg+"</div>\n" +
                    "<button type='button' onclick='insertGroupShow()'><img src='/img/folder.png'/> Group</button>" +
                    "<button type='button' onclick='insertMonitorShow()'><img src='/img/mon.png'/> Monitor</button>" +
                    "<button type='button' onclick='insertServerShow()'><img src='/img/pda.png'/> Server</button>" +
                    "<button type='button' onclick='insertPropShow()'><img src='/img/prop.png'/> Property</button>" +
                    "<textarea id='code' cols='90' rows='60'>"+mon_conf_data+"</textarea>\n\n" +
                    "<form action='/admin' method='POST' onsubmit='return false;'>" +
                    "");
            out.println("<br/><button type='button' onclick='document.location.href=\"/admin\"'><img src='/img/button_cancel.png'/> Cancel</button>");
            out.println("<button type='button' onclick='save()'><img src='/img/save.png'/> Save</button>");
            out.println("<button type='button' onclick='saveAndReload()'><img src='/img/save_reload.png'/> Save &amp Reload</button>");
            out.println("<input type='hidden' name='action' value=''/>");
            out.println("<input type='hidden' name='xml' value=''/>");
            out.println("<form/>\n");
            out.println("<script type='text/javascript'>" +
                    "  var editor = CodeMirror.fromTextArea('code', {" +
                    "    height: '500px', parserfile: 'parsexml.js', stylesheet: '/css/codemirror/xmlcolors.css', path: '/js/codemirror/', indentUnit:3" +
                    "  });" +
                    "</script>\n\n");
            out.println("<div id='group_div' class='dialog_hidden'>" +
                    "Name: <input type='text' id='group_name' size='20'/><br/><br/>" +
                    "<button type='button' onclick='insertGroup()' style=''>Insert Group</button>" +
                    "<button type='button' onclick='insertGroupShow()' style=''>Cancel</button>" +
                    "</div>\n\n");
            out.println("<div id='mon_div' class='dialog_hidden'>" +
                    "Name: <input type='text' id='mon_name' style='width:170px'/><br/>" +
                    "Type: <select id='mon_type' style='width:170px'>");
            for(File f : new File(Monitor.MON_PATH).listFiles()) {
                if(f.getName().toLowerCase().endsWith(".js")) {
                     out.println("<option>"+(f.getName().substring(0, f.getName().length()-3))+"</option>");
                }
            }
            out.println("</select><br/>");
            out.println("Interval: <select id='mon_int' style='width:170px'>" +
                    "<option value='1'>1 minute</option>" +
                    "<option value='5'>5 minutes</option>" +
                    "<option value='10'>10 minutes</option>" +
                    "<option value='15'>15 minutes</option>" +
                    "<option value='30'>30 minutes</option>" +
                    "<option value='45'>45 minutes</option>" +
                    "<option value='60'>1 hour</option>" +
                    "</select>");
            out.println("<button type='button' onclick='insertMonitor()' style=''>Insert Monitor</button>" +
                    "<button type='button' onclick='insertMonitorShow()' style=''>Cancel</button>" +
                    "</div>\n\n");

            out.println("<div id='prop_div' class='dialog_hidden'>" +
                    "Name: <input type='text' id='prop_name' size='20'/><br/>" +
                    "Value: <input type='text' id='prop_value' size='20'/><br/><br/>" +
                    "<button type='button' onclick='insertProp()' style=''>Insert Property</button>" +
                    "<button type='button' onclick='insertPropShow()' style=''>Cancel</button>" +
                    "</div>\n\n");

            out.println("<div id='server_div' class='dialog_hidden'>" +
                    "Name: <input type='text' id='server_name' size='20'/><br/>" +
                    "Hostname: <input type='text' id='server_host' size='20'/><br/>" +
                    "Username: <input type='text' id='server_user' size='20'/><br/>" +
                    "Password: <input type='password' id='server_pass' size='20'/><br/><br/>" +
                    "<button type='button' onclick='insertServer()' style=''>Insert Server</button>" +
                    "<button type='button' onclick='insertServerShow()' style=''>Cancel</button>" +
                    "");

        } catch(IOException ioe) {

        }

        doPageFooter();
    }
}
