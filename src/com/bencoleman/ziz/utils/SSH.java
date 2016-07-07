/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.utils;
import com.jcraft.jsch.*;
import java.io.*;

/**
 *
 * @author colemanb
 */
public class SSH
{
    private Session session;
    private JSch jsch;
    private static final int TIMEOUT = 10000;
    private int rc = -1;

    public SSH(String host, int port, String user, String password) throws JSchException
    {
        jsch = new JSch();

        session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect(TIMEOUT);
    }

    public SSH(String host, int port, String user, String password, String keyfile) throws JSchException
    {
        jsch = new JSch();
        jsch.addIdentity(keyfile, password);

        session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(TIMEOUT);
    }

    public String execCommand(String cmd) throws Exception
    {
        String out = "";

        ChannelExec channel = (ChannelExec)session.openChannel("exec");
        channel.setCommand(cmd);
        channel.setInputStream(null);
        channel.setOutputStream(System.out);
        channel.setErrStream(System.err);

        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] tmp = new byte[1024];
        while(true) {
            while(in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if(i < 0) break;
                out += new String(tmp, 0, i);
            }
            if(channel.isClosed()) {
                break;
            }
            try { Thread.sleep(1000); } catch(Exception ee) {}
        }

        rc = channel.getExitStatus();
        channel.disconnect();

        return out;
    }

    public void disconnect()
    {
        session.disconnect();
    }

    public int getExitCode() {
        return rc;
    }
}
