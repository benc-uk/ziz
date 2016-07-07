using System;
using System.IO;


public class Logger
{
    private StreamWriter log;

    public Logger()
    {
        log = new StreamWriter("log\\collector.log", true);
    }

    public void error(string msg)
    {
        log.WriteLine(DateTime.Now.ToLocalTime()+"\tERROR:\t"+msg);
        log.Flush();
    }

    public void info(string msg)
    {
        log.WriteLine(DateTime.Now.ToLocalTime() + "\tINFO:\t" + msg);
        log.Flush();
    }
}

