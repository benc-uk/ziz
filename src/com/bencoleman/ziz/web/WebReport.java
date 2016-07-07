/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.web;

import com.bencoleman.ziz.*;
import com.bencoleman.ziz.utils.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import jofc2.model.*;
import jofc2.model.axis.*;
import jofc2.model.elements.*;
import jofc2.model.elements.LineChart.*;

/**
 *
 * @author colemanb
 */
public class WebReport
{
    private static final int MAX_ERRORS = 20;
    private ArrayList<DataPoint> raw_data;
    private int max_points = 80;
    private Chart data_chart;
    private Chart status_chart;
    private Chart pie_chart;

    private Monitor monitor;

    private double overall_max;
    private double overall_min;
    private double overall_avg;

    private String start_str;
    private String end_str;

    ArrayList<String> errors = new ArrayList(MAX_ERRORS);
    ArrayList<String> warnings = new ArrayList(MAX_ERRORS);

    public WebReport(String mon_str, String start, String end, int max_dp, boolean force_zero, boolean dates_on)
    {
        this.max_points = max_dp;
        
        this.overall_max = Double.NEGATIVE_INFINITY;
        this.overall_min = Double.POSITIVE_INFINITY;
        this.overall_avg = 0.0;
        this.start_str = start;
        this.end_str = end;

        // find the live monitor based on the name and group passed in
        String[] parts = mon_str.split(" \\[");
        Group grp = null;
        if(parts.length > 1) {
            String gn = parts[1].substring(0, parts[1].length()-1);
            grp = new Group(gn);
            this.monitor = Main.runner.findMonitor(parts[0].trim(), gn);
        } else {
            this.monitor = Main.runner.findMonitor(parts[0].trim(), null);
        }
        
        // no live monitor, so create a temp dummy one
        if(this.monitor == null) {
            this.monitor = new Monitor("Monitor no longer active", parts[0].trim(), 1, new Properties(), grp, -1);
        }

        raw_data = new ArrayList<DataPoint>(10000);
        data_chart = new Chart();
        data_chart.setBackgroundColour("#ffffff");
        status_chart = new Chart();
        status_chart.setBackgroundColour("#ffffff");
        pie_chart = new Chart();
        pie_chart.setBackgroundColour("#ffffff");

        LineChart line_chart = new LineChart();
        line_chart.setColour("#114de8");
        line_chart.setWidth(3);
        data_chart.setTitle(new Text(monitor.getName()+" - monitor values graph"));
        data_chart.addElements(line_chart);

        StackedBarChart bar_chart = new StackedBarChart();
        status_chart.setTitle(new Text(monitor.getName()+" - monitor status chart"));
        status_chart.addElements(bar_chart);
        bar_chart.setAlpha(0.75f);
        
        File data_dir = new File("data/"+monitor.getNameAndGroup());
        
        DecimalFormat format_value = new DecimalFormat("###,###.##");
        DecimalFormat format_percent = new DecimalFormat("###.##");

        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(start.split("-")[0]), Integer.parseInt(start.split("-")[1])-1, Integer.parseInt(start.split("-")[2]));
        Calendar end_cal = Calendar.getInstance();
        end_cal.set(Integer.parseInt(end.split("-")[0]), Integer.parseInt(end.split("-")[1])-1, Integer.parseInt(end.split("-")[2]));
        // set the end hour to 11pm so that the compare works
        end_cal.set(Calendar.HOUR_OF_DAY, 23);
        
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");

        // loop through each data file, incrementing the date as we go
        while(cal.before(end_cal)) {
            File file = new File(data_dir, date_format.format(cal.getTime())+".dat");

            try {
                //System.out.println("file: "+file);
                // load raw data from files into vector
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String data_line = reader.readLine();
                while (data_line != null) {
                    DataPoint dp = new DataPoint(data_line);
                    raw_data.add(dp);
                    data_line = reader.readLine();
                }
                reader.close();

            } catch (Exception ex) {
                Main.log.debug("Tried to read input data file "+file+" but it wasn't found, this is not a problem");
            }
            cal.add(Calendar.DATE, 1);
        }

        // work out our step size
        int step = (int)(raw_data.size() / max_points);
        if(step <= 0) step = 1;

        // sort out some chart stuff - data chart
        XAxis xa = new XAxis();
        xa.setGridColour("#ffffff");
        xa.setColour("#000000");
        XAxisLabels xa_labels = new XAxisLabels();
        xa.setXAxisLabels(xa_labels);
        if(!dates_on) {
            xa_labels.setColour("#ffffff");
        }  else {
            xa_labels.setRotation(Label.Rotation.DIAGONAL);
        }
        data_chart.setXAxis(xa);

