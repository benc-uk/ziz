using System;
using System.Management;
using System.Diagnostics;
using System.Net.Sockets;
using System.Net;
using System.IO;
using System.Threading;


class CommandProcessor
{
    private TcpClient client;
    private char delim = '\x03';

    public CommandProcessor(TcpClient tcp_client)
    {
        client = tcp_client;
    }

    public void processClient()
    {
        // Get a stream object for reading and writing
        NetworkStream stream = client.GetStream();
        StreamReader reader = new StreamReader(stream);
        try
        {
            // Loop to receive all the data sent by the client.
            while (stream.CanRead)
            {
                string line = reader.ReadLine();
                //Console.WriteLine("GOT=" + line);
                if (line == null) continue;
                bool valid_command = false;

                //Collector.log.info("Received: " + line);
                string[] parts = line.Split(delim);
                if (parts[0].ToLower().StartsWith("quit")) {
                    Collector.log.info("QUIT Command");
                    client.Close();
                    return;
                }
                if (parts[0].ToLower().StartsWith("delim"))
                {
                    valid_command = true;
                    Collector.log.info("DELIM Command");
                    delim = line[6];
                    sendMsg("New delimiter: "+delim, stream);
                }

                if (parts[0].ToLower().StartsWith("wmi_counter"))
                {
                    valid_command = true;
                    Collector.log.info("WMI_COUNTER Command");
                    string hostname = null;
                    string username = null;
                    string password = null;
                    string wmiclass = null;
                    string instance = null;
                    string properties = null;
                    for (int p = 1; p < parts.Length; p++)
                    {
                        string part = parts[p];
                        if (part.ToLower().StartsWith("hostname"))
                            hostname = part.Split(new char[] {'='}, 2)[1];

                        if (part.ToLower().StartsWith("username"))
                            username = part.Split(new char[] {'='}, 2)[1];

                        if (part.ToLower().StartsWith("password"))
                            password = part.Split(new char[] { '=' }, 2)[1];

                        if (part.ToLower().StartsWith("wmiclass"))
                            wmiclass = part.Split(new char[] {'='}, 2)[1];

                        if (part.ToLower().StartsWith("instance"))
                            instance = part.Split(new char[] {'='}, 2)[1].ToLower();

                        if (part.ToLower().StartsWith("properties"))
                            properties = part.Split(new char[] {'='}, 2)[1];
                    }
                    String res = WMIUtils.GetWMICounter(hostname, username, password, wmiclass, instance, properties);
                    sendMsg(res, stream);
                }

                if (parts[0].ToLower().StartsWith("wmi_query"))
                {
                    valid_command = true;
                    Collector.log.info("WMI_QUERY Command");
                    string hostname = null;
                    string username = null;
                    string password = null;
                    string query = null;
                    for (int p = 1; p < parts.Length; p++)
                    {
                        string part = parts[p];
                        if (part.ToLower().StartsWith("hostname"))
                            hostname = part.Split(new char[] { '=' }, 2)[1];

                        if (part.ToLower().StartsWith("username"))
                            username = part.Split(new char[] { '=' }, 2)[1];

                        if (part.ToLower().StartsWith("password"))
                            password = part.Split(new char[] { '=' }, 2)[1];

                        if (part.ToLower().StartsWith("query"))
                            query = part.Split(new char[] { '=' }, 2)[1];
                    }
                    String res = WMIUtils.execWMIQuery(hostname, username, password, query);
                    sendMsg(res, stream);
                }

                if(!valid_command)
                    sendMsg("ERROR: Unknown command: " + parts[0], stream);
            }
        }
        catch (IOException e)
        {
            Collector.log.error(e.Message);
        }

        // Shutdown and end connection
        client.Close();
    }

    private void sendMsg(string msg_str, NetworkStream stream)
    {
        byte[] msg = System.Text.Encoding.ASCII.GetBytes(msg_str + "\n\r");
        stream.Write(msg, 0, msg.Length);
        Collector.log.info("Response: "+msg_str);
    }
}

