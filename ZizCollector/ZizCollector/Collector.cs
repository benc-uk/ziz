using System;
using System.Net.Sockets;
using System.Net;
using System.IO;
using System.Threading;

public class Collector
{
    private int port;
    public static Logger log;

    public Collector(int tcp_port)
    {
        Collector.log = new Logger();
        port = tcp_port;
    }

    protected void start()
    {
        try
        {
            TcpListener server = new TcpListener(IPAddress.Parse("127.0.0.1"), port);

            // Start listening for client requests.
            server.Start();
            log.info("Server started on port: "+port);

            // Buffer for reading data
            Byte[] buffer = new Byte[256];

            // Enter the listening loop.
            while (true)
            {
                log.info("Waiting for connection");

                // Perform a blocking call to accept requests.
                // You could also user server.AcceptSocket() here.
                TcpClient client = server.AcceptTcpClient();
                log.info("Client connected: " + client.Client.RemoteEndPoint);

                CommandProcessor proc = new CommandProcessor(client);
                Thread thread = new Thread(proc.processClient);
                thread.Start();
            }
        }
        catch (SocketException e)
        {
            log.error(e.ErrorCode+" "+e.Message);
            if (e.ErrorCode == 10048) {
                Environment.Exit(10048);
            }
        }

    }

    public static void Main(string[] args)
    {
        int port = 12907;
        if (args.Length > 0) {
            port = Int32.Parse(args[0]);
        }

        Collector c = new Collector(port);
        c.start();
    }



    /*private static void getPerfCounter()
    {
        try
        {
            PerformanceCounter counter = new PerformanceCounter("Processor", "% Processor Time", "_Total", "zeno");
            counter.NextValue();
            System.Threading.Thread.Sleep(1200);
            while (true)
            {
                Console.WriteLine(counter.NextValue());
                System.Threading.Thread.Sleep(1200);
            }
        }
        catch (Exception e)
        {
            Console.Write(e.Message);
        }
    }*/
}


