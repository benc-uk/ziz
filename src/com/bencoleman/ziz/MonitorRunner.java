/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.xpath.*;

/**
 *
 * @author colemanb
 */
public class MonitorRunner implements Runnable
{
    private Thread thread;
    private final int WAKE_INTERVAL = 1000;
    //private final int MONITOR_READ_INTERVAL = 30;
    private boolean running = true;
    private Vector<Monitor> monitors;
    private Vector<Group> groups;
    private Vector<Include> includes;
    private long conf_touch_time = -1;

    private ArrayList<String> messages;

    public Vector<Monitor> getMonitors() 
    {
        return monitors;
    }

    public Vector<Group> getGroups() {
        return groups;
    }

    public Monitor getMonitorById(int id)
    {
        for(Monitor m : monitors) {
            if(m.getId() == id) return m;
        }
        return null;
    }

    public Group getGroupById(int id)
    {
        for(Group g : groups) {
            if(g.getId() == id) return g;
        }
        return null;
    }

    public Include getIncludeByName(String name)
    {
        for(Include inc : includes) {
            if(name.equals(inc.getName())) return inc;
        }
        return null;
    }

    public void addMonitor(Monitor monitor) {
        this.monitors.add(monitor);
    }

    public MonitorRunner()
    {
        readMonitors();
        thread = new Thread(this, "Monitor Dispatcher");
        thread.start();
    }

    public void run()
    {
        int ticks = Main.config.getInt("sys_update_check");
        while(running) {
            try {
                thread.sleep(WAKE_INTERVAL);

                ticks--;
                if(ticks <= 0) {
                    ticks = Main.config.getInt("sys_update_check");
                    readMonitors();
                }

                for(Monitor m : monitors) {
                    m.tick(WAKE_INTERVAL);
                    if(m.getTicks() <= 0) {
                        m.resetTicks();
                        Main.log.debug("Running monitor: "+m);
                        m.startMonitor();
                    }

                }
            } catch(InterruptedException ie) {
                Main.log.warn("Monitor-Runner thread woken for shutdown");
            }
        }
    }

    public void readMonitors()
    {
        Main.log.info("Checking monitors.xml config file for changes");

        messages = new ArrayList<String>(10);

        File conf_file = new File(Main.CONF_DIR+"monitors.xml");
        long new_touch_time = conf_file.lastModified();
        if(new_touch_time > conf_touch_time) {
            
            monitors = new Vector<Monitor>(50);
            groups = new Vector<Group>(10);
            includes = new Vector<Include>(10);

            Main.log.info("monitors.xml has been updated, reloading all monitors");
            conf_touch_time = new_touch_time;

            try {
                // Read includes
                InputSource xml_input = new InputSource(new FileReader(Main.CONF_DIR+Main.MONITOR_FILE));
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList inc_nodes = (NodeList) xpath.evaluate("/monitors/include", xml_input, XPathConstants.NODESET);
                for(int n = 0; n < inc_nodes.getLength(); n++) {
                    createInclude(inc_nodes.item(n), xpath);
                }

                // Read monitors at root level
                xml_input = new InputSource(new FileReader(Main.CONF_DIR+Main.MONITOR_FILE));
                xpath = XPathFactory.newInstance().newXPath();
                NodeList monitor_nodes = (NodeList) xpath.evaluate("/monitors/monitor", xml_input, XPathConstants.NODESET);
                for(int n = 0; n < monitor_nodes.getLength(); n++) {
                    createMonitor(monitor_nodes.item(n), xpath, null);
                }

                // Read monitors in groups
                xml_input = new InputSource(new FileReader(Main.CONF_DIR+Main.MONITOR_FILE));
                NodeList group_nodes = (NodeList) xpath.evaluate("//group", xml_input, XPathConstants.NODESET);
                for(int n = 0; n < group_nodes.getLength(); n++) {
                    Group group = new Group();
                    String gn = group_nodes.item(n).getAttributes().getNamedItem("name").getNodeValue();
                    boolean skip_group = false;

                    if(gn.length() <= 0) {
                        Main.log.error("Group name can not be empty");
                        skip_group = true;
                    }

                    // check for dupes
                    for(Group g : groups) {
                        if(g.getName().equalsIgnoreCase(gn)) {
                            Main.log.error("Duplicate group name '"+gn+"' in monitors.xml, group and sub-monitors will not be created");
                            skip_group = true;
                        }
                    }

                    if(!skip_group) {
                        group.setName(gn);
                        groups.add(group);
                        group.setId(groups.size());
                        XPath sub_xpath = XPathFactory.newInstance().newXPath();
                        monitor_nodes = (NodeList) xpath.evaluate("./monitor", group_nodes.item(n), XPathConstants.NODESET);
                        for(int nx = 0; nx < monitor_nodes.getLength(); nx++) {
                            createMonitor(monitor_nodes.item(nx), xpath, group);
                        }
                    }
                }
            } catch(javax.xml.xpath.XPathExpressionException xpe) {
                Main.log.fatal("Error parsing monitors XML: "+xpe.getCause().getMessage());
                messages.add(xpe.getCause().getMessage());
            } catch (Exception e) {
                Main.log.fatal("Error parsing monitors XML: "+e+" "+e.getMessage());
                messages.add(e+" "+e.getMessage());
            }
        }
    }

