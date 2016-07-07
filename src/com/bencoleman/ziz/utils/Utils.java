/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.utils;

import java.util.*;

/**
 * Static utility methods
 * 
 * @author Ben Coleman
 */
public class Utils
{
    public static String flattenProperties(java.util.Properties p)
    {
	String prop_str = "";
        for (Enumeration e = p.keys(); e.hasMoreElements(); /**/) {
		String p_key = (String) e.nextElement();
		String p_value = p.getProperty(p_key);
                if(p_key.equalsIgnoreCase("password"))
                    p_value = "********";
		prop_str += "<b>"+ p_key + "</b>=" + stringToHTMLString(p_value) + "; ";
	}
        return prop_str;
    }


    public static String stringToHTMLString(String string)
    {
		StringBuffer sb = new StringBuffer(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				//
				// HTML Special Chars
				if (c == '"')
					sb.append("&quot;");
				else if (c == '&')
					sb.append("&amp;");
				else if (c == '<')
					sb.append("&lt;");
				else if (c == '>')
					sb.append("&gt;");
				else if (c == '\n')
					// Handle Newline
					sb.append("&lt;br/&gt;");
				else {
					int ci = 0xffff & c;
					if (ci < 160 )
						// nothing special only 7 Bit
						sb.append(c);
					else {
						// Not 7 Bit use the unicode system
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
						}
					}
				}
			}
		return sb.toString();
    }
}
