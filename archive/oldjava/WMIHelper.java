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

/*import org.jinterop.dcom.common.*;
import org.jinterop.dcom.core.*;
import org.jinterop.dcom.impls.*;
import org.jinterop.dcom.impls.automation.*;
*/




public class WMIHelper 
{
    /*
    private boolean connected;
    private JIComServer comStub = null;
    private IJIComObject comObject = null;
    private IJIDispatch dispatch = null;
    private String address = null;
    private JISession session = null;

    public WMIHelper(String domain, String user, String pass)
    {
        try {
            JISystem.setAutoRegisteration(true);
            session = JISession.createSession(domain, user, pass);
            session.useSessionSecurity(false);
            session.setGlobalSocketTimeout(5000);

            comStub = new JIComServer(JIProgId.valueOf("WbemScripting.SWbemLocator"), "zeno", session);
            IJIComObject unknown = comStub.createInstance();
            comObject = (IJIComObject) unknown.queryInterface("76A6415B-CB41-11d1-8B02-00600806D9B6");//ISWbemLocator
            //This will obtain the dispatch interface
            dispatch = (IJIDispatch) JIObjectFactory.narrowObject(comObject.queryInterface(IJIDispatch.IID));

            System.out.println("HHHHEERERE!");
            JIVariant results[] = dispatch.callMethodA("ConnectServer",new Object[]{new JIString("zeno"), new JIString("root\\cimv2"),
                                                        JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM()
                                                        ,JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM(),
                                                        new Integer(0),JIVariant.OPTIONAL_PARAM()});

            System.out.println("HHHHEERERE!");
            IJIDispatch wbemServices_dispatch = (IJIDispatch)JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());
            results = wbemServices_dispatch.callMethodA("ExecQuery", new Object[]{new JIString("Select * from Win32_LogicalDisk"), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM()});
            IJIDispatch wbemObjectSet_dispatch = (IJIDispatch)JIObjectFactory.narrowObject((results[0]).getObjectAsComObject());
            System.out.println("HHHHEERERE!");
            JIVariant variant = wbemObjectSet_dispatch.get("_NewEnum");
            IJIComObject object2 = variant.getObjectAsComObject();

            System.out.println(object2.isDispatchSupported());

            System.out.println("HHHHEERERE!");
            object2.registerUnreferencedHandler(new IJIUnreferenced(){
                    public void unReferenced()
                    {
                            System.out.println("object2 unreferenced...");
                    }
            });

            IJIEnumVariant enumVARIANT = (IJIEnumVariant)JIObjectFactory.narrowObject(object2.queryInterface(IJIEnumVariant.IID));

        } catch (JIException jie) {
            jie.printStackTrace();
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }

    }

    public void query(String query, String[] props)
    {

    }
*/
}