    private void createMonitor(Node mon_node, XPath xpath, Group g)
    {
        try {
            if(mon_node.getAttributes().getNamedItem("name") == null) throw new Exception("name attribute missing from monitor element");
            if(mon_node.getAttributes().getNamedItem("type") == null) throw new Exception("type attribute missing from monitor element");
            if(mon_node.getAttributes().getNamedItem("interval") == null) throw new Exception("interval attribute missing from monitor element");
            
            String name = mon_node.getAttributes().getNamedItem("name").getNodeValue();
            if(name.length() <= 0) {
                Main.log.error("Monitor name can not be empty");
                messages.add("Monitor name can not be empty");
                return;
            }
            // test name for funky HTML chars and nasty junk
            if(!name.matches("^[^\"&'<>=\\|\\]\\[\\\\/\\*:]+$")) {
                Main.log.error("Invalid monitor name detected: '"+name+"' monitor will not be created. Invalid characters are: &\"<>='\\|[]/*:");
                messages.add("Invalid monitor name detected: '"+name+"' monitor will not be created. Invalid characters are: &\"<>='\\|[]/*:");
                return;
            }
            
            // check for dupes
            Vector<Monitor> mons;
            if(g != null)
                mons = g.getMonitors();
            else
                mons = getRootMonitors();

            for(Monitor m : mons) {
                if(m.getName().equalsIgnoreCase(name)) {
                    Main.log.error("Duplicate monitor name '"+name+"'. Monitor will not be created");
                    messages.add("Duplicate monitor name '"+name+"'. Monitor will not be created");
                    return;
                }
            }

            String type = mon_node.getAttributes().getNamedItem("type").getNodeValue();
            int interval = Integer.parseInt(mon_node.getAttributes().getNamedItem("interval").getNodeValue());
            Properties props = new Properties();
            
            NodeList prop_nodes = (NodeList) xpath.evaluate("./*", mon_node, XPathConstants.NODESET);
            for (int np = 0; np < prop_nodes.getLength(); np++) {
                String prop_name = prop_nodes.item(np).getNodeName();
                String prop_value = prop_nodes.item(np).getTextContent();

                // passwords are a special case,
                // if property name is 'password' then we run it through the decrypt
                if(prop_name.equalsIgnoreCase("password")) {
                    prop_value = com.bencoleman.ziz.utils.PasswordEncoder.decrypt(prop_value);
                }

                // handle servers
                if(prop_name.equalsIgnoreCase("include")) {
                    Include inc = getIncludeByName(prop_value);
                    
                    if(inc != null) {
                        /*props.setProperty("username", srv.username);
                        props.setProperty("password", com.bencoleman.ziz.utils.PasswordEncoder.decrypt(srv.password));
                        props.setProperty("hostname", srv.hostname);
                        if(srv.ssh_keyfile != null) props.setProperty("ssh_keyfile", srv.ssh_keyfile);
                        if(srv.ssh_port != null) props.setProperty("ssh_port", srv.ssh_keyfile);*/
                        for (Enumeration e = inc.getProps().keys(); e.hasMoreElements(); /**/) {
                            String p_key = (String) e.nextElement();
                            String p_value = inc.getProps().getProperty(p_key);
                            if(p_key.equalsIgnoreCase("password")) {
                                p_value = com.bencoleman.ziz.utils.PasswordEncoder.decrypt(p_value);
                            }
                            props.setProperty(p_key, p_value);
                        }
                    } else {
                        Main.log.error("Include '"+prop_value+"' is not defined, monitor '"+name+"' is invalid");
                        messages.add("Include '"+prop_value+"' is not defined, monitor '"+name+"' is invalid");
                        return;
                    }
                } else {
                    props.setProperty(prop_name, prop_value);
                }
            }

            // convert interval from minutes to seconds
            interval *= 60;

            // create the monitor object, set it's group (or null if it's at root) and add to the main monitors vector
            int id = monitors.size();
            Monitor mon = new Monitor(type, name, interval, props, g, id);

            monitors.add(mon);

            if(g != null) g.addMonitor(mon);
            
            Main.log.info("Created new monitor: " + mon+" in group "+(g == null?"<root>":g));
        } catch (Exception ex) {
            Main.log.fatal("Error parsing monitor from XML: "+ex+" "+ex.getMessage()+" ");
            messages.add(ex.getMessage());
        }
    }