        // sort out some chart stuff - status chart
        XAxis xa_s = new XAxis();
        xa_s.setGridColour("#ffffff");
        xa_s.setColour("#000000");
        XAxisLabels xa_labels_s = new XAxisLabels();
        xa_s.setXAxisLabels(xa_labels_s);
        if(!dates_on) {
            xa_labels_s.setColour("#ffffff");
        } else {
            xa_labels_s.setRotation(Label.Rotation.DIAGONAL);
        }
        status_chart.setXAxis(xa_s);

        // buckets to count the totals of each status
        int total_good = 0;
        int total_warn = 0;
        int total_error = 0;
        int total_failed = 0;
        boolean flip = false;

        // RIGHT! this is the main loop, through the raw data
        //  - increments in "step" sized jumps
        for(int raw_row = 0; raw_row < raw_data.size(); raw_row += step) {

            // output values for this step
            double value = 0;
            // get the raw datapoint
            DataPoint raw_dp = raw_data.get(raw_row);
            // date + time string for labels and tooltips
            String label = raw_dp.date+" "+raw_dp.time.substring(0, 5);

            // sub-row totals and counters
            double[] values_for_avg = new double[step];
            int[] statuses_for_avg = new int[step];
            int real_step = step;
            int good_count = 0;
            int error_count = 0;
            int warn_count = 0;
            int fail_count = 0;

            // OK this is the sub-loop, this works out the average of all the raw data for the sub-step
            // it also counts up the number of each status
            for(int sub_row = 0; sub_row < step; sub_row++) {
                // hit end of raw-data, break out and set real_step to as far as we got through this sub-loop
                if(raw_row + sub_row >= raw_data.size()) {
                   real_step = sub_row;
                    break;
                }

                // grab the raw value and push into array for later avg calc
                double raw_value = raw_data.get(raw_row + sub_row).value;
                values_for_avg[sub_row] = raw_value;

                // grab the raw status value
                int raw_status = raw_data.get(raw_row + sub_row).status;
                // based on the status code, we increment the various buckets and push the errors/warnings into a list for later display
                switch(raw_status) {
                    case Result.STATUS_GOOD: good_count++; break;
                    case Result.STATUS_WARN: 
                        warn_count++;
                        addErrorOrWarn(warnings, "<tr><td>"+label+"</td><td>"+raw_value+"</td><td style='font-size:0.8em'>"+raw_data.get(raw_row + sub_row).msg+"</td></tr>\n");
                        break;
                    case Result.STATUS_ERROR: 
                        error_count++;
                        addErrorOrWarn(errors, "<tr><td>"+label+"</td><td>"+raw_value+"</td><td style='font-size:0.8em'>"+raw_data.get(raw_row + sub_row).msg+"</td></tr>\n");
                        break;
                    case Result.STATUS_FAILED: 
                        fail_count++;
                        addErrorOrWarn(errors, "<tr><td>"+label+"</td><td>"+raw_value+"</td><td style='font-size:0.8em'>"+raw_data.get(raw_row + sub_row).msg+"</td></tr>\n");
                        break;
                }

                // overall min & max calcuations
                if(raw_value > overall_max)
                    overall_max = raw_value;
                if(raw_value < overall_min)
                    overall_min = raw_value;
                // overall avg calc (treat NaN as zero)
                overall_avg += Double.isNaN(raw_value) ? 0.0 : raw_value;
            }

            // OK done with out sub-loop, we can calc our average for the value
            value = calcAvgValue(values_for_avg, real_step);
            //status = calcAvgStatus(statuses_for_avg, real_step);

            // format the tooltip for our chart point
            String tooltip = ""+format_value.format(value)+monitor.getUnits()+"\n"+label;
            if(Double.isNaN(value)) {
                value = 0.0;
                tooltip = "No values\n"+label;
            }

            // add the point (dot) to the values line chart
            Dot dot = new Dot(value, "#000088", 3, 0);
            dot.setTooltip(tooltip);
            line_chart.addDots(dot);

            // if we're showing the date labels add them to the x-axis
            // flip setting means we only display every other label, prevents crowding and helps readability
            if(dates_on) {
                String axis_label = flip ? "" : label;
                xa.addLabels(axis_label);
                xa_s.addLabels(axis_label);
                flip = !flip;
            }

            // the overall totals get incremented with the sub-totals
            total_good += good_count;
            total_warn += warn_count;
            total_error += error_count;
            total_failed += fail_count;

            // calculate the percentage of each status bucket
            double good_percent = ((double)good_count / (double)real_step) * 100.0;
            double warn_percent = ((double)warn_count / (double)real_step) * 100.0;
            double error_percent = ((double)error_count / (double)real_step) * 100.0;
            double fail_percent = ((double)fail_count / (double)real_step) * 100.0;

            // add status percentages to the bar chart stack
            StackedBarChart.Stack stack = new StackedBarChart.Stack();
            if(good_count > 0)
                stack.addStackValues(new StackedBarChart.StackValue(good_percent, "#4EDB39", format_percent.format(good_percent)+"% Good\n"+label));
            if(warn_count > 0)
                stack.addStackValues(new StackedBarChart.StackValue(warn_percent, "#FFC235", format_percent.format(warn_percent)+"% Warning\n"+label));
            if(error_count > 0)
                stack.addStackValues(new StackedBarChart.StackValue(error_percent, "#EA1717", format_percent.format(error_percent)+"% Error\n"+label));
            if(fail_count > 0)
                stack.addStackValues(new StackedBarChart.StackValue(fail_percent, "#131FD3", format_percent.format(fail_percent)+"% Failed\n"+label));

            // add the stack to the chart
            bar_chart.addStack(stack);
        } //END - raw data loop

