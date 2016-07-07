/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bencoleman.ziz;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author colemanb
 */
public class DataLogger
{
    private boolean enabled;
    public static final String DATA_PATH = "data/";
    private FileWriter out_file;
    private Calendar file_cal;
    private SimpleDateFormat log_date_format;
    private SimpleDateFormat file_date_format;
    private String name;

    public DataLogger(String name)
    {
        this.name = name;
        log_date_format = new SimpleDateFormat("dd-MM-yyyy\tHH:mm:ss");
        file_date_format = new SimpleDateFormat("yyyy-MM-dd");
        enabled = Main.config.getBool("sys_store_data");
        if(enabled)
            newLog();
    }

    public void logData(Result res)
    {
        try {
            Date now = new Date();
            Calendar now_cal = Calendar.getInstance();
            if(now_cal.get(Calendar.DATE) != file_cal.get(Calendar.DATE)) {
                Main.log.info("Rolling data log for "+name+" to a new day, and a new file");
                out_file.flush();
                out_file.close();
                newLog();
            }

            out_file.write(log_date_format.format(now)+"\t"+res.status+"\t"+res.value+"\t"+res.msg+"\n");
            out_file.flush();
        } catch (IOException ex) {
            Main.log.error("Error logging result! "+ex.getMessage());
        }
    }

    private void newLog()
    {
        try {
            file_cal = Calendar.getInstance();
            File path = new File(DATA_PATH  + name + "/");
            path.mkdirs();
            out_file = new FileWriter(DATA_PATH  + name + "/" + file_date_format.format(file_cal.getTime())+".dat", true);
        } catch (IOException ex) {
            Main.log.fatal("Error creating data log "+ex.getMessage());
        }
    }
}