    private void createInclude(Node inc_node, XPath xpath)
    {
        try {
            if(inc_node.getAttributes().getNamedItem("name") == null) throw new Exception("name attribute missing from server element");

            NodeList prop_nodes = (NodeList) xpath.evaluate("./*", inc_node, XPathConstants.NODESET);
            if(prop_nodes.getLength() <= 0) {
                return;
            } else {

                String inc_name = inc_node.getAttributes().getNamedItem("name").getTextContent();
                Include inc = new Include(inc_name);
                for (int np = 0; np < prop_nodes.getLength(); np++) {
                        inc.addProperty(prop_nodes.item(np).getNodeName(), prop_nodes.item(np).getTextContent());
                }
                includes.add(inc);
            }
            /*if(server_node.getAttributes().getNamedItem("name") == null) throw new Exception("name attribute missing from server element");
            if(server_node.getChildNodes().getLength() < 7) throw new Exception("server element requires; hostname, username &amp; password child elements");
            if(!server_node.getChildNodes().item(1).getNodeName().equalsIgnoreCase("hostname")) throw new Exception("hostname element not found in server");
            if(!server_node.getChildNodes().item(3).getNodeName().equalsIgnoreCase("username")) throw new Exception("username element not found in server");
            if(!server_node.getChildNodes().item(5).getNodeName().equalsIgnoreCase("password")) throw new Exception("password element not found in server");

            String name = server_node.getAttributes().getNamedItem("name").getNodeValue();
            String host = server_node.getChildNodes().item(1).getTextContent();
            String user = server_node.getChildNodes().item(3).getTextContent();
            String pass = server_node.getChildNodes().item(5).getTextContent();
            Server srv = new Server(name, host, user, pass);
            servers.add(srv);*/
        } catch (Exception ex) {
            Main.log.fatal("Error parsing server from XML: "+ex+" "+ex.getMessage()+" ");
            messages.add(ex.getMessage());
            //ex.printStackTrace();
        }
    }

    public Vector<Monitor> getRootMonitors()
    {
        Vector<Monitor> temp = new Vector(20);
        for(Monitor m : monitors) {
            if(m.getParentGroup() == null)
                temp.add(m);
        }
        
        return temp;
    }

    public Monitor findMonitor(String mon_name, String group_name)
    {
        for(Monitor m : getMonitors()) {
            if(m.getName().equals(mon_name)) {
                if(group_name != null) {
                    if(m.getParentGroup().getName().equals(group_name))
                        return m;
                } else {
                    return m;
                }
            }
        }
        
        return null;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }
    
}