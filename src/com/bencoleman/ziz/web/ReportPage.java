/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import java.io.File;
import javax.servlet.http.*;

/**
 *
 * @author colemanb
 */
public class ReportPage extends Page
{

    public ReportPage(String target, HttpServletRequest request, HttpServletResponse response)
    {
        super(target, request, response);

        setTitle("Generate Report");
        addHeader("<link href='/css/epoch_styles.css' rel='stylesheet' type='text/css'>");
        addHeader("<script src='/js/epoch_classes.js' type='text/javascript'></script>");
        
        setRefresh(false);
    }

    void render()
    {
        doPageHeader();

        out.println("<script> window.onload = function() { \n" +
                "dp_cal  = new Epoch('epoch_popup','popup',document.getElementById('start_pop'));\n" +
                "dp_cal_2  = new Epoch('epoch_popup','popup',document.getElementById('end_pop'));\n" +
                "d = new Date();" +
                "if(document.getElementById('start_pop').value.length <= 0)" +
                "   document.getElementById('start_pop').value = d.getFullYear()+'-'+(d.getMonth()+1)+'-'+d.getDate();" +
                "if(document.getElementById('end_pop').value.length <= 0)" +
                "   document.getElementById('end_pop').value = d.getFullYear()+'-'+(d.getMonth()+1)+'-'+d.getDate();" +
                "}\n" +
                "validate = function() {\n" +
                "if(document.getElementById('start_pop').value.length <= 0) { alert('Please choose a start date'); return; }\n" +
                "if(document.getElementById('end_pop').value.length <= 0) { alert('Please choose a end date'); return; }\n" +
                "document.forms[0].submit();\n" +
                "}\n" +

                "</script>\n\n");

        String options = "";
        File data_dir = new File("data/");
        try {
            for(File sub_dir : data_dir.listFiles()) {
                if(!sub_dir.isDirectory()) continue;
                options += "<option value='"+sub_dir.getName()+"'>"+sub_dir.getName()+"</option>\n";
            }
        } catch(Exception ioe) {
            System.out.println(ioe.getMessage());
        }

        out.println("<h2>Ziz - Generate Report</h2> " +
                "<form id='placeholder' method='get' action='show_report'>" +
                "<table>\n" +
                "\n<tr><td align='right'><b>Monitor: </td><td><select name='monitor'/>"+options+"</select></td></tr>" +
                "\n<tr><td align='right'><b>Start Date: </td><td><input id='start_pop' type='text' name='start'/>" +
                "&nbsp;<button type='button' onclick='changeDate(\"start_pop\", -1)' style='padding:3px; width:28px; height:28px'><img src='/img/sub.png'/></button>" +
                "<button type='button' onclick='changeDate(\"start_pop\", 1)' style='padding:3px; width:28px;  height:28px'><img src='/img/plus.png'/></button></td></tr>" +
                "\n<tr><td align='right'><b>End Date: </td><td><input id='end_pop' type='text' name='end'/>" +
                "&nbsp;<button type='button' onclick='changeDate(\"end_pop\", -1)' style='padding:3px; width:28px; height:28px'><img src='/img/sub.png'/></button>" +
                "<button type='button' onclick='changeDate(\"end_pop\", 1)' style='padding:3px; width:28px;  height:28px'><img src='/img/plus.png'/></button></td></tr>" +
                "\n<tr><td align='right'><b>Data Points: </td><td><select name='max' value='80'/><option>10</option><option>20</option><option>30</option><option>50</option><option selected='true'>80</option><option>100</option><option>150</option></select></td></tr>" +
                "\n<tr><td align='right'><b>Zero x-axis: </td><td><input type='checkbox' name='zero'></td></tr>" +
                "\n<tr><td align='right'><b>Show Dates: </td><td><input type='checkbox' name='dates'></td></tr>" +
                "\n<tr><td colspan='2' align='center'>" +
                "<button type='button' onclick='document.location.href=\"/\"'><img src='/img/undo.png'/> Return</button>" +
                "<button type='button' onclick='validate()'/><img src='/img/report-22.png'/> Generate</button></td></tr>" +
                "\n</table>" +
                "</form>");

        doPageFooter();
    }

}
