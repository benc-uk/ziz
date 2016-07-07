/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

/**
 *
 * @author colemanb
 */
public class Server
{
    public String name;
    public String hostname;
    public String username;
    public String password;
    public String ssh_keyfile;
    public String ssh_port;

    public Server()
    {
        /*name = "NoName";
        hostname = "";
        username = "";
        password = "";
        ssh_keyfile = "";
        ssh_port = "";*/
    }

    public Server(String n, String h, String u, String p)
    {
        name = n;
        hostname = h;
        username = u;
        password = p;
    }
}
