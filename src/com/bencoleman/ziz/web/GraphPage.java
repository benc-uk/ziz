/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import com.bencoleman.ziz.*;
import java.text.*;
import javax.servlet.http.*;
import jofc2.model.*;
import jofc2.model.axis.*;
import jofc2.model.elements.*;
import jofc2.model.elements.LineChart.*;

/**
 *
 * @author colemanb
 */
public class GraphPage extends Page
{
    private Monitor monitor;
    private int prev_group_id = -1;
    
    public GraphPage(String target, HttpServletRequest request, HttpServletResponse response)
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
        
        setTitle(this.monitor.getName()+" Data Chart");
        //addHeader("<script src='/js/jquery-1.4.2.js' language='javascript' type='text/javascript'></script>");
        addHeader("<script src='/js/swfobject.js' language='javascript' type='text/javascript'></script>");
        addHeader("<script src='/js/extra.js' language='javascript' type='text/javascript'></script>");
        setRefresh(true);
    }

    void render()
    {
       try {
            doPageHeader();
       
            String back_link = "/dash";
            if(prev_group_id != -1)
                back_link = "/dash/"+prev_group_id;

            out.println("<p align='center'><a href='"+back_link+"' style='color:grey'>" +
                    "<img src='/img/undo-16.png'> Return to the Dashboard</a>&nbsp;&nbsp;&nbsp;&nbsp;" +
                    "<a style='float:right' href='#' onclick='OFC.none.rasterize(\"monitor_chart\", \"monitor_chart\"); window.print();'><img src='/img/print.png'/></a><br/>");
            out.println("<table>" +
                    "<tr><td align='center'><div id='monitor_chart'>" +
                    "You need Adobe Flash to display this graph, please " +
                    "<a href='http://www.macromedia.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash'>" +
                    "click here to download it</a>" +
                    "</div></td></tr></table>");
            out.println("<script type='text/javascript'>" +
                    "var wid = document.body.clientWidth-30;" +
                    "swfobject.embedSWF('/swf/open-flash-chart.swf', 'monitor_chart', wid, '320', '9.0.0', " +
                    "'/swf/expressInstall.swf', {'data-file':'/graph/"+monitor.getId()+"?data'});" +
                    "</script>" +
                    "</p>");

            doPageFooter();
        } catch (Exception ex) {
            ex.printStackTrace();
            Main.log.error("Error rendering graph "+ex);
        }        
    }

    void renderOFCData()
    {
       try {
            //Monitor m = Main.runner.getMonitorById(mon_id);
            String unit = monitor.getUnits();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            Chart chart = new Chart(monitor.getName());
            chart.setBackgroundColour("#eeeeee");
            LineChart line_chart = new LineChart();
            line_chart.setColour("#176c1e");
            line_chart.setWidth(3);
            XAxis xa = new XAxis();
            xa.setGridColour("#e8e8e8");
            xa.setColour("#0000aa");

            XAxisLabels xal = new XAxisLabels();
            xal.setRotation(Label.Rotation.VERTICAL);
            xal.setSteps(2);
            xa.setXAxisLabels(xal);

            if(monitor.getResults().size() <= 0) {
                chart.setTitle(new Text("No result for monitor"));
                xa.addLabels("");
            }

            for(Result r : monitor.getResults()) {
                double val = r.value;
                String tooltip = ""+val;
                if(Double.isNaN(r.value)) {
                    val = 0.0;
                    tooltip = r.msg;
                } else {
                    tooltip = val + " " + unit;
                }

                Dot dot = new Dot(val, r.statusColour(), 4, 1);

                dot.setTooltip(tooltip);
                line_chart.addDots(dot);
                xa.addLabels(sdf.format(r.dt));
            }

            chart.addElements(line_chart);
            chart.computeYAxisRange(10);
            chart.getYAxis().setMin(0);
                   
            // this insane gobbledy-gook calculates the best max & and step for the y-axis
            double max_a = monitor.getResultsMax();
            if(max_a == 0.0) max_a = 1.0;
            int step = (int)Math.round((max_a / 100) * 10);
            int rounding_size = 10;
            if(max_a > 1000)
                rounding_size = 100;
            if(max_a > 10000)
                rounding_size = 1000;
            step += (rounding_size / 2);
            step /= rounding_size;
            step *= rounding_size;
 
            chart.getYAxis().setMax(max_a);
            chart.getYAxis().setSteps(step);
            chart.getYAxis().setColour("#0000aa");
            chart.getYAxis().setGridColour("#e8e8e8");
            chart.setXAxis(xa);
            if(unit != null)
                chart.setYLegend(new Text(unit, Text.createStyle(11)));

            //phew! output the chart
            response.getWriter().print(chart);
                   
        } catch (Exception ex) {
            ex.printStackTrace();
            Main.log.error("Error rendering graph "+ex);
        }
    }
}