        // super overall average
        overall_avg = overall_avg / (double)raw_data.size();
        
        PieChart pie = new PieChart();
        pie_chart.addElements(pie);
        pie_chart.setTitle(new Text(monitor.getName()+" status summary"));
        pie.setAlpha(0.7f);
        pie.setBorder(1);
        double total_good_p = (double)total_good / (double)raw_data.size() * 100.0;
        double total_warn_p = (double)total_warn / (double)raw_data.size() * 100.0;
        double total_error_p = (double)total_error / (double)raw_data.size() * 100.0;
        double total_fail_p = (double)total_failed / (double)raw_data.size() * 100.0;
        pie.setColours("#4EDB39", "#FFC235", "#EA1717", "#131FD3");
        pie.setNoLabels(true);
        pie.setGradientFill(true);
        pie.setAnimate(true);
        pie.addAnimations(new PieChart.AnimationPie.Bounce(10));
        PieChart.Slice slice = new PieChart.Slice(total_good_p, "Good");
        slice.setOnMouseOverBreakout();
        slice.setTip(format_percent.format(total_good_p)+"% Good");
        pie.addSlices(slice);
        slice = new PieChart.Slice(total_warn_p, "Warning");
        slice.setTip(format_percent.format(total_warn_p)+"% Warning");
        pie.addSlices(slice);
        slice = new PieChart.Slice(total_error_p, "Error");
        slice.setTip(format_percent.format(total_error_p)+"% Error");
        pie.addSlices(slice);
        slice = new PieChart.Slice(total_fail_p, "Fail");
        slice.setTip(format_percent.format(total_fail_p)+"% Fail");
        pie.addSlices(slice);

        data_chart.computeYAxisRange(10);
        YAxis ya = data_chart.getYAxis();
        ya.setColour("#000000");
        ya.setGridColour("#e8e8e8");
        data_chart.setYLegend(new Text(monitor.getUnits(), Text.TEXT_ALIGN_CENTER));
        if(force_zero)
            ya.setMin(0.0);

