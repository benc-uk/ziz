using System;
using System.Management;

class WMIUtils
{
    public static string GetWMICounter(string host, string user, string pass, string wmiclass, string inst, string props)
    {
        try
        {
            Collector.log.info("Fetching WMI data for class: "+wmiclass+" from host: "+host);
            if (wmiclass == null || wmiclass.Length <= 0)
                return "ERROR: wmiclass not supplied or invalid";
            if (host == null || host.Length <= 0)
                return "ERROR: hostname not supplied or invalid";
 
            ConnectionOptions options = new ConnectionOptions(); 
            if (!host.ToLower().Equals("localhost"))
            {
                options.Username = user;
                options.Password = pass;
            }

            string clause = "";
            if (inst != null && inst.Length > 0) {
                clause = " WHERE Name=\"" + inst + "\"";
            }

            ManagementScope scope = new ManagementScope("\\\\" + host + "\\root\\cimv2", options);
            scope.Connect();
            SelectQuery query = new SelectQuery("SELECT "+props+" FROM Win32_PerfFormattedData_" + wmiclass + clause);
            //Console.WriteLine(query.QueryString);
            ManagementObjectSearcher search = new ManagementObjectSearcher(scope, query);

            String data = "";
            ManagementObjectCollection result = search.Get();
            if (result.Count == 0)
            {
                //Console.WriteLine("0 results!");
                return "ERROR: No results";
            }
            else
            {
                //Console.WriteLine("HERE...");
                int count = result.Count;
                int index = 0;
                foreach (ManagementObject item in result)
                {
                    string comma = "";
                    foreach (PropertyData prop in item.Properties)
                    {
                        data += comma + prop.Name + "=" + prop.Value;
                        comma = ",";
                    }
 
                    if (index != count-1)
                        data = data + "\u0003";
                    index++;
                }
                return data;
            }

            return "ERROR: No results";
        }
        catch (Exception e)
        {
            Collector.log.error(e.Message);
            return "ERROR: " + e.Message;
        }
    }

    public static string execWMIQuery(string host, string user, string pass, string query_str)
    {
        try
        {
            Collector.log.info("Fetching WMI query: " + query_str + " from host: " + host);
            if (query_str == null || query_str.Length <= 0)
                return "ERROR: query not supplied or invalid";
            if (host == null || host.Length <= 0)
                return "ERROR: hostname not supplied or invalid";

            ConnectionOptions options = new ConnectionOptions();
            if (!host.ToLower().Equals("localhost"))
            {
                options.Username = user;
                options.Password = pass;
            }

            ManagementScope scope = new ManagementScope("\\\\" + host + "\\root\\cimv2", options);
            scope.Connect();
            SelectQuery query = new SelectQuery(query_str);
            ManagementObjectSearcher search = new ManagementObjectSearcher(scope, query);

            String data = "";
            ManagementObjectCollection result = search.Get();
            if (result.Count == 0)
            {
                return "ERROR: No results";
            }
            else
            {
                int count = result.Count;
                int index = 0;
                foreach (ManagementObject item in result)
                {
                    string comma = "";
                    foreach (PropertyData prop in item.Properties)
                    {
                        data += comma + prop.Name + "=" + prop.Value;
                        comma = ",";
                    }

                    if (index != count - 1)
                        data = data + '\x09';
                    index++;
                }
                return data;

            }
        }
        catch (Exception e)
        {
            Collector.log.error(e.Message);
            return "ERROR: " + e.Message;
        }
    }
}

