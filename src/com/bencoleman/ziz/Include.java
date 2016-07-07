/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz;

import java.util.Properties;

/**
 *
 * @author colemanb
 */
public class Include
{
    private String name;
    private Properties props;

    public Include(String name)
    {
        this.name = name;
        props = new Properties();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Properties getProps() {
        return props;
    }

    public void addProperty(String name, String value)
    {
        props.setProperty(name, value);
    }
}