        YAxis ya_s = new YAxis();
        ya_s.setMax(100);
        ya_s.setSteps(10);
        ya_s.setGridColour("#ffffff");
        ya_s.setColour("#000000");
        status_chart.setYAxis(ya_s);
        status_chart.setYLegend(new Text("% Precent", Text.TEXT_ALIGN_CENTER));
    }

    public Chart getDataChart() {
        return data_chart;
    }

    public Chart getStatusChart() {
        return status_chart;
    }

    public Chart getPieChart() {
        return pie_chart;
    }

    public void display(HttpServletResponse response)
    {
        if(raw_data.size() <= 0) {
            try {
                response.getWriter().println("<html><body style='background-color:#232323;color:white;font-family:Arial'><h2>" +
                        "No data found for selected monitor and date range<br/><br/><a href='javascript:history.back()'>Please try again</a></h2></body></html>");
            } catch (IOException ex) {;
            }
            return;
        }

        try {
            DecimalFormat format_value = new DecimalFormat("###,###.##");
            PrintWriter writer = response.getWriter();
            String no_flash = "You need Adobe Flash to display this graph, please <a href='http://www.macromedia.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash'>click here to download it</a>";

            String units = monitor.getUnits();
            String title = monitor.getName();
            if(monitor.getParentGroup() != null)
                title += " ["+monitor.getParentGroup().getName()+"]";

            String errs = "";
            for(int i = errors.size()-1; i >= 0; i--) {
                errs += errors.get(i);
            }

            String warns = "";
            for(int i = warnings.size()-1; i >= 0; i--) {
                warns += warnings.get(i);
            }

            String out = "<html> <head> " +
                    "<!--link href='/css/main.css' rel='stylesheet' type='text/css'-->" +
                    "<script src='/js/swfobject.js' language='javascript' type='text/javascript'></script>" +
                    "<script src='/js/extra.js' language='javascript' type='text/javascript'></script>" +
                    "<title>Monitor Report - "+title+"</title>" +
                    "</head> " +
                    "<body style='font-family:Arial'>" +
                    "<div style='float:left'><a href='/reports' style='text-decoration:none'>&laquo; Go Back</a></div>" +
                    "<a style='float:right' href='#' onclick='OFC.none.rasterize(\"data_chart\", \"data_chart\"); OFC.none.rasterize(\"status_chart\", \"status_chart\"); OFC.none.rasterize(\"pie_chart\", \"pie_chart\"); window.print();'><img src='/img/print.png'/></a>" +
                    "<h2 align='center'>Ziz Report - "+title+"</h2>" +
                    "" +
                    "<table align='center' border=0>" +
                    "<tr><td align='center' colspan=2><div id='data_chart'>"+no_flash+"</div></td></tr>" +
                    "<tr><td align='center' colspan=2><div id='status_chart'>"+no_flash+"</div></td></tr>" +
                    "<tr><td width='310px'><div id='pie_chart'>"+no_flash+"</div></td>" +
                    "<td valign='top'>" +
                    "<table style='margin-top:20px;margin-left:30px'>" +
                    "<tr><td><b>Date Range:</b></td><td>"+start_str+" ~ "+end_str+"</td></tr>" +
                    "<tr><td><b>Monitor Runs:</b></td><td>"+raw_data.size()+" total runs</td></tr>" +
                    "<tr><td><b>Maximum:</b></td><td>"+overall_max+units+"</td></tr>" +
                    "<tr><td><b>Average:</b></td><td>"+format_value.format(overall_avg)+units+"</td></tr>" +
                    "<tr><td><b>Minimum:</b></td><td>"+overall_min+units+"</td></tr>" +
                    "<tr><td><br/><b>Type:</b></td><td><br/>"+monitor.getType()+"</td></tr>" +
                    "<tr><td><b>Interval:</b></td><td>"+monitor.getIntervalMins()+" minutes</td></tr>" +
                    "<tr><td><b>Group:</b></td><td>"+monitor.getParentGroup()+"</td></tr>" +
                    "<tr><td><b>Properties:</b></td><td>"+
                    Utils.flattenProperties(monitor.getProperties())+"</td></tr>" +
                    "</table></td></tr>" +
                    "<tr><td colspan=2><h3 style='border-bottom:2px solid black'>Last 20 Errors</h3><table><tr><th width='180px' >Date/Time</th><th width='80px' align='center'>Value</th><th>Error Message</th></tr>"+errs+"</table></td></tr>" +
                    "<tr><td colspan=2><br/><h3 style='border-bottom:2px solid black'>Last 20 Warnings</h3><table><tr><th width='180px' >Date/Time</th><th width='80px' align='center'>Value</th><th>Error Message</th></tr>"+warns+"</table></td></tr>" +
                    "</table>" +
                    "<script type='text/javascript'>" +
                    "swfobject.embedSWF('/swf/open-flash-chart.swf', 'data_chart', '900', '400', " +
                    "'9.0.0', '/swf/expressInstall.swf', {'data-file':'/rep_data_chart','loading':'Please wait while the report is generated...'});" +
                    "swfobject.embedSWF('/swf/open-flash-chart.swf', 'status_chart', '900', '200', " +
                    "'9.0.0', '/swf/expressInstall.swf', {'data-file':'/rep_status_chart','loading':'Please wait while the report is generated...'});" +
                    "swfobject.embedSWF('/swf/open-flash-chart.swf', 'pie_chart', '300', '300', " +
                    "'9.0.0', '/swf/expressInstall.swf', {'data-file':'/rep_pie_chart','loading':'Please wait while the report is generated...'});" +
                    "</script>" +
                    "</body>" +
                    "</html>";
            
            writer.println(out);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private static double calcAvgValue(double[] vals, int size)
    {
        //System.out.println("here "+vals);
        double sum = 0;
        for(int counter = 0; counter < size; counter++) {
            if(Double.isNaN(vals[counter]))
                continue;
            sum = sum + vals[counter];
        }
        return sum / size;
    }

    private void addErrorOrWarn(ArrayList<String> list, String e)
    {
        list.add(e);
        if(list.size() > MAX_ERRORS) {
            list.remove(list.get(0));
        }
    }
    
    class DataPoint
    {
        String date;
        String time;
        int status;
        double value;
        String msg;

        public DataPoint(String d, String t, int s, double v, String m)
        {
            date = d;
            time = t;
            status = s;
            value = v;
            msg = m;
        }

        public DataPoint(String line)
        {
            String[] data = line.split("\t");
            date = data[0];
            time = data[1];
            status = Integer.parseInt(data[2]);
            value = Double.parseDouble(data[3]);
            msg = data[4];
        }

    }
}
