/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bencoleman.ziz;

import java.util.*;
import com.bencoleman.ziz.utils.*;

/**
 *
 * @author colemanb
 */
public class Config extends Properties
{
    Properties defaults = new Properties();

    public Config()
    {
        super();
    }

    public int getInt(String key)
    {
        try {
            if(!this.containsKey(key)) {
                Main.log.fatal("Configuration error! Property '"+key+"' was not found in config file.");
                return 0;
            }
            return Integer.parseInt(this.getProperty(key));
        } catch(NumberFormatException nfe) {
            Main.log.fatal("Configuration error! Property '"+key+"' is not a valid integer.");
            return 0;
        }
    }

    public boolean getBool(String key)
    {
        try {
            if(!this.containsKey(key)) {
                Main.log.fatal("Configuration error! Property '"+key+"' was not found in config file.");
                return false;
            }
            return Boolean.parseBoolean(this.getProperty(key));
        } catch(Exception nfe) {
            Main.log.fatal("Configuration error! Property '"+key+"' is not a valid boolean. Please use true and false");
            return false;
        }
    }

    public String getStr(String key)
    {
        try {
            if(!this.containsKey(key)) {
                Main.log.fatal("Configuration error! Property '"+key+"' was not found in config file.");
                return "";
            }
            return this.getProperty(key);
        } catch(Exception nfe) {
            Main.log.fatal("Configuration error! Property '"+key+"' is not a valid string.");
            return "";
        }
    }

    public String getPassword(String key)
    {
        try {
            if(!this.containsKey(key)) {
                Main.log.fatal("Configuration error! Property '"+key+"' was not found in config file.");
                return "";
            }
            return PasswordEncoder.decrypt(getProperty(key));
        } catch(Exception nfe) {
            Main.log.fatal("Configuration error! Property '"+key+"' is not a valid password, decryption failed.");
            return "";
        }
    }
}
