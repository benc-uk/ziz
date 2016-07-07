/*
 * Copyright (C) Ben Coleman. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 2.0, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package com.bencoleman.ziz.utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import com.bencoleman.ziz.Main;

/**
 *
 * @author colemanb
 */
public class HttpResourceDownloader
{
    private HashMap<String, String> urls;
    protected long tot_res_bytes;
    private int thread_count;
    private URL base;

    public HttpResourceDownloader(URL base_url, int threads)
    {
        urls = new HashMap(100);
        thread_count = threads;
        base = base_url;
    }

    public long downloadAll(String html)
    {
        //|url\\((.*?)\\)|<link.*?stylesheet.*?href=[\"|'](.*?)[\"|']
        Pattern re = Pattern.compile("src=[\"|'](.*?)[\"|']|url\\((.*?)\\)|<link.*?stylesheet.*?href=[\"|'](.*?)[\"|']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = re.matcher(html);
        //System.out.println(""+matcher.find());
        while(matcher.find()) {
            for(int g = 1; g <= matcher.groupCount(); g++) {
                if(matcher.group(g) != null) {
                    //System.out.println(matcher.group(g));
                    urls.put(matcher.group(g).replace('\'', ' ').replace('\"', ' ').trim(), "");
                }
            }
        }

        // remove any blank urls
        urls.remove("");

        // pool of downloader threads to do the work
        Downloader[] loaders = new Downloader[thread_count];
        Iterator<String> url_strings = urls.keySet().iterator();

        try {
            // main loop - through all the urls we need to download
            while(url_strings.hasNext()) {
                // loop through the thread pool
                for(int d = 0; d < thread_count; d++) {
                    // if we find any empty slots or threads that have finished, then set them working on the next url
                    if(loaders[d] == null || !loaders[d].thread.isAlive()) {
                        if(!url_strings.hasNext()) continue;
                        try {
                            // contruct a full proper URL from the string and the base URL
                            URL new_url = new URL(base, url_strings.next());
                            loaders[d] = new Downloader(new_url);
                        } catch(MalformedURLException mue) {
                        }
                    }
                }
                // now we poll all the threads to see if they are busy, soon as one has finsihed we break and return to the top of the main loop
                int d = 0;
                while(true) {
                    if(!loaders[d].thread.isAlive()) { 
                        break;
                    }
                    // increment thread counter, and loop back to zero if over the end
                    d++;
                    if(d >= thread_count) d = 0;
                    // need a tiny pause to stop the CPU getting hammered
                    Thread.sleep(10);
                }
            }

            // wait for remaining threads to finish downloading
            boolean still_busy;
            do {
                still_busy = false;
                for(int d = 0; d < thread_count; d++) {
                    if(loaders[d].thread.isAlive()) still_busy = true;
                }
                // need a tiny pause to stop the CPU getting hammered
                Thread.sleep(10);
            } while(still_busy);


        } catch (Exception ex) {
        }

        return tot_res_bytes;
    }

    private class Downloader implements Runnable
    {
        public Thread thread;
        public URL url;

        public Downloader(URL url)
        {
            this.url = url;
            thread = new Thread(this);
            thread.start();
        }

        public void run()
        {
            try {
                //System.out.println("   Downloading "+url+"...");
                HttpURLConnection http;
                if(Main.config.containsKey("mon_http_proxy")) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Main.config.getStr("mon_http_proxy"), Main.config.getInt("mon_http_proxy_port")));
                    http = (HttpURLConnection) url.openConnection(proxy);
                    if(Main.config.containsKey("mon_http_proxy_user")) {
                        String proxy_auth = Base64Coder.encodeString(Main.config.getStr("mon_http_proxy_user")+":"+Main.config.getPassword("mon_http_proxy_password"));
                        http.setRequestProperty("Proxy-Authorization", "Basic " + proxy_auth);
                    }
                } else {
                    http = (HttpURLConnection) url.openConnection();
                }
                http.connect();
                InputStream is = http.getInputStream();
                byte[] buff = new byte[2048];
                int read_in = 0;
                while ((read_in = is.read(buff)) != -1) {
                    tot_res_bytes += read_in;
                }
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }
}
