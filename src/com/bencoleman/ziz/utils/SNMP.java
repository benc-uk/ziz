/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bencoleman.ziz.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.*;
import org.snmp4j.util.*;

/**
 *
 * @author colemanb
 */
public class SNMP
{
    private Snmp snmp;
    private CommunityTarget target;
    private TransportMapping transport;

    // private SnmpContext context;
    public SNMP(String hostname, String comm_string, int port, int version) throws Exception
    {
        transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
        target = new CommunityTarget();
        target.setCommunity(new OctetString(comm_string));
        target.setAddress(new UdpAddress(InetAddress.getByName(hostname), 161));
        target.setRetries(1);
        target.setTimeout(2000);
        target.setVersion(2);
        if(version == 2)
            target.setVersion(SnmpConstants.version2c);
        else
            target.setVersion(SnmpConstants.version1);
    }

    public String getSingleOID(String oid) throws IOException
    {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        ResponseEvent resp = snmp.send(pdu, target);
        PDU resp_pdu = resp.getResponse();

        VariableBinding vb = (VariableBinding)resp_pdu.getVariableBindings().get(0);
        return vb.getVariable().toString();
    }

    public String[] getColumnOID(String oid)
    {
        TableUtils tu = new TableUtils(snmp, new DefaultPDUFactory());
        List<TableEvent> out = tu.getTable(target, new OID[]{new OID(oid)}, null, null);
        System.out.println(out);
        String[] result = new String[out.size()];
        int i = 0;
        for(TableEvent te : out) {
            result[i] = te.getColumns()[0].getVariable().toString();
            i++;
        }

        return result;
    }

    public String[][] getColumnOIDExtra(String oid)
    {
        TableUtils tu = new TableUtils(snmp, new DefaultPDUFactory());
        List<TableEvent> out = tu.getTable(target, new OID[]{new OID(oid)}, null, null);
        System.out.println(out);
        String[][] result = new String[out.size()][2];
        int i = 0;
        for(TableEvent te : out) {
            result[i][0] = te.getColumns()[0].getVariable().toString();
            result[i][1] = te.getColumns()[0].getOid().toString();
            i++;
        }

        return result;
    }

    public void close() throws IOException
    {
        this.snmp.close();
    }

    /*public static void main(String[] args)
    {
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("cheeseball"));
            target.setAddress(new UdpAddress(InetAddress.getByName("192.168.1.100"), 161));
            target.setRetries(0);
            target.setTimeout(500);
            target.setVersion(2);
            target.setVersion(SnmpConstants.version1);

            TableUtils tu = new TableUtils(snmp, new DefaultPDUFactory());
            //List<TreeEvent> out = tu.getSubtree(target, new OID(".1.3.6.1.2.1.25.2.3.1"));
            List<TableEvent> out = tu.getTable(target, new OID[]{new OID(".1.3.6.1.2.1.25.2.3.1.5"), new OID(".1.3.6.1.2.1.25.2.3.1.6")}, null, null);
            for(TableEvent te : out) {
                System.out.println(te.getColumns()[0]);
                System.out.println(te.getColumns()[1]);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/
}
