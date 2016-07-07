/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.utils;

import java.net.SocketException;
import org.apache.commons.net.telnet.*;
import java.io.*;

/**
 *
 * @author colemanb
 */
public class Telnet
{
    private TelnetClient telnet;
    private InputStream in;
    private PrintStream out;
    private String prompt = "=>";

    public Telnet(String host, int port) throws SocketException, IOException
    {
        telnet = new TelnetClient();

        telnet.connect(host, port);
        in = telnet.getInputStream();
        out = new PrintStream(telnet.getOutputStream());
    }

      public String readUntil(String pattern) throws IOException
      {
         char lastChar = pattern.charAt( pattern.length() - 1 );
         StringBuffer sb = new StringBuffer();
         boolean found = false;
         char ch = ( char )in.read();
         while( true ) {
          //System.out.print( ch );
          sb.append( ch );
          if( ch == lastChar ) {
            if( sb.toString().endsWith( pattern ) ) {
                 return sb.toString();
            }
          }
          ch = ( char )in.read();
         }
    }

    public void write( String value )
    {
        out.println(value);
        out.flush();
    }

    public String sendCommand(String command) throws IOException
    {
        write(command);
        String line = readUntil(prompt);
        return line;
    }

    public void disconnect() throws IOException
    {
        telnet.disconnect();
    }

    public String getPrompt()
    {
        return prompt;
    }

    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public TelnetClient getTelnetClient()
    {
        return telnet;
    }
}
