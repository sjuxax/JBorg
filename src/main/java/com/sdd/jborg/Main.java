package com.sdd.jborg;

import com.sdd.jborg.cloud.Aws;
import com.sdd.jborg.cloud.OpenStack;

import static com.sdd.jborg.scripts.Standard.*;

public class Main
{
	public static void main(String[] args)
	{
		server.setFqdn(args[1]);

		final Script script = Script.findMatch();
		if (script == null)
		{
			Logger.err("Unable to locate matching script.");
			return;
		}
		script.assimilate(); // loop 1

		switch (args[0].toLowerCase())
		{
			case "assemble":
				// TODO: lookup openstack datacenter params dynamically by server fqdn
				final String provider = networks.getObject("datacenters").getObject("sbi-slc").getString("provider");
				switch (provider)
				{
					case "openstack":
						OpenStack.create(server.getString("fqdn"));
						break;
					case "aws":
						Aws.create(server.getString("fqdn"));
						break;
					default:
						die(new Exception("Unsupported provider: "+ provider));
						return;
				}

				// flow through to assimilate

			case "assimilate":
				Logger.setHost(server.getObject("ssh").getString("host"));
				ssh = new Ssh().connect(
					server.getObject("ssh").getString("host"),
					server.getObject("ssh").getInteger("port"),
					server.getObject("ssh").getString("user"),
					server.getObject("ssh").getString("key")
				);

				go(); // loop 2
				ssh.close();
				Logger.info("Assimilation complete.");
				break;
		}
	}

	public static void die(final Exception reason)
	{
		Logger.err("Aborting. Reason: " + reason.getMessage());
		reason.printStackTrace();
		System.exit(1);
	}
}
