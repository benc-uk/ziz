/*
 * JawTest.java
 *
 * Created on 16 August 2007, 13:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.bencoleman.ziz;

import java.net.*;
import java.util.HashMap;

//import org.jawin.*;
//import org.jawin.win32.*;
//import org.jawin.constants.*;

/**
 *
 * @author Ben Coleman
 */
public class WMIHelper2
{
/*
    private boolean connected;
    private DispatchPtr WmiService;
    private int perf_counter_refresh_delay = 1;
    
    public WMIHelper2()
    {
        connected = false;
        perf_counter_refresh_delay = 1;
    }
    
    public boolean connect(String host, String username, String password) throws COMException
    {
        Ole32.CoInitialize();
        
        String local_host_name = "localhost";
        try {
            local_host_name = InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch (UnknownHostException ex) {
        }

        DispatchPtr locator = new DispatchPtr("WbemScripting.SWbemLocator");

        // OK invoke the ConnectServer method on the WMIService (SWbemLocator)
        //  - Note. Local connections take different parameters (you can't specify a username/password) so we need to capture that
        if(host.toLowerCase().equalsIgnoreCase("localhost") || host.toLowerCase().startsWith("127.0.0") || host.toLowerCase().startsWith(local_host_name))  {
            // We've "guessed" that we need a local connection
            WmiService = (DispatchPtr)locator.invoke("ConnectServer", host, "root\\cimv2");
        } else {
            // Remote connection
            // The magic 128 at the end of the connect parameters means timeout after 2 minutes of trying, 
            //  - however this only works on OS higher than W2K. The probe _has_ to run on XP or higher
            WmiService = (DispatchPtr)locator.invokeN("ConnectServer", new Object[]{host, "root\\cimv2", username, password, "", "", 128});            
        }

        locator.close();
        locator = null;

        connected = true;
        return true;
    }
    
    public void close() throws COMException
    {
        System.out.println("Closing WMI...");
        WmiService.close();
        WmiService = null;
        Ole32.CoUninitialize();
    }
    
    public HashMap[] query(String query, String[] props) throws COMException 
    {
        IdentityManager.registerProxy(WellKnownGUIDs.IID_IUnknown, IEnumVariant.class);

        DispatchPtr lSWbemObjectSet = (DispatchPtr)WmiService.invoke("ExecQuery", query);

        int count = ((Integer)lSWbemObjectSet.get("Count")).intValue();
        IEnumVariant item_enum = (IEnumVariant)lSWbemObjectSet.get("_NewEnum");

        HashMap[] data = new HashMap[count];
        for(int temp = 0; temp < count; temp++) {
            data[temp] = new HashMap(props.length);
        }
        
        for(int i = 0; i < count; i++) {
            DispatchPtr items[] = new DispatchPtr[1];
            item_enum.Next(1, items);
            for(String prop : props) {
                data[i].put(prop, items[0].get(prop));
            }
        }

        lSWbemObjectSet.close();
        item_enum.close();

        return data;
    }
     
    public HashMap[] perfCounterCooked(String perf_class, String[] props) throws COMException 
    {
        IdentityManager.registerProxy(WellKnownGUIDs.IID_IUnknown, IEnumVariant.class);

        DispatchPtr refresher = new DispatchPtr("WbemScripting.Swbemrefresher");
        DispatchPtr ref_enum = (DispatchPtr) refresher.invokeN("AddEnum", new Object[]{WmiService, "Win32_PerfFormattedData_"+perf_class});
        DispatchPtr item_set = (DispatchPtr)ref_enum.get("ObjectSet");

        refresher.invoke("Refresh");
        try {
            Thread.sleep(getPerfCounterRefreshDelay() * 1010);
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        }
        refresher.invoke("Refresh");
        
        int count = ((Integer)item_set.get("Count")).intValue();
        //System.out.println(count);
        HashMap[] data = new HashMap[count];
        for(int temp = 0; temp < count; temp++) {
            data[temp] = new HashMap(props.length);
        }
        
        IEnumVariant item_enum = (IEnumVariant)item_set.get("_NewEnum");
        
        for(int i = 0; i < count; i++) {
            //System.out.println("inside loop "+i);
            DispatchPtr items[] = new DispatchPtr[1];
            item_enum.Next(1, items);
            for(String prop : props) {
                //System.out.println(prop+" === "+items[0].get(prop));
                data[i].put(prop, items[0].get(prop));
            }
            items = null;
        }


        item_enum.close();
        refresher.close();
        ref_enum.close();
        item_set.close();
        item_enum = null;
        refresher = null;
        ref_enum = null;
        item_set = null;
        
        return data;        
    }
    
    public HashMap[] perfCounterRaw(String perf_class, String[] props) throws COMException 
    {
        IdentityManager.registerProxy(WellKnownGUIDs.IID_IUnknown, IEnumVariant.class);

        DispatchPtr refresher = new DispatchPtr("WbemScripting.Swbemrefresher");
        DispatchPtr ref_enum = (DispatchPtr) refresher.invokeN("AddEnum", new Object[]{WmiService, "Win32_PerfRawData_"+perf_class}); 
        DispatchPtr item_set = (DispatchPtr)ref_enum.get("ObjectSet");
        
        refresher.invoke("Refresh");
        
        int count = ((Integer)item_set.get("Count")).intValue();
        HashMap[] data = new HashMap[count];
        for(int temp = 0; temp < count; temp++) {
            data[temp] = new HashMap(props.length);
        }
        
        IEnumVariant item_enum = (IEnumVariant)item_set.get("_NewEnum");
        
        for(int i = 0; i < count; i++) {
            DispatchPtr items[] = new DispatchPtr[1];
            item_enum.Next(1, items);
            for(String prop : props) {
                //System.out.println(prop+" === "+items[0].get(prop));
                data[i].put(prop, items[0].get(prop));
            }
        }
        
        item_enum.close();
        refresher.close();
        ref_enum.close();
        item_set.close();
        
        return data;          
    }

    public int getPerfCounterRefreshDelay()
    {
        return perf_counter_refresh_delay;
    }

    public void setPerfCounterRefreshDelay(int delay_seconds)
    {
        this.perf_counter_refresh_delay = delay_seconds;
    }
    */
}
